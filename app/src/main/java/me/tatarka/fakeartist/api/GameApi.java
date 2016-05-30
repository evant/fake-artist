package me.tatarka.fakeartist.api;

import com.shephertz.app42.gaming.multiplayer.client.WarpClient;
import com.shephertz.app42.gaming.multiplayer.client.command.WarpResponseResultCode;
import com.shephertz.app42.gaming.multiplayer.client.events.ConnectEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.LiveRoomInfoEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.MatchedRoomsEvent;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomData;
import com.shephertz.app42.gaming.multiplayer.client.events.RoomEvent;
import com.shephertz.app42.gaming.multiplayer.client.listener.ConnectionRequestListener;
import com.shephertz.app42.gaming.multiplayer.client.listener.NotifyListener;
import com.shephertz.app42.gaming.multiplayer.client.listener.RoomRequestListener;
import com.shephertz.app42.gaming.multiplayer.client.listener.ZoneRequestListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import me.tatarka.fakeartist.api.listeners.ConnectonRequestAdapter;
import me.tatarka.fakeartist.api.listeners.NotifyAdapter;
import me.tatarka.fakeartist.api.listeners.RoomRequestAdapter;
import me.tatarka.fakeartist.api.listeners.ZoneRequestAdapter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class GameApi {

    private static final String TAG = "GameApi";
    private static final int MAX_PLAYERS = 16;
    private static final int MAX_TURN_TIME = 3600; // seconds

    private WarpClient client;

    @Inject
    public GameApi(WarpClient client) {
        this.client = client;
    }

    public rx.Observable<Room> connectAndCreateRoom(final String userName) {
        return connect(userName).flatMap(new Func1<ConnectEvent, Observable<Room>>() {
            @Override
            public Observable<Room> call(ConnectEvent connectEvent) {
                if (connectEvent.getResult() == WarpResponseResultCode.SUCCESS) {
                    return createRoom(userName);
                } else {
                    return Observable.error(new ConnectException(connectEvent));
                }
            }
        });
    }

    public rx.Observable<Room> connectAndJoinRoom(final String roomName, final String userName) {
        return connect(userName).flatMap(new Func1<ConnectEvent, Observable<Room>>() {
            @Override
            public Observable<Room> call(ConnectEvent event) {
                if (event.getResult() == WarpResponseResultCode.SUCCESS) {
                    return joinRoom(roomName, userName);
                } else {
                    return Observable.error(new ConnectException(event));
                }
            }
        });
    }

    /**
     * Create a room with a random 6-char name and joins it. You will get room updates each time a
     * player joins or leaves.
     */
    public rx.Observable<Room> createRoom(final String userName) {
        return rx.Observable.create(new Observable.OnSubscribe<Room>() {
            private String roomId;
            private String roomName;
            private ArrayList<String> players;

            @Override
            public void call(final Subscriber<? super Room> subscriber) {
                final NotifyListener notifyListener = new NotifyAdapter() {
                    @Override
                    public void onRoomDestroyed(RoomData roomData) {
                        if (roomData.getName().equals(roomName)) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onCompleted();
                            }
                        }
                    }

                    @Override
                    public void onUserJoinedRoom(RoomData roomData, String userName) {
                        if (roomData.getName().equals(roomName)) {
                            int index = Collections.binarySearch(players, userName);
                            if (index < 0) {
                                players.add(-(index + 1), userName);
                            }
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(new Room(roomName, players));
                            }
                        }
                    }

                    @Override
                    public void onUserLeftRoom(RoomData roomData, String userName) {
                        if (roomData.getName().equals(roomName)) {
                            players.remove(userName);
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(new Room(roomName, players));
                            }
                        }
                    }
                };
                final ZoneRequestListener zoneRequestListener = new ZoneRequestAdapter() {
                    @Override
                    public void onCreateRoomDone(RoomEvent roomEvent) {
                        if (roomEvent.getResult() == WarpResponseResultCode.SUCCESS) {
                            if (!subscriber.isUnsubscribed()) {
                                roomId = roomEvent.getData().getId();
                                client.joinAndSubscribeRoom(roomId);
                            }
                        } else {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(new ApiException(roomEvent.getResult()));
                            }
                        }
                    }
                };
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        client.removeNotificationListener(notifyListener);
                        client.removeZoneRequestListener(zoneRequestListener);
                        if (roomId != null) {
                            client.leaveAndUnsubscribeRoom(roomId);
                        }
                    }
                }));

                if (!subscriber.isUnsubscribed()) {
                    client.addZoneRequestListener(zoneRequestListener);
                    client.addNotificationListener(notifyListener);
                    roomName = generateRoomName();
                    players = new ArrayList<>(1);
                    players.add(userName);

                    subscriber.onNext(new Room(roomName, players));
                }

                if (!subscriber.isUnsubscribed()) {
                    HashMap<String, Object> props = new HashMap<>();
                    props.put("name", roomName);
                    client.createTurnRoom(roomName, userName, MAX_PLAYERS, props, MAX_TURN_TIME);
                }
            }
        });
    }

    public rx.Observable<Room> joinRoom(final String roomName, final String userName) {
        return rx.Observable.create(new Observable.OnSubscribe<Room>() {
            String roomId;
            List<String> players;

            @Override
            public void call(final Subscriber<? super Room> subscriber) {
                final NotifyListener notifyListener = new NotifyAdapter() {
                    @Override
                    public void onRoomDestroyed(RoomData roomData) {
                        if (roomData.getName().equals(roomName)) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onCompleted();
                            }
                        }
                    }

                    @Override
                    public void onUserJoinedRoom(RoomData roomData, String userName) {
                        if (roomData.getName().equals(roomName)) {
                            int index = Collections.binarySearch(players, userName);
                            if (index < 0) {
                                players.add(-(index + 1), userName);
                            }
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(new Room(roomName, players));
                            }
                        }
                    }

                    @Override
                    public void onUserLeftRoom(RoomData roomData, String userName) {
                        if (roomData.getName().equals(roomName)) {
                            players.remove(userName);
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(new Room(roomName, players));
                            }
                        }
                    }
                };
                final ZoneRequestListener zoneRequestListener = new ZoneRequestAdapter() {
                    @Override
                    public void onGetMatchedRoomsDone(MatchedRoomsEvent matchedRoomsEvent) {
                        if (matchedRoomsEvent.getResult() == WarpResponseResultCode.SUCCESS) {
                            RoomData[] datas = matchedRoomsEvent.getRoomsData();
                            if (datas.length == 0) {
                                if (!subscriber.isUnsubscribed()) {
                                    subscriber.onError(new NoRoomException(roomName));
                                }
                            } else {
                                RoomData data = datas[0];
                                roomId = data.getId();
                                if (!subscriber.isUnsubscribed()) {
                                    client.getLiveRoomInfo(roomId);
                                }
                            }
                        } else {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(new ApiException(matchedRoomsEvent.getResult()));
                            }
                        }
                    }
                };
                final RoomRequestListener roomRequestListener = new RoomRequestAdapter() {
                    @Override
                    public void onGetLiveRoomInfoDone(LiveRoomInfoEvent liveRoomInfoEvent) {
                        if (liveRoomInfoEvent.getResult() == WarpResponseResultCode.SUCCESS) {
                            String[] users = liveRoomInfoEvent.getJoinedUsers();
                            for (String user : users) {
                                int index = Collections.binarySearch(players, user);
                                if (index < 0) {
                                    players.add(-(index + 1), user);
                                }
                            }
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onNext(new Room(roomName, players));
                            }
                            if (!subscriber.isUnsubscribed()) {
                                client.joinAndSubscribeRoom(roomId);
                            }
                        } else {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(new ApiException(liveRoomInfoEvent.getResult()));
                            }
                        }
                    }
                };

                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        client.removeNotificationListener(notifyListener);
                        client.removeZoneRequestListener(zoneRequestListener);
                        client.removeRoomRequestListener(roomRequestListener);
                        if (roomId != null) {
                            client.leaveAndUnsubscribeRoom(roomId);
                        }
                    }
                }));
                if (!subscriber.isUnsubscribed()) {
                    client.addNotificationListener(notifyListener);
                    client.addZoneRequestListener(zoneRequestListener);
                    client.addRoomRequestListener(roomRequestListener);

                    players = new ArrayList<>(1);
                    players.add(userName);

                    subscriber.onNext(new Room(roomName, players));
                }

                if (!subscriber.isUnsubscribed()) {
                    HashMap<String, Object> props = new HashMap<>();
                    props.put("name", roomName);
                    client.getRoomInRangeWithProperties(1, MAX_PLAYERS, props);
                }
            }
        });
    }

    /**
     * Connects the user with the given name. You should check the returned connect event to
     * determine if the connection is successful.
     */
    public rx.Observable<ConnectEvent> connect(final String userName) {
        return rx.Observable.create(new Observable.OnSubscribe<ConnectEvent>() {
            @Override
            public void call(final Subscriber<? super ConnectEvent> subscriber) {
                final ConnectionRequestListener listener = new ConnectonRequestAdapter() {
                    @Override
                    public void onConnectDone(ConnectEvent connectEvent) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(connectEvent);
                        }
                    }

                    @Override
                    public void onDisconnectDone(ConnectEvent connectEvent) {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onCompleted();
                        }
                    }
                };
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        client.removeConnectionRequestListener(listener);
                        client.disconnect();
                    }
                }));
                client.addConnectionRequestListener(listener);
                client.connectWithUserName(userName);
            }
        });
    }

    private static String generateRoomName() {
        StringBuilder code = new StringBuilder();
        String possible = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 6; i++) {
            code.append(possible.charAt((int) Math.floor(Math.random() * possible.length())));
        }
        return code.toString();
    }
}
