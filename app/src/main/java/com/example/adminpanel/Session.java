package com.example.adminpanel;

public class Session {
    private String sessionId;
    private String title;
    private String description;
    private String duration;
    public Session(){}
    public Session(String title, String description, String duration) {
        this.title = title;
        this.description = description;
        this.duration = duration;
    }
    public Session(String sessionId,String title, String description, String duration) {
        this.sessionId=sessionId;
        this.title = title;
        this.description = description;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public String  getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
