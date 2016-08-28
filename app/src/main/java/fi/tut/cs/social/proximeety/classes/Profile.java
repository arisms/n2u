package fi.tut.cs.social.proximeety.classes;

import java.io.Serializable;

public class Profile implements Serializable{

    private String mId;
    private String mUsername;
    private String mPassword;
    private String mDeviceId;
    private int mClues;
    private boolean active;
    private String gender;
    private int age;
    private String community;
    private String email;

    public Profile(String id, String username, String password, String deviceId, int clues, boolean active) {
        this.mId = id;
        this.mUsername = username;
        this.mPassword = password;
        this.mDeviceId = deviceId;
        this.mClues = clues;
        this.active = active;
    }

    public void setId(String id) { mId = id; }
    public String getId() { return mId; }

    public void setUsername(String username) {
        mUsername = username;
    }
    public String getUsername() { return mUsername; }

    public void setPassword(String password) { mPassword = password; }
    public String getPassword() { return mPassword; }

    public void setDeviceId(String deviceId) { mDeviceId = deviceId; }
    public String getDeviceId() { return mDeviceId; }

    public void setActive(boolean active) { this.active = active; }
    public boolean getActive() { return this.active; }

    public void setClues(int clues) { this.mClues = clues; }
    public int getClues() { return this.mClues; }

    public void setGender(String gender) { this.gender = gender; }
    public String getGender() {return this.gender;}

    public void setAge(int age) {this.age = age;}
    public int getAge() {return this.age;}

    public void setCommunity(String community) { this.community = community; }
    public String getCommunity() { return this.community; }

    public void setEmail(String email) { this.email = email; }
    public String getEmail() { return this.email; }

}
