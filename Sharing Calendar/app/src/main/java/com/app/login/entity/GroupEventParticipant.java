package com.app.login.entity;

public class GroupEventParticipant {
    private int id;
    private int groupEventId;
    private int userId;
    private String preferredTimeSlots;
    private String perferredDateTime;
    private String votedTimeSlot;
    private String votedTimeSlotList;
    private boolean isTimeSlotSubmitted;


    public String getVotedTimeSlotList() {
        return votedTimeSlotList;
    }

    public void setVotedTimeSlotList(String votedTimeSlotList) {
        this.votedTimeSlotList = votedTimeSlotList;
    }

    public String getPerferredDateTime() {
        return perferredDateTime;
    }

    public void setPerferredDateTime(String perferredDateTime) {
        this.perferredDateTime = perferredDateTime;
    }

    public boolean isTimeSlotSubmitted() {
        return isTimeSlotSubmitted;
    }

    public void setTimeSlotSubmitted(boolean timeSlotSubmitted) {
        isTimeSlotSubmitted = timeSlotSubmitted;
    }

    // 新增字段
    // Getter and Setter methods
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroupEventId() {
        return groupEventId;
    }

    public void setGroupEventId(int groupEventId) {
        this.groupEventId = groupEventId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPreferredTimeSlots() {
        return preferredTimeSlots;
    }

    public void setPreferredTimeSlots(String preferredTimeSlots) {
        this.preferredTimeSlots = preferredTimeSlots;
    }

    public String getVotedTimeSlot() {
        return votedTimeSlot;
    }

    public void setVotedTimeSlot(String votedTimeSlot) {
        this.votedTimeSlot = votedTimeSlot;
    }

    public boolean getIsTimeSlotSubmitted() {
        return isTimeSlotSubmitted;
    }

    public void setIsTimeSlotSubmitted(boolean isTimeSlotSubmitted) {
        this.isTimeSlotSubmitted = isTimeSlotSubmitted;
    }
}
