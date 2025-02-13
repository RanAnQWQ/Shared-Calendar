package com.app.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.app.login.groupEvent.CreateGroupEventActivity;
import com.app.login.dao.UserDao;
import com.app.login.entity.User;

import java.util.ArrayList;
import java.util.List;

public class SelectFriendActivity extends AppCompatActivity implements FriendAdapter.OnFriendsSelectedListener {

    private ListView listView;
    private UserDao userDao;
    private FriendAdapter adapter;
    private List<User> friendsList;
    private List<User> selectedFriendsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_friend);

        listView = findViewById(R.id.list_view);
        userDao = new UserDao();
        selectedFriendsList = new ArrayList<>();
        friendsList = new ArrayList<>();

        loadFriends();

        Button doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            Intent intent = new Intent(SelectFriendActivity.this, CreateGroupEventActivity.class);
            // 确保传递的是 ArrayList<User> 类型
            intent.putExtra("selected_friends", new ArrayList<>(selectedFriendsList));
            startActivity(intent);
            finish();
        });

    }

    private void loadFriends() {
        new Thread(() -> {
            String currentUserId = getCurrentUserId(); // 获取当前用户ID
            List<String> friendNames = userDao.getFriendIds(currentUserId); // 获取朋友的名字列表

            // 创建一个列表来存储对应的User对象
            List<User> friendsList = new ArrayList<>();

            for (String friendName : friendNames) {
                User friend = userDao.findUser(friendName);
                if (friend != null) {
                    friendsList.add(friend);
                }
            }

            // 更新UI
            runOnUiThread(() -> {
                if (!friendsList.isEmpty()) {
                    adapter = new FriendAdapter(SelectFriendActivity.this, friendsList, SelectFriendActivity.this);  // 将回调传递给适配器
                    listView.setAdapter(adapter);
                } else {
                    Toast.makeText(SelectFriendActivity.this, "No friends found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    // 当朋友选择发生变化时调用
    @Override
    public void onFriendsSelected(List<User> selectedFriends) {
        selectedFriendsList = selectedFriends; // 更新 selectedFriendsList
    }

    private String getCurrentUserId() {
        // 从 SharedPreferences 获取当前用户ID
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        return sharedPreferences.getString("currentUserId", null);
    }
}

