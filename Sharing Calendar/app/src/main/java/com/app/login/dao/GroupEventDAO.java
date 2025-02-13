package com.app.login.dao;

import com.app.login.entity.GroupEvent;
import com.app.login.entity.GroupEventParticipant;
import com.app.login.util.JDBCUtils;
import android.util.Log;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GroupEventDAO {

    private static final String TAG = "GroupEventDAO";

    // 获取集体事件
    public GroupEvent getGroupEventById(int id) {
        String query = "SELECT * FROM groupevent WHERE id = ?";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return null; // 返回空
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        GroupEvent event = new GroupEvent();
                        event.setId(rs.getInt("id"));
                        event.setTitle(rs.getString("title"));
                        event.setDescription(rs.getString("description"));
                        event.setCreatorId(rs.getInt("creator_id"));
                        event.setTentativeStartDate(rs.getString("start_date"));
                        event.setTentativeEndDate(rs.getString("end_date"));
                        event.setFinalStartDate(rs.getString("final_start_date"));
                        event.setFinalEndDate(rs.getString("final_end_date"));
                        event.setStatus(rs.getString("status"));
                        return event;
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "获取集体事件时出错: " + e.getMessage());
        }
        return null;
    }

    // 创建新的集体事件
    public void createGroupEvent(GroupEvent event) {
        String query = "INSERT INTO groupevent (creator_id, start_date, end_date, status, description, title) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }

            // Log the event data to be inserted
            Log.d(TAG, "Preparing to insert group event with details:");
            Log.d(TAG, "Creator ID: " + event.getCreatorId());
            Log.d(TAG, "Tentative Start Date: " + event.getTentativeStartDate());
            Log.d(TAG, "Tentative End Date: " + event.getTentativeEndDate());
            Log.d(TAG, "Status: " + event.getStatus());
            Log.d(TAG, "Description: " + event.getDescription());
            Log.d(TAG, "Title: " + event.getTitle());

            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, event.getCreatorId());
                stmt.setString(2, event.getTentativeStartDate());
                stmt.setString(3, event.getTentativeEndDate());
                stmt.setString(4, event.getStatus());
                stmt.setString(5, event.getDescription());
                stmt.setString(6, event.getTitle());  // 设置标题字段
                stmt.executeUpdate();

                // Log the SQL query being executed
                Log.d(TAG, "Executing query: " + stmt.toString());

                // 获取插入的事件ID
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    event.setId(generatedKeys.getInt(1));  // 假设 ID 是自动生成的
                }

                // Log successful insertion
                Log.d(TAG, "Group event inserted successfully with ID: " + event.getId());
            }
        } catch (SQLException e) {
            Log.e(TAG, "创建集体事件时出错: " + e.getMessage());
        }
    }

    // 获取所有未完成的集体事件
    public List<GroupEvent> getIncompleteGroupEvents() {
        String query = "SELECT * FROM groupevent WHERE status = 'PENDING' OR status = 'ONGOING'";
        List<GroupEvent> events = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return events; // 返回空列表
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        GroupEvent event = new GroupEvent();
                        event.setId(rs.getInt("id"));
                        event.setTitle(rs.getString("title"));
                        event.setDescription(rs.getString("description"));
                        event.setCreatorId(rs.getInt("creator_id"));
                        event.setTentativeStartDate(rs.getString("start_date"));
                        event.setTentativeEndDate(rs.getString("end_date"));
                        event.setFinalStartDate(rs.getString("final_start_date"));
                        event.setFinalEndDate(rs.getString("final_end_date"));
                        event.setStatus(rs.getString("status"));
                        events.add(event);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "获取未完成集体事件时出错: " + e.getMessage());
        }
        return events;
    }

    // 更新集体事件状态
    public void updateGroupEventStatus(int groupEventId, String status) {
        String query = "UPDATE groupevent SET status = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, status);
                stmt.setInt(2, groupEventId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            Log.e(TAG, "更新集体事件状态时出错: " + e.getMessage());
        }
    }

    // 获取由当前用户创建或参与的未完成群体事件
    public List<GroupEvent> getUserRelatedGroupEvents(int userId) {
        // 查询由当前用户创建或参与的事件，状态为 'PENDING' 或 'ONGOING'
        String query = "SELECT DISTINCT ge.* FROM GroupEvent ge " +
                "LEFT JOIN GroupEventParticipant gep ON ge.id = gep.group_event_id " +
                "WHERE ge.status IN ('PENDING', 'ONGOING') " + // 筛选状态为 'PENDING' 或 'ONGOING'
                "AND (ge.creator_id = ? OR gep.user_id = ?)";  // 筛选当前用户创建或参与的事件

        List<GroupEvent> events = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return events; // 返回空列表
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        GroupEvent event = new GroupEvent();
                        event.setId(rs.getInt("id"));
                        event.setTitle(rs.getString("title"));
                        event.setDescription(rs.getString("description"));
                        event.setCreatorId(rs.getInt("creator_id"));
                        event.setTentativeStartDate(rs.getString("start_date"));
                        event.setTentativeEndDate(rs.getString("end_date"));
                        event.setFinalStartDate(rs.getString("final_start_date"));
                        event.setFinalEndDate(rs.getString("final_end_date"));
                        event.setStatus(rs.getString("status"));
                        events.add(event);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "An error occurred when retrieving group events related to the current user. " + e.getMessage());
        }
        return events;
    }

    // 检查参与者并更新事件状态
    public void updateEventStatusIfAllPreferredTimeSelected(int eventId) {
        String checkQuery = "SELECT COUNT(*) AS remaining " +
                "FROM GroupEventParticipant " +
                "WHERE group_event_id = ? AND preferred_time IS NULL";
        String updateQuery = "UPDATE GroupEvent SET status = 'ONGOING' WHERE id = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }

            // 检查是否所有参与者都已选择 preferred_time
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, eventId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        int remaining = rs.getInt("remaining");
                        if (remaining == 0) {
                            // 所有参与者已选择 preferred_time，更新事件状态为 ONGOING
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, eventId);
                                updateStmt.executeUpdate();
                                Log.d(TAG, "Event ID " + eventId + " status updated to ONGOING.");
                            }
                        } else {
                            Log.d(TAG, "Event ID " + eventId + " still has " + remaining + " participants without preferred_time.");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "更新事件状态时出错: " + e.getMessage());
        }
    }

    public void updateEventStatus(int eventId, String newStatus) {
        String query = "UPDATE groupevent SET status = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, eventId);
                stmt.executeUpdate();
                Log.i(TAG, "事件状态已更新为 " + newStatus + "，事件 ID: " + eventId);
            }
        } catch (SQLException e) {
            Log.e(TAG, "更新事件状态时出错: " + e.getMessage());
        }
    }

    // GroupEventDTO.java



    // 获取投票最多的时间段
