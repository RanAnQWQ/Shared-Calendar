package com.app.login.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private int id;
    private String userAccount;
    private String userPassword;

    // 默认构造函数
    public User() {
    }

    // 带参数的构造函数
    public User(int id, String userAccount, String userPassword) {
        this.id = id;
        this.userAccount = userAccount;
        this.userPassword = userPassword;
    }

    // 从 Parcel 中恢复 User 对象
    protected User(Parcel in) {
        id = in.readInt();
        userAccount = in.readString();
        userPassword = in.readString(); // 读取 userPassword
    }

    // 用于将 User 对象写入 Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id); // 写入 id
        dest.writeString(userAccount); // 写入 userAccount
        dest.writeString(userPassword); // 写入 userPassword
    }

    // 用于创建 User 对象
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in); // 从 Parcel 中创建 User 对象
        }

        @Override
        public User[] newArray(int size) {
            return new User[size]; // 创建 User 数组
        }
    };

    // 返回用户信息字符串
    @Override
    public String toString() {
        return "  id: " + id + "            name:" + userAccount; // 返回用户名字
    }

    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    // 实现 Parcelable 接口的描述符方法
    @Override
    public int describeContents() {
        return 0;
    }
}
