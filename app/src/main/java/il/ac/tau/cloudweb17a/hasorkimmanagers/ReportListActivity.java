package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.Report.getLastReportStartTime;
import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class ReportListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ReportListLog";
    private LinearLayout reportListLayout;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    RadioGroup isOnlyOpenGroup;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean isOnlyOpen = false;
    private int numberOfReports = 10;
    private int numberOfReportsToAdd = 10;
    ReportAdapter mAdapter;
    NavigationView navigationView;

    MyCallBackClass showList = new MyCallBackClass() {
        @Override
        public void execute() {
            MyCallBackClass setUIVisible = new MyCallBackClass() {
                @Override
                public void execute() {
                    //mRecyclerView.setVisibility(View.VISIBLE);
                    findViewById(R.id.report_list_progress_bar).setVisibility(View.GONE);
                    reportListLayout.setVisibility(View.VISIBLE);
                    Log.d(TAG,"set ui visible");
                }
            };


            mAdapter = new ReportAdapter(isOnlyOpen, user.getIsManager(), getApplicationContext(), activity, setUIVisible, numberOfReports);
            mRecyclerView.setAdapter(mAdapter);


            if (getUser().getIsManager()) {
                mRecyclerView.addOnScrollListener(
                        new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                int visibleItemCount = mLayoutManager.getChildCount();
                                int totalItemCount = mLayoutManager.getItemCount();
                                int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();
                                if (pastVisibleItems + visibleItemCount >= numberOfReports) {
                                    //Toast.makeText(ReportListActivity.this,"Lat",Toast.LENGTH_LONG).show();

                                    numberOfReports = numberOfReports + numberOfReportsToAdd;
                                    Query query = FirebaseDatabase.getInstance().getReference().child("reports")
                                            .orderByChild("startTime").startAt(getLastReportStartTime()).limitToFirst(numberOfReportsToAdd);
                                    mAdapter.updateList(query, showList);

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_list);

        reportListLayout = findViewById(R.id.reports_list_layout);

        isOnlyOpenGroup = findViewById(R.id.list_type_buttons_group);
        isOnlyOpenGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.all_reports_button:
                        isOnlyOpen = false;
                        break;
                    case R.id.open_reports_button:
                        isOnlyOpen = true;
                        break;
                }
                showList.execute();
            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ListItemDecoration decoration = new ListItemDecoration(this, Color.LTGRAY, 1f);
        mRecyclerView.addItemDecoration(decoration);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        activity = this;

        FirebaseMessaging.getInstance().subscribeToTopic("new_report");

        //setting up a user object for the list
        user = getUser();

        user.checkCreds(new MyCallBackClass() {
            @Override
            public void execute() {
                if(getUser().getIsManager()) {
                    navigationView.getMenu().getItem(1).setVisible(false);

                }else{
                    navigationView.getMenu().getItem(1).setVisible(true);
                }

                checkPermissions(showList);
            }
        });

    }

    private void checkPermissions(MyCallBackClass showList) {
        Context context = this.getApplicationContext();

        if ((ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            if (user.getIsManager()) {
                showList.execute();
            } else {
                distanceService.getDeviceLocation(showList, mFusedLocationProviderClient, this);
            }
        } else {
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
                    distanceService.getDeviceLocation(showList, mFusedLocationProviderClient, this);
                }
            }
        }


    }

    public interface MyCallBackClass {
        void execute();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_reports) {
            startActivity(new Intent(this, ReportListActivity.class));
        } else if (id == R.id.nav_my_reports) {
            startActivity(new Intent(this, MyReportActivity.class));
        } else if (id == R.id.nav_statistics) {
            startActivity(new Intent(this, StatisticsActivity.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_agri) {
            Intent websiteIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.moag_website)));
            if (websiteIntent.resolveActivity(getPackageManager()) != null)
                startActivity(websiteIntent);
        } else if (id == R.id.nav_share) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
