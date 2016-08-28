package fi.tut.cs.social.proximeety.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User<id,username, deviceId> {

    private final String id;
    private final String username;
    private final String deviceId;
    private int mClues;

    public User(String id, String username, String deviceId, int clues) {
        this.id = id;
        this.username = username;
        this.deviceId = deviceId;
        this.mClues = clues;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getDeviceId() { return deviceId; }
    public int getClues() { return mClues; }

    @Override
    public int hashCode() { return id.hashCode() ^ username.hashCode() ^ deviceId.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        User pairo = (User) o;
        return (this.id.equals(pairo.getId()) &&
                this.username.equals(pairo.getUsername()) &&
                this.deviceId.equals(pairo.getDeviceId()));
    }
};