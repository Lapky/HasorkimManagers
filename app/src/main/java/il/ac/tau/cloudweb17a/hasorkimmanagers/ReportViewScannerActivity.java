package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;

public class ReportViewScannerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "ReportViewScanner";
    private GoogleMap mMap;
    private static final int DEFAULT_ZOOM = 15;

    private Report report;
    private String userId;


    public void refreshUI() {


        LinearLayout linearLayout = findViewById(R.id.activeReportReporterNameLayout);
        if (!report.isAssignedScanner(userId))
            linearLayout.setVisibility(View.GONE);
        else
            linearLayout.setVisibility(View.VISIBLE);

        TextView activeReportStatus = findViewById(R.id.activeReportStatus);
        String status =report.statusInHebrew();
        Log.d(TAG,status);
        activeReportStatus.setText(status);


        TextView activeReportLocation = findViewById(R.id.activeReportLocation);
        activeReportLocation.setText(report.getAddress());

        TextView activeReportOpenTime = findViewById(R.id.activeReportOpenTime);
        activeReportOpenTime.setText(report.getStartTimeAsString());

        TextView activeReportArrivalTime = findViewById(R.id.activeReportArrivalTime);
        activeReportArrivalTime.setText(report.getDuration());

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.activeReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.activeReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

/*        if (!report.isOpenReport()) {
            LinearLayout linearLayout = findViewById(R.id.activeReportReporterNameLayout);
            linearLayout.setVisibility(LinearLayout.GONE);
            buttonEnlist.setVisibility(LinearLayout.GONE);
        } else {*/
            TextView activeReportReporterName = findViewById(R.id.activeReportReporterName);
            activeReportReporterName.setText(report.getReporterName());

            TextView activeReportPhoneNumber = findViewById(R.id.activeReportPhoneNumber);
            activeReportPhoneNumber.setText(report.getPhoneNumber());
        /*}*/

        if ((Objects.equals(report.getStatus(), "SCANNER_ON_THE_WAY")) || (report.isScannerEnlisted(userId)))
            buttonEnlist.setVisibility(LinearLayout.GONE);
        else
            buttonEnlist.setVisibility(LinearLayout.VISIBLE);

        if (report.isScannerEnlisted(userId))
            buttonUnenlist.setVisibility(LinearLayout.VISIBLE);
        else
            buttonUnenlist.setVisibility(LinearLayout.GONE);

        if (Objects.equals(report.getAssignedScanner(), userId) && !Objects.equals(report.getStatus(), "SCANNER_ON_THE_WAY"))
            scannerOnTheWay.setVisibility(LinearLayout.VISIBLE);
        else
            scannerOnTheWay.setVisibility(LinearLayout.GONE);
    }

    Button buttonEnlist;
    Button buttonUnenlist;
    Button scannerOnTheWay;
    boolean isScannerEnlisted;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view_scanner);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.scanner_map);
        mapFragment.getMapAsync(this);

        report = (Report) getIntent().getSerializableExtra("Report");
        userId = getUser().getId();

        isScannerEnlisted = report.isScannerEnlisted(userId);
        report.setIsScannerEnlistedStatus(isScannerEnlisted);

        if (report.getImageUrl() != null && isScannerEnlisted) {
            FrameLayout mapLayout = findViewById(R.id.scannerMapLayout);
            ImageView scannerReportImage = findViewById(R.id.scannerReportImage);
            mapLayout.setVisibility(View.VISIBLE);
            scannerReportImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(report.getImageUrl()).into(scannerReportImage);
        }



        DatabaseReference assignedScannerRef = FirebaseDatabase.getInstance().getReference("reports").child(report.getId()).child("assignedScanner");
            assignedScannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String assignedScanner = dataSnapshot.getValue(String.class);
                report.setAssignedScanner(assignedScanner);
                refreshUI();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

            DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("reports").child(report.getId()).child("status");
            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String status = dataSnapshot.getValue(String.class);
                    report.setStatus(status);
                    refreshUI();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });



        buttonEnlist= findViewById(R.id.scannerAvailable);
        buttonUnenlist = findViewById(R.id.scannerCancelEnlistment);
        scannerOnTheWay = findViewById(R.id.scannerOnTheWay);


        buttonEnlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.addToPotentialScanners(userId);
                if (Objects.equals(report.getStatus(), "NEW"))
                    report.reportUpdateStatus("SCANNER_ENLISTED",new ReportListActivity.MyCallBackClass() {
                        @Override
                        public void execute() {
                            refreshUI();
                        }
                    });
            }

        });

        buttonUnenlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.subtrectFromPotentialScanners(userId);
                report.setIsScannerEnlistedStatus(false) ;
                if (report.getAvailableScanners() < 1) report.reportUpdateStatus("NEW", new ReportListActivity.MyCallBackClass() {
                    @Override
                    public void execute() {
                        refreshUI();
                    }
                });

                if (Objects.equals(report.getAssignedScanner(), userId))
                    report.reportUpdateAssignedScanner("");


            }

        });

        scannerOnTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.updateOnTheWayTimestamp();
                report.reportUpdateStatus("SCANNER_ON_THE_WAY", new ReportListActivity.MyCallBackClass() {
                    @Override
                    public void execute() {
                        refreshUI();
                    }
                });

            }

        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(report.getLat(), report.getLong());
        mMap.addMarker(new MarkerOptions().position(location).title("מיקום הדיווח")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }


    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ReportViewScannerActivity.this, ReportListActivity.class));
        finish();

    }

}



