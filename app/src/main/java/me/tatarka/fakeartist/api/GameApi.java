package me.tatarka.fakeartist.api;

import android.graphics.Color;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.events.ConnectEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.LiveRoomInfoEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.MatchedRoomsEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.MoveEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomData;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import me.tatarka.fakeartist.api.listeners.ConnectonRequestAdapter;
import me.tatarka.fakeartist.api.listeners.NotifyAdapter;
import me.tatarka.fakeartist.api.listeners.RoomRequestAdapter;
import me.tatarka.fakeartist.api.listeners.ZoneRequestAdapter;
import me.tatarka.fakeartist.game.main.Drawing;
import rx.subjects.BehaviorSubject;

import static com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode.SUCCESS;

@Singleton
public class GameApi {

    private static final String TAG = "GameApi";
    private static final int MAX_TURN_TIME = 3600; // seconds

    private static final int NEXT_STATE_NONE = 0;
    private static final int NEXT_STATE_CREATE_ROOM = 1;
    private static final int NEXT_STATE_FIND_ROOM = 2;
    private static final int NEXT_STATE_LOCK_QM = 3;
    private static final int NEXT_STATE_ROOM_INFO = 4;

    private final WarpClient client;
    private final State.Builder currentState;
    private final BehaviorSubject<Game> subject;

    private int nextState = NEXT_STATE_NONE;

    @Inject
    public GameApi(final WarpClient client) {
        this.client = client;
        this.currentState = new State.Builder();
        this.subject = BehaviorSubject.create(new Game(Event.LOADING, currentState.build()));
    }

