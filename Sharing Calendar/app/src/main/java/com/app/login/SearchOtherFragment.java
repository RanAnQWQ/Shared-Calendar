package com.app.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.login.dao.UserDao;
import com.app.login.entity.User;

import java.util.ArrayList;
import java.util.List;

public class SearchOtherFragment extends Fragment {
    private EditText et_search;
    private Button btn_search;
    private ListView listView;
    private ArrayAdapter<User> userAdapter;
    private List<User> userList; // 用户列表

    public SearchOtherFragment() {
        // Required empty public constructor
    }
    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) requireActivity()).hideToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) requireActivity()).hideToolbar();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_other, container, false);

        et_search = view.findViewById(R.id.et_search);
        btn_search = view.findViewById(R.id.btn_search);
        Button btnFriendRequestReceived = view.findViewById(R.id.btn_friend_request_received); // 获取新按钮

        listView = view.findViewById(R.id.list_view);

        // 初始化用户列表
        userList = new ArrayList<>();

        // 创建适配器
        userAdapter = new ArrayAdapter<User>(getContext(), R.layout.user_list, userList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.user_list, parent, false);
                }
                User user = getItem(position);
                TextView textView = convertView.findViewById(R.id.item_text);
                if (user != null) {
                    textView.setText("  id:" + user.getId() + "     " + user.getUserAccount());
                }

                Button requestButton = convertView.findViewById(R.id.item_request_button);
                requestButton.setOnClickListener(v -> sendFriendRequest(String.valueOf(user.getUserAccount())));

                return convertView;
            }
        };
        listView.setAdapter(userAdapter);

        btn_search.setOnClickListener(v -> searchUsers());

        // 设置 Friend Request Received 按钮的点击事件
        btnFriendRequestReceived.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FriendRequestsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void searchUsers() {
        String userAccount = et_search.getText().toString().trim();

        new Thread(() -> {
            UserDao userDao = new UserDao();
            List<User> resultList = userDao.findUsersByAccount(userAccount);

            getActivity().runOnUiThread(() -> {
                if (resultList != null && !resultList.isEmpty()) {
                    userList.clear();
                    userList.addAll(resultList);
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "find no user, please check the name again", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void sendFriendRequest(String agreeID) {
        String requestID = getCurrentUserId(); // 获取当前用户的ID
        String status = "waiting"; // 默认状态
        //Toast.makeText(getContext(), "收到id： "+requestID, Toast.LENGTH_SHORT).show();
        // 使用AsyncTask异步插入好友请求
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                UserDao userDao = new UserDao();
                return userDao.insertFriendRequest(requestID, agreeID, status);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(getContext(), "request successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "fail to request", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private String getCurrentUserId() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("currentUserId", null);
        //Toast.makeText(getContext(), "收到id： "+userId, Toast.LENGTH_SHORT).show();
        //Log.d("SearchOtherFragment", "获取的用户ID: " + userId); // 打印日志
        return userId;
    }

}
