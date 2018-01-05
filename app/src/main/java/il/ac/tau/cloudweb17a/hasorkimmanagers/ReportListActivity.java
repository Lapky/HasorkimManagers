package il.ac.tau.cloudweb17a.hasorkimmanagers;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class ReportListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    MyCallBackClass showList = new MyCallBackClass() {
        @Override
        public void execute() {
            RecyclerView.Adapter mAdapter = new ReportAdapter(false, user.getIsManager(), getApplicationContext(), activity);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL));
            mRecyclerView.setAdapter(mAdapter);

            dText.setText(String.format("%s %s %s", String.valueOf(user.getIsManager()), distanceService.myLatLong.Lat, distanceService.myLatLong.Long));
        }
    };

    User user;
    Activity activity;
    TextView dText;
    private boolean mLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_list);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        activity = this;

        //setting up a user objext for the list
        user = getUser();

        dText = findViewById(R.id.dText);
        dText.setText(String.valueOf(user.getIsManager()));

        user.checkCreds( new MyCallBackClass() {
            @Override
            public void execute() {
                checkPermissions(showList);
            }
        });

    }

    private void checkPermissions(MyCallBackClass showList) {
        Context context = this.getApplicationContext();

        if ((ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)    ) {
            mLocationPermissionGranted = true;
            distanceService.getDeviceLocation(showList,mFusedLocationProviderClient, this);
        }
        else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        //mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    distanceService.getDeviceLocation(showList,mFusedLocationProviderClient, this);
                }
            }
        }


    }

    public interface MyCallBackClass {
        void execute();
    }


}