// 获取投票最多的时间段
    public String getMostVotedTimeSlot(List<GroupEventParticipant> participants) {
        Map<String, Integer> voteCountMap = new HashMap<>();

        // 统计每个voted_time_slot的投票次数
        for (GroupEventParticipant participant : participants) {
            String votedTimeSlot = participant.getVotedTimeSlot();
            if (votedTimeSlot != null && !votedTimeSlot.isEmpty()) {
                voteCountMap.put(votedTimeSlot, voteCountMap.getOrDefault(votedTimeSlot, 0) + 1);
            }
        }

        // 找出出现最多的时间段
        String mostVotedTimeSlot = null;
        int maxVotes = 0;
        for (Map.Entry<String, Integer> entry : voteCountMap.entrySet()) {
            if (entry.getValue() > maxVotes) {
                mostVotedTimeSlot = entry.getKey();
                maxVotes = entry.getValue();
            }
        }

        return mostVotedTimeSlot;
    }

    // 获取投票最多的时间段
    public String getMostVotedTimeSlotByList(List<GroupEventParticipant> participants) {
        Map<String, Integer> voteCountMap = new HashMap<>();

        // 统计每个 timeSlotList 的投票次数
        for (GroupEventParticipant participant : participants) {
            // 将 voted_time_slot_list 转为 List<String>
            String[] timeSlotArray = participant.getVotedTimeSlotList()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "") // 去掉引号
                    .split(", ");

            // 将数组转换为 List<String>
            List<String> timeSlotList = Arrays.asList(timeSlotArray);

            // 统计每个时间段的投票次数
            for (String timeSlot : timeSlotList) {
                if (timeSlot != null && !timeSlot.isEmpty()) {
                    voteCountMap.put(timeSlot, voteCountMap.getOrDefault(timeSlot, 0) + 1);
                }
            }
        }

        // 找出出现最多的时间段
        String mostVotedTimeSlot = null;
        int maxVotes = 0;
        for (Map.Entry<String, Integer> entry : voteCountMap.entrySet()) {
            if (entry.getValue() > maxVotes) {
                mostVotedTimeSlot = entry.getKey();
                maxVotes = entry.getValue();
            }
        }

        return mostVotedTimeSlot;
    }




}
