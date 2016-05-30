package me.tatarka.fakeartist.game.lobby;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.api.Room;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.Holder> {

    private String userName;
    private Room room;

    public PlayersAdapter(String userName) {
        super();
        this.userName = userName;
        setHasStableIds(true);
    }

    public void setRoom(Room room) {
        this.room = room;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(room.players.get(position));
    }

    @Override
    public int getItemCount() {
        return room == null ? 0 : room.players.size();
    }

    @Override
    public long getItemId(int position) {
        return room.players.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return room.players.get(position).equals(userName)
                ? R.layout.loppy_player_item : R.layout.lobby_item;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView name;

        public Holder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name);
        }

        void bind(String player) {
            name.setText(player);
        }
    }
}