package com.app.login.groupEvent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.login.R;
import com.app.login.entity.User;

import java.util.List;



public class GroupEventFriendAdapter extends RecyclerView.Adapter<GroupEventFriendAdapter.FriendViewHolder> {

    private Context context;
    private List<User> friendsList;

    public GroupEventFriendAdapter(Context context, List<User> friendsList) {
        this.context = context;
        this.friendsList = friendsList;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_wobox, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        User friend = friendsList.get(position);
        holder.friendName.setText(friend.getUserAccount()); // 设置朋友的名称
    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    // ViewHolder 用于显示每个朋友的信息
    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView friendName;

        public FriendViewHolder(View itemView) {
            super(itemView);
            friendName = itemView.findViewById(R.id.friend_name);
        }
    }
}
