package com.app.login.dao;

import android.util.Log;
import com.app.login.entity.Event;
import com.app.login.util.JDBCUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private static final String TAG = "EventDAO";

    // 获取个人事件
    public List<Event> getPersonalEvents(int userId) {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE userID = ?"; // 使用正确的表名和字段

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return events; // 返回空列表
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, userId); // 设置参数为用户ID
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String title = rs.getString("title");
                        String description = rs.getString("description");
                        long startTimeMilli = rs.getTimestamp("startTime").getTime(); // 使用 getTimestamp 获取时间戳
                        long endTimeMilli = rs.getTimestamp("endTime").getTime();
                        boolean eventIsPersonal = rs.getBoolean("isPersonal");

                        events.add(new Event(id, title, description, startTimeMilli, endTimeMilli, eventIsPersonal, userId));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取个人事件时出错: " + e.getMessage());
        }
        return events;
    }

    // 根据时间范围获取事件
    public List<Event> getEventsByTimeRange(int userId, DateTime startTime, DateTime endTime) {
        List<Event> events = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        String sql = "SELECT * FROM event WHERE userID = ? AND startTime >= ? AND endTime <= ?";
        Log.e("EventDAO", "Querying events between "+dtf.print(startTime) +" and "+dtf.print(endTime)+" for user ID: "+userId);


        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return events; // 返回空列表
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, userId); // 设置参数为用户ID
                ps.setString(2, dtf.print(startTime)); // 设置开始时间
                ps.setString(3, dtf.print(endTime)); // 设置结束时间
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {

                        int id = rs.getInt("id");
                        Log.e("EventDAO", "id: 8888888888   "+id);
                        String title = rs.getString("title");
                        String description = rs.getString("description");
                        long startTimeMilli = rs.getTimestamp("startTime").getTime(); // 使用 getTimestamp 获取时间戳
                        long endTimeMilli = rs.getTimestamp("endTime").getTime();
                        boolean eventIsPersonal = rs.getBoolean("isPersonal");

                        events.add(new Event(id, title, description, startTimeMilli, endTimeMilli, eventIsPersonal, userId));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取时间范围内的事件时出错: " + e.getMessage());
        }
        Log.e("EventDAO", "size: 8888888888   "+events.size());
        return events;
    }

    // 添加事件
    public void addEvent(Event event) {
        String sql = "INSERT INTO event (title, description, startTime, endTime, isPersonal, userID) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = JDBCUtils.getConn();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, new java.sql.Timestamp(event.getStartTimeMilli())); // 转换为 Timestamp
            ps.setTimestamp(4, new java.sql.Timestamp(event.getEndTimeMilli()));
            ps.setBoolean(5, event.isEventIsPersonal());
            ps.setInt(6, event.getUserId()); // 将用户ID转换为int
            ps.executeUpdate();
        } catch (Exception e) {
            Log.e(TAG, "添加事件时出错: " + e.getMessage());
        }
    }

    // 修改事件
    public void updateEvent(Event event) {
        String sql = "UPDATE event SET title = ?, description = ?, startTime = ?, endTime = ?, isPersonal = ?, userID = ? WHERE id = ?";
        try (Connection connection = JDBCUtils.getConn();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, new java.sql.Timestamp(event.getStartTimeMilli())); // 转换为 Timestamp
            ps.setTimestamp(4, new java.sql.Timestamp(event.getEndTimeMilli()));
            ps.setBoolean(5, event.isEventIsPersonal());
            ps.setInt(6, event.getUserId()); // 将用户ID转换为int
            ps.setInt(7, event.getId()); // 设置事件ID
            ps.executeUpdate();
        } catch (Exception e) {
            Log.e(TAG, "修改事件时出错: " + e.getMessage());
        }
    }

    // 获取单个事件数据
    public Event getEventFromDatabase(long eventId) {
        Event event = null;
        String sql = "SELECT * FROM event WHERE id = ?"; // 使用正确的表名和字段

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return null; // 返回空
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setLong(1, eventId); // 设置参数为事件ID
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id");
                        String title = rs.getString("title");
                        String description = rs.getString("description");
                        long startTimeMilli = rs.getTimestamp("startTime").getTime(); // 使用 getTimestamp 获取时间戳
                        long endTimeMilli = rs.getTimestamp("endTime").getTime();
                        boolean eventIsPersonal = rs.getBoolean("isPersonal");
                        int userId = rs.getInt("userID"); // 获取创建事件的用户ID
                        event = new Event(id, title, description, startTimeMilli, endTimeMilli, eventIsPersonal, userId);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取事件时出错: " + e.getMessage());
        }
        return event;
    }

    // 删除事件
    public void deleteEventById(int eventId) {
        String sql = "DELETE FROM event WHERE id = ?";
        try (Connection connection = JDBCUtils.getConn();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, eventId); // 设置事件ID
            ps.executeUpdate();
        } catch (Exception e) {
            Log.e(TAG, "删除事件时出错: " + e.getMessage());
        }
    }

    public List<Event> getEventsForUsers(List<Integer> userIds) {
        List<Event> allEvents = new ArrayList<>();
        String sql = "SELECT * FROM event WHERE userID = ?";

        try (Connection connection = JDBCUtils.getConn()) {
            if (connection == null) {
                Log.e(TAG, "数据库连接失败，无法执行查询");
                return allEvents;
            }
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                for (int userId : userIds) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int id = rs.getInt("id");
                            String title = rs.getString("title");
                            String description = rs.getString("description");
                            long startTimeMilli = rs.getTimestamp("startTime").getTime();
                            long endTimeMilli = rs.getTimestamp("endTime").getTime();
                            boolean eventIsPersonal = rs.getBoolean("isPersonal");

                            Event event = new Event(id, title, description, startTimeMilli, endTimeMilli, eventIsPersonal, userId);
                            allEvents.add(event);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "获取事件时出错: " + e.getMessage());
        }
        return allEvents;
    }

}
