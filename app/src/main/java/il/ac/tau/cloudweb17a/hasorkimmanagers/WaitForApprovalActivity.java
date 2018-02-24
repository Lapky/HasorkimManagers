package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class WaitForApprovalActivity extends AppCompatActivity {

    TextView tv_enter_your_name;
    TextView tv_phone_number;
    EditText et_name;
    Button btn_submit;
    TextView tv_recieved_request;
    ProgressBar pb_loading;

    private static final String TAG = WaitForApprovalActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait_for_approval);

        String phoneNumber = "";

        tv_enter_your_name = findViewById(R.id.tv_enter_your_name);
        tv_phone_number = findViewById(R.id.tv_phone_number);
        et_name = findViewById(R.id.et_name);
        btn_submit = findViewById(R.id.btn_submit);
        tv_recieved_request = findViewById(R.id.tv_recieved_request);
        pb_loading = findViewById(R.id.pb_loading);

        Intent intent = getIntent();
        if (intent != null) {
            phoneNumber = intent.getStringExtra("phone_number");
        }

        final String finalPhoneNumber = phoneNumber;


        final ValueEventListener approved = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot UserSS : dataSnapshot.getChildren()) {
                    User dbUser = UserSS.getValue(User.class);
                    if (dbUser != null && dbUser.id != null && dbUser.id.equals(FirebaseInstanceId.getInstance().getToken())) { // user in db
                        if (dbUser.isApproved()) { // user is approved
                            User.setUser(dbUser);
                            setUserDetails(dbUser.isManager(), dbUser.getId());
                            Intent intent = new Intent(WaitForApprovalActivity.this, ReportListActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };


        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) { // no user
                    Query mUserReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").orderByChild("id").equalTo(FirebaseInstanceId.getInstance().getToken());

                    mUserReference.addValueEventListener(approved);

                    setVisibilityToAddName();
                    et_name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            if (charSequence.toString().trim().length() == 0) {
                                btn_submit.setEnabled(false);
                            } else {
                                btn_submit.setEnabled(true);
                            }
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {

                        }
                    });

                    tv_phone_number.setText(finalPhoneNumber);

                    btn_submit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            User.createNewUser(et_name.getText().toString(), finalPhoneNumber);
                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference userRef = ref.child("users");
                            userRef.push().setValue(User.getUser());
                            setVisibilityToWaiting();
                        }
                    });
                }

                for (DataSnapshot UserSS : dataSnapshot.getChildren()) {
                    User dbUser = UserSS.getValue(User.class);
                    if (dbUser != null && dbUser.id != null && dbUser.id.equals(FirebaseInstanceId.getInstance().getToken())) { // user in db
                        if (dbUser.isApproved()) { // user is approved
                            User.setUser(dbUser);
                            setUserDetails(dbUser.isManager(), dbUser.getId());
                            Intent intent = new Intent(WaitForApprovalActivity.this, ReportListActivity.class);
                            startActivity(intent);
                            finish();
                        } else { // user is not approved
                            setVisibilityToWaiting();
                            Query mUserReference = FirebaseDatabase.getInstance().getReference()
                                    .child("users").orderByChild("id").equalTo(FirebaseInstanceId.getInstance().getToken());

                            mUserReference.addValueEventListener(approved);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };

        Query mUserReference = FirebaseDatabase.getInstance().getReference()
                .child("users").orderByChild("id").equalTo(FirebaseInstanceId.getInstance().getToken());

        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    private void setUserDetails(boolean manager, String userId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.isManager), manager);
        editor.putString(getString(R.string.UserId), userId);
        editor.apply();
    }


    private void setVisibilityToWaiting() {
        tv_recieved_request.setVisibility(View.VISIBLE);
        tv_recieved_request.setText(getString(R.string.wait_for_approval));

        pb_loading.setVisibility(View.INVISIBLE);
        tv_enter_your_name.setVisibility(View.INVISIBLE);
        tv_phone_number.setVisibility(View.INVISIBLE);
        et_name.setVisibility(View.INVISIBLE);
        btn_submit.setVisibility(View.INVISIBLE);
    }

    private void setVisibilityToAddName() {
        tv_enter_your_name.setVisibility(View.VISIBLE);
        tv_phone_number.setVisibility(View.VISIBLE);
        et_name.setVisibility(View.VISIBLE);
        btn_submit.setVisibility(View.VISIBLE);

        tv_recieved_request.setVisibility(View.VISIBLE);
        tv_recieved_request.setText(getString(R.string.received_your_request));

        pb_loading.setVisibility(View.INVISIBLE);

    }
}
