package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private MainActivity mainActivity;

    public MessageAdapter(MainActivity mainActivity, List<Message> messages) {
        this.mainActivity = mainActivity;
        mMessages = messages;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout = -1;

        if(viewType == Message.MY_MESSAGE) {
            layout = R.layout.my_chat_message;
        }
        else {
            layout = R.layout.other_chat_message;
        }

        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        viewHolder.setMessage(message.getMessage());

        viewHolder.messageTimestampTV.setText(mainActivity.datesDifferenceChat(mMessages.get(position).getTimeSent()));

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mMessageView;
        private TextView messageTimestampTV;

        public ViewHolder(View itemView) {
            super(itemView);

            mMessageView = (TextView) itemView.findViewById(R.id.message);
            mMessageView.setTypeface(mainActivity.futura);
            messageTimestampTV = (TextView) itemView.findViewById(R.id.messageTimestampTV);
            messageTimestampTV.setTypeface(mainActivity.futura);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
        }
    }

}