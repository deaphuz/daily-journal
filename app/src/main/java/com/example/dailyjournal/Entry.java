package com.example.dailyjournal;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

public class Entry implements Serializable {
    private UUID id;
    private String name;
    private String description;
    private String videoID;
    private Date date;
    private boolean done;

    public Entry() {
        id = UUID.randomUUID();
        name = "";
        description = "";
        videoID = "";
        date = new Date();
        done = false;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getVideoID() {
        return videoID;
    }
    public void setVideoID(String vid) {
        this.videoID = vid;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
