package fi.tut.cs.social.proximeety.classes;

import java.util.Date;

public class Message {

    public static final int MY_MESSAGE = 0;
    public static final int OTHER_MESSAGE = 1;

    private int mType;
    private String mMessage;
    private String mUsername;
    private Date mTimeSent;

    private Message() {}

    public int getType() {
        return mType;
    };

    public String getMessage() {
        return mMessage;
    };

    public String getUsername() {
        return mUsername;
    };

    public Date getTimeSent() { return mTimeSent; }

    public static class Builder {
        private final int mType;
        private String mUsername;
        private String mMessage;
        private Date mTimeSent;

        public Builder(int type) {
            mType = type;
        }

        public Builder username(String username) {
            mUsername = username;
            return this;
        }

        public Builder message(String message) {
            mMessage = message;
            return this;
        }

        public Builder timeSent(Date timeSent) {
            mTimeSent = timeSent;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mUsername = mUsername;
            message.mMessage = mMessage;
            message.mTimeSent = mTimeSent;
            return message;
        }
    }
}
