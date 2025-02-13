package com.app.login.dao;
import com.app.login.entity.User;
import com.app.login.util.JDBCUtils;
import android.util.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * author: yan
 * date: 2022.02.17
 * **/
public class UserDao {

    private static final String TAG = "mysql-party-UserDao";

    /**
     * function: 登录
     * */
    public int login(String userAccount, String userPassword){
        // mysql简单的查询语句。这里是根据user表的userAccount字段来查询某条记录
        String sql = "select * from user where userAccount = ?";
        HashMap<String, Object> map = new HashMap<>();
        // 根据数据库名称，建立连接
        Connection connection = JDBCUtils.getConn();
        int msg = 0;
        try {
            if (connection != null){// connection不为null表示与数据库建立了连接
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null){
                    Log.e(TAG,"account：" + userAccount);
                    //根据账号进行查询
                    ps.setString(1, userAccount);
                    // 执行sql查询语句并返回结果集
                    ResultSet rs = ps.executeQuery();
                    int count = rs.getMetaData().getColumnCount();
                    //将查到的内容储存在map里
                    while (rs.next()){
                        // 注意：下标是从1开始的
                        for (int i = 1;i <= count;i++){
                            String field = rs.getMetaData().getColumnName(i);
                            map.put(field, rs.getString(field));
                        }
                    }
                    connection.close();
                    ps.close();

                    if (map.size()!=0){
                        StringBuilder s = new StringBuilder();
                        //寻找密码是否匹配
                        for (String key : map.keySet()){
                            if(key.equals("userPassword")){
                                if(userPassword.equals(map.get(key))){
                                    msg = 1;            //密码正确
                                }
                                else
                                    msg = 2;            //密码错误
                                break;
                            }
                        }
                    }else {
                        Log.e(TAG, "查询结果为空");
                        msg = 3;
                    }
                }else {
                    msg = 0;
                }
            }else {
                msg = 0;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "异常login：" + e.getMessage());
            msg = 0;
        }
        return msg;
    }


    /**
     * function: 注册
     * */
    public boolean register(User user){
        HashMap<String, Object> map = new HashMap<>();
        // 根据数据库名称，建立连接
        Connection connection = JDBCUtils.getConn();

        try {
            String sql = "insert into user(userAccount,userPassword) values (?,?)";
            if (connection != null){// connection不为null表示与数据库建立了连接
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null){

                    //将数据插入数据库
                    ps.setString(1,user.getUserAccount());
                    ps.setString(2,user.getUserPassword());
                    // 执行sql查询语句并返回结果集
                    int rs = ps.executeUpdate();
                    if(rs>0)
                        return true;
                    else
                        return false;
                }else {
                    return  false;
                }
            }else {
                return  false;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "异常register：" + e.getMessage());
            return false;
        }

    }

    /**
     * function: 根据账号进行查找该用户是否存在
     * */
    public User findUser(String userAccount) {

        // 根据数据库名称，建立连接
        Connection connection = JDBCUtils.getConn();
        User user = null;
        try {
            String sql = "select * from user where userAccount = ?";
            if (connection != null){// connection不为null表示与数据库建立了连接
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null) {
                    ps.setString(1, userAccount);
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        //注意：下标是从1开始
                        int id = rs.getInt(1);
                        String userAccount1 = rs.getString(2);
                        String userPassword = rs.getString(3);
                        user = new User(id, userAccount1, userPassword);
                        Log.d("findUser", "11111111: " + user);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "异常findUser：" + e.getMessage());
            return null;
        }
        return user;
    }
    /**
     * function: 根据 userAccount 查找用户并罗列出来
     */
    public List<User> findUsersByAccount(String userAccount) {
        // 建立数据库连接
        Connection connection = JDBCUtils.getConn();
        List<User> userList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM user WHERE userAccount LIKE ?";
            if (connection != null) { // 确保与数据库连接成功
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null) {
                    ps.setString(1, "%" + userAccount + "%"); // 使用模糊查询（如果需要完全匹配则使用直接传递userAccount）
                    ResultSet rs = ps.executeQuery();

                    while (rs.next()) {
                        // 注意：下标从1开始
                        int id = rs.getInt(1);
                        String userAccount1 = rs.getString(2);
                        String userPassword = rs.getString(3);
                        // 创建用户对象并添加到列表中
                        User user = new User(id,userAccount1, userPassword);
                        userList.add(user);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "异常findUsersByAccount：" + e.getMessage());
        } finally {
            // 确保关闭连接等资源
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return userList; // 返回查找到的用户列表
    }
    public boolean insertFriendRequest(String requestID, String agreeID, String status) {
        // 建立数据库连接
        Connection connection = JDBCUtils.getConn();

        try {
            // SQL插入语句
            String sql = "INSERT INTO friend (requestID, agreeID, status) VALUES (?, ?, ?)";
            if (connection != null) { // 确保连接成功
                PreparedStatement ps = connection.prepareStatement(sql);
                if (ps != null) {
                    // 设置参数
                    ps.setString(1, requestID);
                    ps.setString(2, agreeID);
                    ps.setString(3, status);

                    // 执行插入操作
                    int rowsInserted = ps.executeUpdate();
                    return rowsInserted > 0; // 如果插入的行数大于0，则表示成功
                } else {
                    return false; // PreparedStatement创建失败
                }
            } else {
                return false; // 连接失败
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "异常insertFriendRequest：" + e.getMessage());
            return false; // 发生异常时返回失败
        } finally {
            // 确保关闭连接
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * function: 获取好友列表
     * 返回与当前用户ID相匹配的好友
     */
    public List<String> getFriendIds(String currentUserId) {
        List<String> friendsList = new ArrayList<>();
        Connection connection = JDBCUtils.getConn();

        try {
            // 使用动态参数替换硬编码的 '11'
            String sql = "SELECT " +
                    "CASE " +
                    "    WHEN f.agreeID = ? THEN f.requestID " +
                    "    WHEN f.requestID = ? THEN f.agreeID " +
                    "END AS resultID " +
                    "FROM friend f " +
                    "WHERE (f.agreeID = ? OR f.requestID = ?) " +
                    "AND f.status = 'agree'";

            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(sql);
                ps.setString(1, currentUserId); // Set the current user's ID for agreeID
                ps.setString(2, currentUserId); // Set the current user's ID for requestID
                ps.setString(3, currentUserId); // Set the current user's ID for WHERE condition (agreeID)
                ps.setString(4, currentUserId); // Set the current user's ID for WHERE condition (requestID)

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String resultId = rs.getString("resultID");
                    // 如果需要可对 resultId 进行进一步处理
                    if (resultId != null) { // 确保 resultId 不为 null
                        friendsList.add(resultId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "异常getFriendIds：" + e.getMessage());
        } finally {
            // 确保关闭连接
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return friendsList; // 返回朋友 ID 的列表
    }



}
