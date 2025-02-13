package com.app.login;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import android.widget.ArrayAdapter;

import com.app.login.entity.User;

import java.util.ArrayList;  // 添加这个导入
import java.util.List;

public class FriendAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> friendsList;
    private SparseBooleanArray selectedItems;
    private OnFriendsSelectedListener listener;  // 回调接口

    public interface OnFriendsSelectedListener {
        void onFriendsSelected(List<User> selectedFriends);
    }

    public FriendAdapter(@NonNull Context context, List<User> friendsList, OnFriendsSelectedListener listener) {
        super(context, R.layout.list_item_friend, friendsList);
        this.context = context;
        this.friendsList = friendsList;
        this.selectedItems = new SparseBooleanArray();
        this.listener = listener;  // 传入回调接口
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_friend, parent, false);
        }

        User friend = getItem(position); // 获取当前的朋友对象
        TextView friendName = view.findViewById(R.id.friend_name);
        CheckBox checkBox = view.findViewById(R.id.friend_checkbox);

        friendName.setText(friend.getUserAccount()); // 设置朋友的名称
        checkBox.setChecked(selectedItems.get(position, false)); // 设置复选框的状态

        // 设置复选框状态变化的监听器
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedItems.put(position, true); // 标记为选中
            } else {
                selectedItems.put(position, false); // 标记为未选中
            }
            notifyFriendsSelectionChanged();  // 通知选中列表发生变化
        });

        return view;
    }

    // 获取所有选中的朋友列表
    private void notifyFriendsSelectionChanged() {
        List<User> selectedFriends = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            int position = selectedItems.keyAt(i);
            if (selectedItems.valueAt(i)) {
                selectedFriends.add(friendsList.get(position)); // 添加到选中的朋友列表
            }
        }
        if (listener != null) {
            listener.onFriendsSelected(selectedFriends);  // 调用回调方法
        }
    }
}
