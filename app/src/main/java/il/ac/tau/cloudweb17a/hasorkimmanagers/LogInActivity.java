package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;

public class LogInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;


    View mRootView;
    private static final String TAG = LogInActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mRootView = findViewById(R.id.root);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            auth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                @Override
                public void onSuccess(GetTokenResult result) {
                    String idToken = result.getToken();
                    //Do whatever
                    GoToReportList(idToken);
                }
            });

        } else {
            // not signed in
            Bundle params = new Bundle();
            params.putString(AuthUI.EXTRA_DEFAULT_COUNTRY_CODE, "il");

            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(
                                    Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).setParams(params).build()))
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);

        }
    }


    public void GoToReportList(String idToken) {
        getLocationPermission();
        FirebaseMessaging.getInstance().subscribeToTopic("new_report");
        Intent intent = new Intent(LogInActivity.this, ReportListActivity.class);
        intent.putExtra("token", idToken);
        startActivity(intent);
        finish();
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            );
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                GoToReportList(response.getIdpToken());
                finish();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    showSnackbar(R.string.sign_in_cancelled);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showSnackbar(R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showSnackbar(R.string.unknown_error);
                    return;
                }
            }

            Log.e(TAG, getString(R.string.unknown_sign_in_response));
        }
    }

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
