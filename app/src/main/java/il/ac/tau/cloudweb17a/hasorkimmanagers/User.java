package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class User implements java.io.Serializable {


    private static final String TAG = "USER_CLASS";
    private static User user;

    public String id;
    private String name;
    public boolean isApproved;
    public boolean isManager;


    private User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    private User(String id, String name, boolean isManager, boolean isApproved) {
        this.id = id;
        this.name = name;
        this.isManager = isManager;
        this.isApproved = isApproved;
    }

    public static User getUser() {
        if (user == null) {
            user = new User(FirebaseInstanceId.getInstance().getToken(), "שחר", true, false);
        }
        return user;
    }

    public String getId() {
        return id;
    }

    public boolean getIsManager() {
        return isManager;
    }

    public void setIsManager(boolean manager) {
        isManager = manager;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void checkCreds(final ReportListActivity.MyCallBackClass getReportList) {

        final ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() == 0) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference userRef = ref.child("users");
                    userRef.push().setValue(getUser());
                    getReportList.execute();
                }
                if (dataSnapshot.getChildrenCount() > 1) {
                    Log.e(TAG, "found two or more users with same id in the db");
                }

                for (DataSnapshot UserSS : dataSnapshot.getChildren()) {
                    User dbUser = UserSS.getValue(User.class);
                    //Log.d(TAG, "id:" +dbUser.id);
                    //Log.d(TAG, "is manager:" +dbUser.isManager);
                    if (dbUser.id != null && dbUser.id.equals(getUser().getId())) {
                        //Log.d(TAG, "setting is manager:" +dbUser.isManager);
                        getUser().setIsManager(dbUser.isManager);
                        getReportList.execute();
                        //break;
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
                .child("users").orderByChild("id").equalTo(this.id);

        mUserReference.addListenerForSingleValueEvent(postListener);
    }

    public String getName() {
        return name;
    }
}
