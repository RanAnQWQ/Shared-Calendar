package com.app.login.dao;

import android.util.Log;
import android.widget.Toast;

import com.app.login.entity.FriendRequest;
import com.app.login.util.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestDAO {
    private static final String TAG = "FriendRequestDAO";

    public ArrayList<FriendRequest> getFriendRequests(String currentUserId) {
        ArrayList<FriendRequest> requests = new ArrayList<>();
        // 修改SQL查询，只选择 agreeID 为当前用户的请求
        String sql = "SELECT * FROM friend WHERE status = 'waiting' AND agreeID = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return requests; // 返回空列表
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, currentUserId); // 设置参数为当前用户的ID
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String recordID = rs.getString("recordID");
                        String requestID = rs.getString("requestID");
                        String agreeID = rs.getString("agreeID");
                        String status = rs.getString("status");
                        requests.add(new FriendRequest(recordID, requestID, agreeID, status));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取好友请求时出错: " + e.getMessage());
        }
        return requests;
    }



    public void updateFriendRequestStatus(String requestID, String agreeID, String status) {
        String sql = "UPDATE friend SET status = ? WHERE requestID = ? AND agreeID = ?";
        try (Connection connection = JDBCUtils.getConn();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, requestID);
            ps.setString(3, agreeID);
            ps.executeUpdate();
        } catch (Exception e) {
            Log.e(TAG, "更新好友请求状态时出错: " + e.getMessage());
        }
    }
}
