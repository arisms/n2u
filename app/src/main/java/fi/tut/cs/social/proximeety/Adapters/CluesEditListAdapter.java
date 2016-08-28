package fi.tut.cs.social.proximeety.Adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;
import fi.tut.cs.social.proximeety.classes.Clue;

public class CluesEditListAdapter extends ArrayAdapter<Clue> {
    private final String TAG = "CluesEditListAdapter";    // Debugging
    MainActivity mainActivity;
    List<Clue> mClues;
    LayoutInflater mInflater;

    EditText answerET;
    Spinner questionSP;
    int mPosition;

    public CluesEditListAdapter(Context context, List<Clue> clues) {
        super(context, -1, clues);

        mainActivity = (MainActivity) context;
        mClues = clues;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.clues_edit_list_item, parent, false);

        // Spinners
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(mainActivity,
                R.array.questions_array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        questionSP = (Spinner) view.findViewById(R.id.questionSP);
        questionSP.setAdapter(adapter1);
        questionSP.setSelection(adapter1.getPosition(mClues.get(position).getQuestion()));

        questionSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                //Log.d(TAG, "onItemSelected: " + parent.getItemAtPosition(pos) + ", position: " + position);
                mClues.get(position).setQuestion(parent.getItemAtPosition(pos).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        answerET = (EditText) view.findViewById(R.id.answerET);
        answerET.setText(mClues.get(position).getAnswer());

        answerET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged list answer: " + s.toString());
                mPosition = position;
                mClues.get(mPosition).setAnswer(s.toString());
            }
        });

        return view;
    }
}
