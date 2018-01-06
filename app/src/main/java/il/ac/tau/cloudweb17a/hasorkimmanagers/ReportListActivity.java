package il.ac.tau.cloudweb17a.hasorkimmanagers;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class ReportListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    RadioGroup isOnlyOpenGroup;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean isOnlyOpen=false;
    private int numberOfReports=10;
    private int numberOfReportsToAdd=10;

    MyCallBackClass showList = new MyCallBackClass() {
        @Override
        public void execute() {
            MyCallBackClass setUIVisible=new MyCallBackClass() {
                @Override
                public void execute() {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.report_list_progress_bar).setVisibility(View.GONE);
                }
            };


            RecyclerView.Adapter mAdapter = new ReportAdapter(isOnlyOpen, user.getIsManager(), getApplicationContext(), activity, setUIVisible,numberOfReports);
            mRecyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL));
            mRecyclerView.setAdapter(mAdapter);

            if(getUser().getIsManager()){

                mRecyclerView.addOnScrollListener(
                    new RecyclerView.OnScrollListener() {
                        private boolean mIsLoading=false;
                        @Override
                        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                            if (mIsLoading)
                                return;
                            int visibleItemCount = mLayoutManager.getChildCount();
                            int totalItemCount = mLayoutManager.getItemCount();
                            int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                            if (pastVisibleItems + visibleItemCount >= totalItemCount) {
                                //Toast.makeText(ReportListActivity.this,"Lat",Toast.LENGTH_LONG).show();
                                mIsLoading=true;
                                numberOfReports = numberOfReports + numberOfReportsToAdd;
                                showList.execute();
                            }
                        }
                    }
                );
                isOnlyOpenGroup.setVisibility(View.VISIBLE);
                setUIVisible.execute();
            }
        }
    };

    User user;
    Activity activity;
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_list);
        isOnlyOpenGroup = findViewById(R.id.list_type_buttons_group);
        isOnlyOpenGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.all_reports_button:
                        isOnlyOpen=false;
                        break;
                    case R.id.open_reports_button:
                        isOnlyOpen=true;
                        break;
                }
                showList.execute();
            }
        });


        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        activity = this;

        //setting up a user object for the list
        user = getUser();

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
            if(user.getIsManager()){
                showList.execute();
            }else {
                distanceService.getDeviceLocation(showList, mFusedLocationProviderClient, this);
            }
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
                    distanceService.getDeviceLocation(showList,mFusedLocationProviderClient, this);
                }
            }
        }


    }

    public interface MyCallBackClass {
        void execute();
    }


}
