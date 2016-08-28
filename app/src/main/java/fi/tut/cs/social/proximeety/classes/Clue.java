package fi.tut.cs.social.proximeety.classes;

import java.io.Serializable;
import java.util.Date;

public class Clue implements Serializable {

    private String id;
    private String ownerId;
    private String question;
    private String answer;
    private int orderNumber;
    private Date dateUpdated;
    private int likes;

    public Clue(String id, String ownerId, String question, String answer, int orderNumber, Date dateUpdated, int numberOfLikes) {
        this.id = id;
        this.ownerId = ownerId;
        this.question = question;
        this.answer = answer;
        this.orderNumber = orderNumber;
        this.dateUpdated = dateUpdated;
        this.likes = numberOfLikes;
    }

    public void setId(String id) { this.id = id; }
    public String getId() { return this.id; }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public String getOwnerId() { return this.ownerId; }

    public void setQuestion(String question) {
        this.question = question;
    }
    public String getQuestion() { return this.question; }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
    public String getAnswer() { return this.answer; }

    public void setOrderNumber(int orderNumber) {
        this.orderNumber = orderNumber;
    }
    public int getOrderNumber() { return this.orderNumber; }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
    public Date getDateUpdated() { return this.dateUpdated; }

    public int getLikes() { return this.likes; }
    public void setLikes(int numberOfLikes) { this.likes = numberOfLikes; }
    public void increaseLikes() { this.likes++; }
}
