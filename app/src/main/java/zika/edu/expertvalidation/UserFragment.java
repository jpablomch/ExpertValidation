package zika.edu.expertvalidation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserFragment extends Fragment {

    private AppCompatActivity parent;
    private EditText mUserEdit;
    private Button mSaveButton;
    private SharedPreferences prefs;

    public UserFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        parent = (AppCompatActivity)context;
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceStace){
        prefs = parent.getSharedPreferences("ExpertValidation", 0);
        super.onCreate(savedInstanceStace);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        parent.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
        parent.getSupportActionBar().setTitle("User Settings");

        mUserEdit = (EditText)view.findViewById(R.id.username_edit);
        mSaveButton = (Button)view.findViewById(R.id.save_button);

        mUserEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(mUserEdit.getText().toString().length() > 0) {
                    mSaveButton.setEnabled(true);
                } else {
                    mSaveButton.setEnabled(false);
                }
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("username", mUserEdit.getText().toString()).apply();
                Toast.makeText(parent, "Username changed", Toast.LENGTH_SHORT).show();
                parent.getSupportActionBar().setSubtitle(mUserEdit.getText().toString());
            }
        });

        return view;
    }


}
