package com.app.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.app.login.dao.FriendRequestDAO;
import com.app.login.entity.FriendRequest;

import java.util.ArrayList;

public class FriendRequestsActivity extends AppCompatActivity {
    private ListView listView;
    private FriendRequestDAO friendRequestDAO;
    private ArrayList<FriendRequest> requestList;
    private FriendRequestAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        friendRequestDAO = new FriendRequestDAO();
        listView = findViewById(R.id.list_view);
        requestList = new ArrayList<>();
        loadFriendRequests();
    }

    private void loadFriendRequests() {
        new Thread(() -> {
            String userId = getCurrentUserId(); // 在后台线程获取用户ID
            if (userId != null) {
                requestList = friendRequestDAO.getFriendRequests(userId);
                runOnUiThread(() -> {
                    //Toast.makeText(FriendRequestsActivity.this, "获取到请求列表"+requestList.toString(), Toast.LENGTH_SHORT).show();
                    adapter = new FriendRequestAdapter(FriendRequestsActivity.this, requestList, FriendRequestsActivity.this::handleFriendRequest);
                    listView.setAdapter(adapter);
                });
            } else {
                runOnUiThread(() -> Toast.makeText(FriendRequestsActivity.this, "无法获取用户ID", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleFriendRequest(String requestID, String agreeID, boolean isAccepted) {
        new Thread(() -> {
            String status = isAccepted ? "agree" : "disagree";
            String message = isAccepted ? "you agree with this request" : "you reject this request";

            friendRequestDAO.updateFriendRequestStatus(requestID, agreeID, status);

            runOnUiThread(() -> Toast.makeText(FriendRequestsActivity.this, message, Toast.LENGTH_SHORT).show());

            loadFriendRequests();
        }).start();
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}
