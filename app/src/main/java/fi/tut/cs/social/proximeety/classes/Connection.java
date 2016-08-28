package fi.tut.cs.social.proximeety.classes;

import java.io.Serializable;
import java.util.Date;

public class Connection implements Serializable, Comparable {

    public String _id;
    public String user1Id;
    public String user2Id;
    public int timesMet;
    public int faceToFace;
    public Date lastFaceToFace;
    public Date lastMet;
    public Date lastUpdate;
    public boolean hasUpdate;
    public boolean blocked;

    public Connection(String _id, String user1Id, String user2Id, int timesMet, int faceToFace, Date lastFaceToFace, Date lastMet, Date lastUpdate, boolean blocked) {
        this._id = _id;
        this.user1Id = user1Id;
        this.user2Id = user2Id;
        this.timesMet = timesMet;
        this.faceToFace = faceToFace;
        this.lastFaceToFace = lastFaceToFace;
        this.lastMet = lastMet;
        this.lastUpdate = lastUpdate;
        this.hasUpdate = false;
        this.blocked = blocked;
    }

    @Override
    public int compareTo(Object another) {
        if(((Connection)another).timesMet > timesMet){
            return 1;
        }if(((Connection)another).timesMet == timesMet){
            return 0;
        }else{
            return -1;
        }
    }

}
