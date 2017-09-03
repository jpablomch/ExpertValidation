package zika.edu.expertvalidation;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/*
 * Activity : Takes in username and saves it in preferences
 */
public class WelcomeActivity extends AppCompatActivity {

    private EditText mUserEdit;
    private Button mNextButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getSupportActionBar().setTitle("Welcome");

        prefs = getSharedPreferences("ExpertValidation", 0);
        if(!prefs.getString("username", "").equals("")){
            startValidation();
        }

        mUserEdit = (EditText)findViewById(R.id.username_edit);
        mNextButton = (Button)findViewById(R.id.next_button);

        mUserEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if(mUserEdit.getText().toString().length() > 0) {
                    mNextButton.setEnabled(true);
                } else {
                    mNextButton.setEnabled(false);
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefs.edit().putString("username", mUserEdit.getText().toString()).apply();
                startValidation();
            }
        });
    }

    private void startValidation(){
        Intent intent = new Intent(WelcomeActivity.this, ValidationActivity.class);
        startActivity(intent);
        finish();
    }

}
