package com.app.login.entity;

public class FriendRequest {
    private String recordID;   // 记录ID
    private String requestID;  // 发送请求的用户ID
    private String agreeID;    // 接收请求的用户ID
    private String status;      // 状态（waiting, agree, disagree）

    public FriendRequest(String recordID, String requestID, String agreeID, String status) {
        this.recordID = recordID;
        this.requestID = requestID;
        this.agreeID = agreeID;
        this.status = status;
    }

    public String getRecordID() {
        return recordID;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getAgreeID() {
        return agreeID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
