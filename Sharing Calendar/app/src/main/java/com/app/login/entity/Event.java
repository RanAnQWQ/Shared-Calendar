package com.app.login.entity;


public class Event {
    public int id;               // 事件ID
    public String title;         // 事件标题
    public String description;   // 事件描述
    public long startTimeMilli;          // 开始时间（毫秒）
    public long endTimeMilli;
    public boolean eventIsPersonal;
    //private String date;          // 事件日期
    public int userId;        // 用户ID（用于关联用户）

    // 构造函数
    public Event( int id,String title, String description, long startTimeMilli, long endTimeMilli,boolean eventIsPersonal, int userId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTimeMilli = startTimeMilli;
        this.endTimeMilli = endTimeMilli;
        this.eventIsPersonal = eventIsPersonal;
        //this.date = date;
        this.userId = userId;
    }
    // 仅两个参数的构造函数
    public Event(long startTimeMilli, long endTimeMilli) {
        this.title = ""; // 或者其他默认值
        this.description = ""; // 或者其他默认值
        this.startTimeMilli = startTimeMilli;
        this.endTimeMilli = endTimeMilli;
        this.eventIsPersonal = false; // 或者其他默认值
        this.userId = -1; // 或者其他默认值
    }
    // 无参构造函数
    public Event() {
        // 默认值可以根据需要设置
        this.title = "";
        this.description = "";
        this.startTimeMilli = System.currentTimeMillis();
        this.endTimeMilli = System.currentTimeMillis();
        this.eventIsPersonal = false;
        this.userId = 0;
    }


    // Getter 和 Setter 方法
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public long getStartTimeMilli() {
        return startTimeMilli;
    }

    public void setStartTimeMilli(long startTimeMilli) {
        this.startTimeMilli = startTimeMilli;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEndTimeMilli() {
        return endTimeMilli;
    }

    public void setEndTimeMilli(long endTimeMilli) {
        this.endTimeMilli = endTimeMilli;
    }

/*    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }*/

    public boolean isEventIsPersonal() {
        return eventIsPersonal;
    }

    public void setEventIsPersonal(boolean eventIsPersonal) {
        this.eventIsPersonal = eventIsPersonal;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
