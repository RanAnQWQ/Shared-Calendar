package com.app.login;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.app.login.dao.UserDao;
import com.app.login.entity.User;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private EditText et_userName;
    private EditText et_password;
    private Button btn_login;
    private MyHandler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_userName = findViewById(R.id.et_userName);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);

        handler = new MyHandler(this);
        btn_login.setOnClickListener(this::login);
    }

    public void reg(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    public void login(View view) {
        String account = et_userName.getText().toString();
        String password = et_password.getText().toString();

        new Thread() {
            @Override
            public void run() {
                UserDao userDao = new UserDao();
                int msg = userDao.login(account, password);
                handler.sendEmptyMessage(msg);
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    private static class MyHandler extends Handler {
        private final WeakReference<LoginActivity> activityReference;

        MyHandler(LoginActivity activity) {
            activityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            LoginActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            if (msg.what == 0) {
                Toast.makeText(activity.getApplicationContext(), "登录失败", Toast.LENGTH_LONG).show();
            } else if (msg.what == 1) {
                String account = activity.et_userName.getText().toString();
                activity.saveCurrentUserId(account); // 传递用户 ID
                activity.startActivity(new Intent(activity.getApplicationContext(), MainActivity.class));
            } else if (msg.what == 2) {
                Toast.makeText(activity.getApplicationContext(), "密码错误", Toast.LENGTH_LONG).show();
            } else if (msg.what == 3) {
                Toast.makeText(activity.getApplicationContext(), "账号不存在", Toast.LENGTH_LONG).show();
            }
        }
    }

/*    private void getUserIdByAccount(String account) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                UserDao userDao = new UserDao();
                User user = userDao.findUser(account);
                String userId = user != null ? String.valueOf(user.getId()) : null;

                // 切换到主线程处理结果
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // 处理返回的 userId（如果需要更新UI或处理结果）
                        if (userId != null) {
                            // 处理有效的 userId
                            Toast.makeText(getActivity(), "User ID: " + userId, Toast.LENGTH_SHORT).show();
                            // 可以在这里执行后续操作，比如更新UI或存储userId
                        } else {
                            // 处理找不到用户的情况
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }*/


    private void saveCurrentUserId(String userId) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("currentUserId", userId);
        editor.apply();
    }
}
