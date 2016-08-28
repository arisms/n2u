package fi.tut.cs.social.proximeety.classes;

import java.io.Serializable;

/**
 * Created by Aris on 09/12/15.
 */
public class Achievement implements Serializable {

    private String type;
    private String title;
    private String text;
    private int rank;
    private int currentValue;
    private int maxValue;

    public Achievement (String type, String title, String text, int rank, int maxValue) {
        this.type = type;
        this.title = title;
        this.text = text;
        this.rank = rank;
        currentValue = 0;
        this.maxValue = maxValue;
    }

    public void setType(String type) { this.type = type; }
    public String getType() { return this.type; }

    public void setTitle(String text) { this.title = text; }
    public String getTitle() { return this.title; }

    public void setText(String text) { this.text = text; }
    public String getText() { return this.text; }

    public void setRank(int rank) { this.rank = rank; }
    public int getRank() { return this.rank; }

    public void setCurrentValue(int currentValue) { this.currentValue = currentValue; }
    public int getCurrentValue() { return this.currentValue; }

    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }
    public int getMaxValue() { return this.maxValue; }

}
