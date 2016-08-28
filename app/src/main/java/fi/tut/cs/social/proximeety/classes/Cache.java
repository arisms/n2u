package fi.tut.cs.social.proximeety.classes;

import java.io.Serializable;

public class Cache implements Serializable {
    public Profile mProfile;
    public Connection mConnection1;
    public Connection mConnection2;

    public Cache (Profile mProfile, Connection mConnection1, Connection mConnection2) {
        this.mProfile = mProfile;
        this.mConnection1 = mConnection1;
        this.mConnection2 = mConnection2;
    }

}
