package com.app.login;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.app.login.entity.FriendRequest;

import java.util.List;

public class FriendRequestAdapter extends ArrayAdapter<FriendRequest> {
    private Context context;
    private List<FriendRequest> friendRequests; // 改为 FriendRequest 的列表
    private FriendRequestListener listener; // 添加监听器字段

    public FriendRequestAdapter(@NonNull Context context, List<FriendRequest> friendRequests, FriendRequestListener listener) {
        super(context, 0, friendRequests);
        this.context = context;
        this.friendRequests = friendRequests;
        this.listener = listener; // 初始化监听器
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.friend_request_item, parent, false);
        }

        // 获取当前好友请求
        FriendRequest friendRequest = friendRequests.get(position);

        TextView tvFriendName = convertView.findViewById(R.id.tv_friend_name);
        tvFriendName.setText(friendRequest.getRequestID()); // 假设你想显示 agreeID 或其他信息

        Button btnAccept = convertView.findViewById(R.id.btn_accept);
        btnAccept.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRequestHandled(friendRequest.getRequestID(), friendRequest.getAgreeID(), true); // 同意
            }
        });

        Button btnReject = convertView.findViewById(R.id.btn_reject);
        btnReject.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFriendRequestHandled(friendRequest.getRequestID(), friendRequest.getAgreeID(), false); // 拒绝
            }
        });

        return convertView;
    }

    // 定义一个接口来处理好友请求的同意和拒绝
    public interface FriendRequestListener {
        void onFriendRequestHandled(String requestID, String agreeID, boolean isAccepted);
    }
}
