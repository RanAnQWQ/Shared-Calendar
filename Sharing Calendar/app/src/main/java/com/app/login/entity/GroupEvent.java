package com.app.login.entity;

public class GroupEvent {
    private int id;
    private String title;
    private String description;
    private int creatorId;
    private String tentativeStartDate;
    private String tentativeEndDate;
    private String finalStartDate;
    private String finalEndDate;
    private String status;

    // Getter and Setter methods
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

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getTentativeStartDate() {
        return tentativeStartDate;
    }

    public void setTentativeStartDate(String tentativeStartDate) {
        this.tentativeStartDate = tentativeStartDate;
    }

    public String getTentativeEndDate() {
        return tentativeEndDate;
    }

    public void setTentativeEndDate(String tentativeEndDate) {
        this.tentativeEndDate = tentativeEndDate;
    }

    public String getFinalStartDate() {
        return finalStartDate;
    }

    public void setFinalStartDate(String finalStartDate) {
        this.finalStartDate = finalStartDate;
    }

    public String getFinalEndDate() {
        return finalEndDate;
    }

    public void setFinalEndDate(String finalEndDate) {
        this.finalEndDate = finalEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
