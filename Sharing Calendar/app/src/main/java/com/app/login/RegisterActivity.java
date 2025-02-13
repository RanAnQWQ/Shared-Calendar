package com.app.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // 导入 Toolbar
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.app.login.dao.UserDao;
import com.app.login.entity.User;

public class RegisterActivity extends AppCompatActivity {
    private EditText userAccount;
    private EditText userPassword;
    private EditText et_again_password;
    private SharedPreferences mSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化 EditText
        userAccount = findViewById(R.id.et_userName);
        userPassword = findViewById(R.id.et_password);
        et_again_password = findViewById(R.id.et_again_password);

        // 设置 Toolbar
        Toolbar toolbar = findViewById(R.id.toorbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击返回箭头时，返回到 LoginActivity
                finish(); // 结束当前活动，返回上一个活动
            }
        });
    }

    public void register(View view) {
        String userAccount1 = userAccount.getText().toString();
        String userPassword1 = userPassword.getText().toString();
        String userPassword2 = et_again_password.getText().toString(); // 获取确认密码

        // 判断两次输入的密码是否相等
        if (!userPassword1.equals(userPassword2)) {
            Toast.makeText(getApplicationContext(), "The two passwords are different. Please re-enter them", Toast.LENGTH_LONG).show();
            return; // 退出方法，不继续注册
        }

        User user = new User();
        user.setUserAccount(userAccount1);
        user.setUserPassword(userPassword1);

        new Thread() {
            @Override
            public void run() {
                int msg = 0;
                UserDao userDao = new UserDao();
                User uu = userDao.findUser(user.getUserAccount());
                if (uu != null) {
                    msg = 1;
                } else {
                    boolean flag = userDao.register(user);
                    if (flag) {
                        msg = 2;
                    }
                }
                hand.sendEmptyMessage(msg);
            }
        }.start();
    }

    @SuppressLint("HandlerLeak")
    final Handler hand = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                Toast.makeText(getApplicationContext(), "Registration failure", Toast.LENGTH_LONG).show();
            } else if (msg.what == 1) {
                Toast.makeText(getApplicationContext(), "This account already exists, please change another account", Toast.LENGTH_LONG).show();
            } else if (msg.what == 2) {
                Toast.makeText(getApplicationContext(), "Registered successfully", Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra("a", "注册");
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        }
    };
}
