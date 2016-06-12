package me.tatarka.fakeartist.game.lobby;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.api.State;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.Holder> {

    private String userName;
    private State state;

    public PlayersAdapter(String userName) {
        super();
        this.userName = userName;
        setHasStableIds(true);
    }

    public void setState(State state) {
        this.state = state;
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.bind(state.players.get(position));
    }

    @Override
    public int getItemCount() {
        return state == null ? 0 : state.players.size();
    }

    @Override
    public long getItemId(int position) {
        return state.players.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return state.players.get(position).equals(userName)
                ? R.layout.loppy_player_item : R.layout.lobby_item;
    }

    public class Holder extends RecyclerView.ViewHolder {
        TextView name;
        TextView qm;

        public Holder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.username);
            qm = (TextView) itemView.findViewById(R.id.qm);
        }

        void bind(String player) {
            name.setText(player);
            qm.setVisibility(state.role(player) == State.Role.QM ? View.VISIBLE : View.GONE);
        }
    }
}