    public rx.Observable<Game> connect() {
        client.addConnectionRequestListener(new ConnectonRequestAdapter() {
            @Override
            public void onConnectDone(ConnectEvent connectEvent) {
                int state = nextState;
                nextState = NEXT_STATE_NONE;
                if (connectEvent.getReasonCode() == SUCCESS) {
                    switch (state) {
                        case NEXT_STATE_CREATE_ROOM:
                            doCreateRoom(currentState.roomName, currentState.userName);
                            break;
                        case NEXT_STATE_FIND_ROOM:
                            doFindRoom(currentState.roomName);
                            break;
                    }
                }
            }

            @Override
            public void onDisconnectDone(ConnectEvent connectEvent) {
                if (connectEvent.getReasonCode() == SUCCESS) {
                    doDisconnected();
                }
            }
        });

        client.addZoneRequestListener(new ZoneRequestAdapter() {
            @Override
            public void onCreateRoomDone(RoomEvent roomEvent) {
                if (roomEvent.getResult() == SUCCESS) {
                    nextState = NEXT_STATE_LOCK_QM;
                    String roomId = roomEvent.getData().getId();
                    String roomName = roomEvent.getData().getName();
                    doJoinRoom(roomId, roomName);
                }
            }

            @Override
            public void onGetMatchedRoomsDone(MatchedRoomsEvent matchedRoomsEvent) {
                if (matchedRoomsEvent.getResult() == SUCCESS) {
                    RoomData[] datas = matchedRoomsEvent.getRoomsData();
                    if (datas.length != 0) {
                        RoomData data = datas[0];
                        String roomId = data.getId();
                        String roomName = data.getName();
                        doJoinRoom(roomId, roomName);
                    }
                }
            }
        });

        client.addRoomRequestListener(new RoomRequestAdapter() {
            @Override
            public void onJoinAndSubscribeRoomDone(RoomEvent roomEvent) {
                int state = nextState;
                nextState = NEXT_STATE_NONE;
                if (roomEvent.getResult() == SUCCESS) {
                    String roomId = roomEvent.getData().getId();
                    switch (state) {
                        case NEXT_STATE_LOCK_QM:
                            nextState = NEXT_STATE_ROOM_INFO;
                            String userName = roomEvent.getData().getRoomOwner();
                            doLockQm(roomId, userName);
                            break;
                        default:
                            doGetRoomInfo(roomId);
                            break;
                    }
                }
            }

            @Override
            public void onGetLiveRoomInfoDone(LiveRoomInfoEvent liveRoomInfoEvent) {
                if (liveRoomInfoEvent.getResult() == SUCCESS) {
                    List<String> players = Arrays.asList(liveRoomInfoEvent.getJoinedUsers());
                    doConnected(players, liveRoomInfoEvent.getProperties());
                    boolean ready = liveRoomInfoEvent.getProperties().containsKey("ready-" + currentState.userName);
                    if (ready) {
                        doReadyComplete();
                    }
                }
            }

            @Override
            public void onLockPropertiesDone(byte b) {
                int state = nextState;
                nextState = NEXT_STATE_NONE;
                switch (state) {
                    case NEXT_STATE_ROOM_INFO:
                        doGetRoomInfo(currentState.roomId);
                        break;
                }
            }

            @Override
            public void onUpdatePropertyDone(LiveRoomInfoEvent liveRoomInfoEvent) {
                if (liveRoomInfoEvent.getResult() == SUCCESS) {
                    currentState.set(liveRoomInfoEvent.getProperties());
                    if (isEveryoneReady(currentState.build(), liveRoomInfoEvent.getProperties())) {
                        doStart();
                    } else if (isReady(currentState.userName, liveRoomInfoEvent.getProperties())) {
                        doReadyComplete();
                    }
                }
            }
        });

        client.addNotificationListener(new NotifyAdapter() {
            @Override
            public void onRoomDestroyed(RoomData roomData) {
                doDisconnect();
            }

            @Override
            public void onUserJoinedRoom(RoomData roomData, String player) {
                if (roomData.getId().equals(currentState.roomId)) {
                    doJoinedRoom(player);
                }
            }

            @Override
            public void onUserLeftRoom(RoomData roomData, String player) {
                if (roomData.getId().equals(currentState.roomId)) {
                    doLeftRoom(player);
                }
            }

            @Override
            public void onGameStarted(String username, String roomId, String roomOwner) {
                if (roomId.equals(currentState.roomId)) {
                    doStarted(username);
                }
            }

            @Override
            public void onUserChangeRoomProperty(RoomData roomData, String username, HashMap<String, Object> properties, HashMap<String, String> lockedProperties) {
                if (roomData.getId().equals(currentState.roomId)) {
                    currentState.set(properties);
                }
            }

            @Override
            public void onMoveCompleted(MoveEvent moveEvent) {
                int player = currentState.players.indexOf(moveEvent.getSender());
                Drawing.Line lastLine = currentState.drawing.lastLine();
                // Prevents the list getting added twice since the sill be called for your own turn finish.
                if (lastLine == null || player != lastLine.player) {
                    Drawing.Line line = Drawing.Line.deserialize(player, moveEvent.getMoveData());
                    currentState.drawing = currentState.drawing.withNewLine(line);
                }
                currentState.turn = moveEvent.getNextTurn();
                subject.onNext(new Game(Event.TURN, currentState.build()));
            }
        });

        return subject;
    }

    public void createRoom(String userName) {
        nextState = NEXT_STATE_CREATE_ROOM;
        doConnect(generateRoomName(), userName);
    }

    public void joinRoom(String roomName, String userName) {
        nextState = NEXT_STATE_FIND_ROOM;
        doConnect(roomName, userName);
    }

    public void leaveRoom() {
        doDisconnect();
    }

    public void setup(State state, String category, String title) {
        doSetup(state, category, title, pickFakeArtist(state), generateColors(state));
    }

    public void ready(State state) {
        doReady(state);
    }

    public void finishTurn(Drawing drawing) {
        Drawing.Line line = drawing.lastLine();
        if (line == null) {
            throw new IllegalStateException();
        }
        client.sendMove(line.serialize(), currentState.nextTurn());
    }

    private void doConnect(String roomName, String userName) {
        currentState.roomName = roomName;
        currentState.userName = userName;
        client.connectWithUserName(userName);
    }

    private void doDisconnect() {
        client.disconnect();
    }

    private void doCreateRoom(String roomName, String userName) {
        currentState.roomName = roomName;
        client.createTurnRoom(roomName, userName,
                Game.MAX_PLAYERS,
                Props.of().put("roomName", roomName).build(),
                MAX_TURN_TIME);
    }

