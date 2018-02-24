package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.iid.FirebaseInstanceId;

public class User implements java.io.Serializable {


    private static final String TAG = "USER_CLASS";
    private static User user;

    public String id;
    private String name;
    private String phoneNumber;
    private boolean approved;
    private boolean manager;


    public void setName(String name) {
        this.name = name;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public boolean isManager() {
        return manager;
    }

    public void setManager(boolean manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    private User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    private User(String id, String name, String phoneNumber, boolean isManager, boolean isApproved) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.manager = isManager;
        this.approved = isApproved;
    }

    public static void createNewUser(String name, String phoneNumber) {
        user = new User(FirebaseInstanceId.getInstance().getToken(), name, phoneNumber, false, false);
        addUserToDb();
    }

    public static void setUser(User user) {
        User.user = user;
    }


    public static User getUser() {
        return user;
    }

    private static void addUserToDb() {
    }
}
