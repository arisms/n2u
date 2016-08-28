package fi.tut.cs.social.proximeety.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.BufferUnderflowException;

import fi.tut.cs.social.proximeety.MainActivity;
import fi.tut.cs.social.proximeety.R;

public class LogInRegisterFragment extends Fragment {
    public final String TAG = "LogInRegisterFragment";

    Button logInButton, registerButton;
    EditText usernameET, passwordET;
    LogInRegisterFragmentListener mCallback;
    MainActivity mainActivity;

    AnimationDrawable testAnimation;

    public LogInRegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log_in_register, container, false);
        mainActivity = (MainActivity) getActivity();

        Log.d(TAG, "onCreateView()");

        // Edit Texts
        usernameET = (EditText) view.findViewById(R.id.usernameET);
        usernameET.setTypeface(mainActivity.futura, 0);
        passwordET = (EditText) view.findViewById(R.id.passwordET);
        passwordET.setTypeface(mainActivity.futura, 0);

        // Hide the soft keyboard
        InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // Buttons
        logInButton = (Button) view.findViewById(R.id.loginBN);
        logInButton.setTypeface(mainActivity.futura, 0);
        logInButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (passwordET.getText().length() == 0)
                    wrongCredentials("password", "empty");
                else if (usernameET.getText().length() == 0)
                    wrongCredentials("username", "empty");
                else
                    mCallback.onLogInButton(usernameET.getText().toString(), passwordET.getText().toString());

            }
        });
        registerButton = (Button) view.findViewById(R.id.registerBN);
        registerButton.setTypeface(mainActivity.futura, 0);
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(passwordET.getText().length() == 0)
                    wrongCredentials("password", "empty");
                else if(usernameET.getText().length() == 0)
                    wrongCredentials("username", "empty");
                else
                    mCallback.onRegisterButton(usernameET.getText().toString(), passwordET.getText().toString());
            }
        });
        logInButton.requestFocus();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (LogInRegisterFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LogInRegisterFragmentListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final View mView = view;

        view.post(new Runnable() {
            @Override
            public void run() {
                // Hide the soft keyboard
                InputMethodManager imm = (InputMethodManager)mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
            }
        });
    }


    public void wrongCredentials(String field, String error) {
        if(field.equals("username")) {
            if(error.equals("empty"))
                Toast.makeText(getActivity().getApplicationContext(), "Username must not be empty!", Toast.LENGTH_SHORT).show();
            else if(error.equals("exists"))
                Toast.makeText(getActivity().getApplicationContext(), "Username is taken.", Toast.LENGTH_SHORT).show();

            usernameET.requestFocus();
        }
        else if(field.equals("password")) {
            if(error.equals("empty"))
                Toast.makeText(getActivity().getApplicationContext(), "Password must not be empty!", Toast.LENGTH_SHORT).show();

            passwordET.requestFocus();
        }
        else if(error.equals("not_found"))
            Toast.makeText(getActivity().getApplicationContext(), "Incorrect username or password." + '\n' + "Please try again.", Toast.LENGTH_SHORT).show();
    }

    public interface LogInRegisterFragmentListener {
        void onLogInButton(String username, String password);
        void onRegisterButton(String username, String password);
    }
}