    private void doJoinRoom(String roomId, String roomName) {
        currentState.roomId = roomId;
        currentState.roomName = roomName;
        client.joinAndSubscribeRoom(roomId);
    }

    private void doFindRoom(String roomName) {
        currentState.roomName = roomName;
        client.getRoomInRangeWithProperties(1, Game.MAX_PLAYERS,
                Props.of().put("roomName", roomName).build());
    }

    private void doLockQm(String roomId, String userName) {
        currentState.roomId = roomId;
        currentState.userName = userName;
        currentState.qm = userName;
        HashMap<String, Object> props = new HashMap<>();
        currentState.get(props);
        client.lockProperties(props);
    }

    private void doGetRoomInfo(String roomId) {
        currentState.roomId = roomId;
        client.getLiveRoomInfo(roomId);
    }

    private void doConnected(List<String> players, HashMap<String, Object> props) {
        currentState.setPlayers(players);
        currentState.set(props);
        subject.onNext(new Game(Event.CONNECTED, currentState.build()));
    }

    private void doDisconnected() {
        subject.onNext(new Game(Event.DISCONNECTED, currentState.build()));
    }

    private void doJoinedRoom(String player) {
        currentState.addPlayer(player);
        subject.onNext(new Game(Event.PLAYER_JOINED, currentState.build()));
    }

    private void doLeftRoom(String player) {
        currentState.removePlayer(player);
        subject.onNext(new Game(Event.PLAYER_LEFT, currentState.build()));
    }

    private void doSetup(State state, String category, String title, String fake, int[] colors) {
        //TODO: may have to reconnect and join room
        currentState.set(state);
        currentState.category = category;
        currentState.title = title;
        currentState.fake = fake;
        currentState.drawing = new Drawing(colors, Collections.<Drawing.Line>emptyList());
        HashMap<String, Object> props = Props.of()
                .put("ready-" + state.userName, true)
                .build();
        currentState.get(props);
        client.updateRoomProperties(state.roomId, props, null);
    }

    private void doReady(State state) {
        //TODO: may have to reconnect and join room
        currentState.set(state);
        client.updateRoomProperties(state.roomId, Props.of()
                .put("ready-" + state.userName, true)
                .build(), null);
    }

    private void doStart() {
        client.startGame();
    }

    private void doReadyComplete() {
        subject.onNext(new Game(Event.READY, currentState.build()));
    }

    private void doStarted(String username) {
        currentState.turn = username;
        subject.onNext(new Game(Event.START, currentState.build()));
    }

    private static String generateRoomName() {
        StringBuilder code = new StringBuilder();
        String possible = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 6; i++) {
            code.append(possible.charAt((int) Math.floor(Math.random() * possible.length())));
        }
        return code.toString();
    }

    private static String pickDefaultQm(State state) {
        int index = state.players.indexOf(state.qm);
        return state.players.get((index + 1) % state.players.size());
    }

    private static String pickFakeArtist(State state) {
        if (state.qm == null) {
            throw new IllegalStateException("qm must be not be null: " + state);
        }
        List<String> players = new ArrayList<>(state.players);
        players.remove(players.indexOf(state.qm));
        Collections.shuffle(players);
        return players.get(0);
    }

    private static int[] generateColors(State state) {
        int size = state.players.size();
        int[] colors = new int[size];
        List<Integer> allColors = new ArrayList<>(Arrays.asList(
                Color.parseColor("#E91E63"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#3F51B5"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#FF5722")
        ));
        Collections.shuffle(allColors);
        for (int i = 0; i < size; i++) {
            colors[i] = allColors.get(i);
        }
        return colors;
    }

    private static boolean isReady(String player, HashMap<String, Object> props) {
        return props.containsKey("ready-" + player);
    }

    private static boolean isEveryoneReady(State state, HashMap<String, Object> props) {
        for (String player : state.players) {
            if (!isReady(player, props)) {
                return false;
            }
        }
        return true;
    }

    private static class Props {
        private final HashMap<String, Object> props = new HashMap<>();

        public static Props of() {
            return new Props();
        }

        Props put(String key, Object value) {
            props.put(key, value);
            return this;
        }

        HashMap<String, Object> build() {
            return props;
        }
    }
}
