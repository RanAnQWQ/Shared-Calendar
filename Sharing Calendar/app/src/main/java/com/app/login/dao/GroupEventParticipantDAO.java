package com.app.login.dao;

import android.util.Log;
import com.app.login.entity.GroupEventParticipant;
import com.app.login.util.JDBCUtils;  // 假设 JDBCUtils 是你用来管理数据库连接的工具类

import java.sql.*;
import java.util.*;

public class GroupEventParticipantDAO {

    private static final String TAG = "GroupEventParticipantDAO";

    // 获取所有参与者
    public List<GroupEventParticipant> getParticipantsByEventId(int groupEventId) {
        String query = "SELECT * FROM groupeventparticipant WHERE group_event_id = ?";
        List<GroupEventParticipant> participants = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "The database connection failed and the query could not be executed");
                return participants; // 返回空列表
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, groupEventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        GroupEventParticipant participant = new GroupEventParticipant();
                        participant.setId(rs.getInt("id"));
                        participant.setGroupEventId(rs.getInt("group_event_id"));
                        participant.setUserId(rs.getInt("user_id"));
                        participant.setPreferredTimeSlots(rs.getString("preferred_time_slots"));
                        participant.setPerferredDateTime(rs.getString("perferred_date_time"));
                        participant.setVotedTimeSlot(rs.getString("voted_time_slot"));
                        participant.setVotedTimeSlotList(rs.getString("voted_time_slot_list"));
                        participant.setIsTimeSlotSubmitted(rs.getBoolean("is_time_slot_submitted")); // 获取是否提交了期望时间段
                        participants.add(participant);
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error obtaining participant: " + e.getMessage());
        }
        return participants;
    }

    // 创建新的参与者
    public void createParticipant(GroupEventParticipant participant) {
        String query = "INSERT INTO groupeventparticipant (group_event_id, user_id, preferred_time_slots, is_time_slot_submitted, voted_time_slot) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "The database connection failed and the query could not be executed");
                return;
            }

            // Log the participant data to be inserted
            Log.d(TAG, "Preparing to insert participant with details:");
            Log.d(TAG, "Group Event ID: " + participant.getGroupEventId());
            Log.d(TAG, "User ID: " + participant.getUserId());
            Log.d(TAG, "Preferred Time Slots: " + participant.getPreferredTimeSlots());
            Log.d(TAG, "Is Time Slot Submitted: " + participant.getIsTimeSlotSubmitted());
            Log.d(TAG, "Voted Time Slot: " + participant.getVotedTimeSlot());

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, participant.getGroupEventId());
                stmt.setInt(2, participant.getUserId());
                stmt.setString(3, participant.getPreferredTimeSlots());  // 存储 JSON 格式的期望时间段
                stmt.setBoolean(4, participant.getIsTimeSlotSubmitted());  // 提交标志
                stmt.setString(5, participant.getVotedTimeSlot());

                // Log the SQL query being executed
                Log.d(TAG, "Executing query: " + stmt.toString());

                stmt.executeUpdate();
                Log.d(TAG, "Participant inserted successfully.");
            }
        } catch (SQLException e) {
            Log.e(TAG, "创建参与者时出错: " + e.getMessage());
        }
    }


    // 更新期望时间段和提交状态
    // 更新期望时间段和提交状态
    public void updatePreferredTimeSlots(int userId, int eventId, String preferredTimeSlots, String perferredDateTime) {
        String query = "UPDATE groupeventparticipant SET preferred_time_slots = ?, perferred_date_time = ? , is_time_slot_submitted = TRUE " +
                "WHERE user_id = ? AND group_event_id = ?";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "The database connection failed and the query could not be executed");
                return;
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, preferredTimeSlots); // 设置 preferred_time_slots
                stmt.setString(2, perferredDateTime); // 设置 preferred_time_slots
                stmt.setInt(3, userId);               // 设置 user_id
                stmt.setInt(4, eventId);              // 设置 group_event_id
                stmt.executeUpdate();
                Log.i(TAG, "Preferred time slots updated successfully for user_id: " + userId + ", event_id: " + eventId);
            }
        } catch (SQLException e) {
            Log.e(TAG, "更新期望时间段时出错: " + e.getMessage());
        }
    }

    // 提交投票的时间段
    // 提交投票的时间段
    public void submitVoteForTimeSlot(int userId, String selectedTimeSlot, int eventId) {
        String query = "UPDATE groupeventparticipant SET voted_time_slot = ? WHERE user_id = ? AND group_event_id = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, selectedTimeSlot);  // 设置选定的时间段
                stmt.setInt(2, userId);               // 设置用户ID
                stmt.setInt(3, eventId);              // 设置事件ID
                int rowsUpdated = stmt.executeUpdate(); // 执行更新

                if (rowsUpdated > 0) {
                    Log.d(TAG, "The user voting period was successfully updated.");
                } else {
                    Log.e(TAG, "没有找到该用户在事件中的记录，无法更新投票时间段");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "提交投票时出错: " + e.getMessage());
        }
    }

    public void submitVoteForTimeSlotList(int userId, String selectedTimeSlotList, int eventId) {
        String query = "UPDATE groupeventparticipant SET voted_time_slot_list = ? WHERE user_id = ? AND group_event_id = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return;
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, selectedTimeSlotList);  // 设置选定的时间段
                stmt.setInt(2, userId);               // 设置用户ID
                stmt.setInt(3, eventId);              // 设置事件ID
                int rowsUpdated = stmt.executeUpdate(); // 执行更新

                if (rowsUpdated > 0) {
                    Log.d(TAG, "The user voting period was successfully updated.");
                } else {
                    Log.e(TAG, "没有找到该用户在事件中的记录，无法更新投票时间段");
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "提交投票时出错: " + e.getMessage());
        }
    }
    // 检查是否所有参与者都提交了期望时间
    public boolean canVote(int eventId) {
        String queryCheck = "SELECT COUNT(*) FROM groupeventparticipant WHERE group_event_id = ? AND is_time_slot_submitted = 0";
        String queryUpdate = "UPDATE groupevent SET status = 'ONGOING' WHERE id = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return false;
            }

            // 检查是否所有参与者都提交了期望时间段
            try (PreparedStatement stmtCheck = connection.prepareStatement(queryCheck)) {
                stmtCheck.setInt(1, eventId);
                try (ResultSet rs = stmtCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt(1) == 0) { // 没有未提交的参与者
                            // 更新 groupevent 表中的 status 为 'ONGOING'
                            try (PreparedStatement stmtUpdate = connection.prepareStatement(queryUpdate)) {
                                stmtUpdate.setInt(1, eventId);
                                int rowsUpdated = stmtUpdate.executeUpdate();
                                if (rowsUpdated > 0) {
                                    Log.i(TAG, "事件状态更新为 ONGOING，事件 ID: " + eventId);
                                } else {
                                    Log.e(TAG, "未能更新事件状态，事件 ID: " + eventId);
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "检查是否可以投票时出错: " + e.getMessage());
        }
        return false;
    }

    // 获取所有参与者的期望时间段
    public List<String> getPreferredTimeSlotsByEventId(int groupEventId) {
        String query = "SELECT preferred_time_slots FROM groupeventparticipant WHERE group_event_id = ?";
        List<String> preferredTimeSlotsList = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "The database connection failed and the query could not be executed");
                return preferredTimeSlotsList; // 返回空列表
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, groupEventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String preferredTimeSlots = rs.getString("preferred_time_slots");
                        if (preferredTimeSlots != null) {
                            preferredTimeSlotsList.add(preferredTimeSlots); // 将期望时间段加入列表
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "获取期望时间段时出错: " + e.getMessage());
        }
        return preferredTimeSlotsList;
    }


    // 获取所有参与者的期望时间段
    public List<String> getPreferredTimeSlotsListByEventId(int groupEventId) {
        String query = "SELECT perferred_date_time FROM groupeventparticipant WHERE group_event_id = ?";
        List<String> preferredTimeSlotsList = new ArrayList<>();
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "The database connection failed and the query could not be executed");
                return preferredTimeSlotsList; // 返回空列表
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, groupEventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String preferredTimeSlots = rs.getString("perferred_date_time");

                        if (preferredTimeSlots != null) {
                            // 转为数组
                            String[] timeSlotArray =preferredTimeSlots
                                    .replace("[", "")
                                    .replace("]", "")
                                    .replace("\"", "") // 去掉引号
                                    .split(", ");

                            // 将数组转换为 List<String>
                            List<String> timeSlotList = Arrays.asList(timeSlotArray);
                            preferredTimeSlotsList.addAll(timeSlotList); // 将期望时间段加入列表
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "获取期望时间段时出错: " + e.getMessage());
        }


        return preferredTimeSlotsList;
    }


    // 检查是否所有参与者已投票
    public boolean allParticipantsVoted(int eventId) {
        String query = "SELECT COUNT(*) FROM groupeventparticipant WHERE group_event_id = ? AND voted_time_slot_list IS NULL";
        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return false;
            }
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, eventId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;  // 如果未投票的参与者数为 0，返回 true
                    }
                }
            }
        } catch (SQLException e) {
            Log.e(TAG, "检查是否所有参与者已投票时出错: " + e.getMessage());
        }
        return false;
    }



}
