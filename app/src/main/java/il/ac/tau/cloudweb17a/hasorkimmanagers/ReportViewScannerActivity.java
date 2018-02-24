package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;

public class ReportViewScannerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = ReportViewScannerActivity.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102;


    private static final int DEFAULT_ZOOM = 15;

    private Report report;
    private String userId;
    private String imagePath = null;


    public void refreshUI() {
        TextView activeReportStatus = findViewById(R.id.activeReportStatus);
        String status = report.statusInHebrew();
        Log.d(TAG, status);
        activeReportStatus.setText(status);

        if ((Objects.equals(report.getStatus(), "CLOSED")) || (Objects.equals(report.getStatus(), "CANCELED"))) {
            Button scannerAvailable = findViewById(R.id.scannerAvailable);
            LinearLayout enlistedScannerLayout = findViewById(R.id.enlistedScannerLayout);
            scannerAvailable.setVisibility(View.GONE);
            enlistedScannerLayout.setVisibility(View.GONE);

            LinearLayout.LayoutParams scrollParam = new LinearLayout.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    0,
                    5f
            );
            ScrollView scannerReportScrollView = findViewById(R.id.scannerReportScrollView);
            scannerReportScrollView.setLayoutParams(scrollParam);

            LinearLayout.LayoutParams mapParam = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    0,
                    14f
            );
            FrameLayout map = findViewById(R.id.scannerMapLayout);
            map.setLayoutParams(mapParam);
            return;
        }

        LinearLayout reporterDetailsLayout = findViewById(R.id.activeReportReporterDetailsLayout);
        if (!Objects.equals(report.getAssignedScanner(), userId)) {
            reporterDetailsLayout.setVisibility(View.GONE);

            LinearLayout.LayoutParams scrollParam = new LinearLayout.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    0,
                    3f
            );
            ScrollView scannerReportScrollView = findViewById(R.id.scannerReportScrollView);
            scannerReportScrollView.setLayoutParams(scrollParam);

            LinearLayout.LayoutParams mapParam = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    0,
                    8f
            );
            FrameLayout map = findViewById(R.id.scannerMapLayout);
            map.setLayoutParams(mapParam);
        }
        else
            reporterDetailsLayout.setVisibility(View.VISIBLE);

        TextView activeReportLocation = findViewById(R.id.activeReportLocation);
        activeReportLocation.setText(report.getAddress());

        TextView activeReportArrivalTime = findViewById(R.id.activeReportArrivalTime);
        activeReportArrivalTime.setText(report.getDuration());

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.activeReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.activeReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

        if ((Objects.equals(report.getStatus(), "SCANNER_ON_THE_WAY")) || (report.isScannerEnlisted(userId)))
            buttonEnlist.setVisibility(View.GONE);
        else {
            buttonEnlist.setVisibility(View.VISIBLE);
            enlistedScannerLayout.setVisibility(View.GONE);
        }

        if (report.isScannerEnlisted(userId)) {
            buttonUnenlist.setVisibility(View.VISIBLE);
            enlistedScannerLayout.setVisibility(View.VISIBLE);
        }
        else
            buttonUnenlist.setVisibility(View.GONE);

        if (Objects.equals(report.getAssignedScanner(), userId) && !Objects.equals(report.getStatus(), "SCANNER_ON_THE_WAY")) {
            scannerOnTheWay.setVisibility(View.VISIBLE);
            enlistedScannerLayout.setVisibility(View.VISIBLE);
        } else
            scannerOnTheWay.setVisibility(View.GONE);

        if (Objects.equals(report.getAssignedScanner(), userId)) {
            if (report.getImageUrl() == null) {
                LinearLayout.LayoutParams mapParam = new LinearLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        0,
                        4.5f
                );
                FrameLayout map = findViewById(R.id.scannerMapLayout);
                map.setLayoutParams(mapParam);
            }
            else {
                ImageView scannerReportImage = findViewById(R.id.scannerReportImage);
                scannerReportImage.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams mapParam = new LinearLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        0,
                        2.9f
                );
                FrameLayout map = findViewById(R.id.scannerMapLayout);
                map.setLayoutParams(mapParam);

                try {
                    Glide.with(this).load(report.getImageUrl()).into(scannerReportImage);
                } catch (Exception ignored) {

                }
            }
        }
        else {
            ImageView scannerReportImage = findViewById(R.id.scannerReportImage);
            scannerReportImage.setVisibility(View.GONE);

            LinearLayout.LayoutParams mapParam = new LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    0,
                    7f
            );
            FrameLayout map = findViewById(R.id.scannerMapLayout);
            map.setLayoutParams(mapParam);
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            downloadImage();
        }
    }

    private void downloadImage() {
        if (report.getImageUrl() != null) {
            Glide.with(this).asBitmap().load(report.getImageUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    saveImage(resource);
                    setShareIntent();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE: {
                if ((grantResults.length > 0) &&
                        (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    downloadImage();
                } else Toast.makeText(this, R.string.need_permission,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    Button buttonEnlist;
    Button buttonUnenlist;
    Button scannerOnTheWay;
    boolean isScannerEnlisted;
    LinearLayout enlistedScannerLayout;

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
        report.setScannerEnlisted(isScannerEnlisted);

        DatabaseReference assignedScannerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("assignedScanner");
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

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("potentialScanners");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Iterable<DataSnapshot> contactChildren = snapshot.getChildren();
                for (DataSnapshot userId : contactChildren)
                    report.addPotentialScanner(userId.getKey(), userId.getValue().toString());

                report.setAvailableScanners(report.getPotentialScannersSize());

                refreshUI();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        });

        DatabaseReference statusRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("status");
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

        TextView activeReportReporterName = findViewById(R.id.activeReportReporterName);
        activeReportReporterName.setText(report.getReporterName());

        final TextView activeReportPhoneNumber = findViewById(R.id.activeReportPhoneNumber);
        activeReportPhoneNumber.setText(report.getPhoneNumber());

        ImageButton callReporterNumber = findViewById(R.id.call_number_button);
        callReporterNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callReporter(activeReportPhoneNumber.getText().toString());
            }
        });

        final LinearLayout scannerReportExtraPhoneNumberLayout = findViewById(R.id.scannerReportExtraPhoneNumberLayout);
        final TextView scannerReportExtraPhoneNumber = findViewById(R.id.scannerReportExtraPhoneNumber);

        DatabaseReference statusExtraPhoneNumber = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("extraPhoneNumber");

        statusExtraPhoneNumber.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String extraPhoneNumber = dataSnapshot.getValue(String.class);
                if ((extraPhoneNumber != null) && (!extraPhoneNumber.isEmpty())) {
                    scannerReportExtraPhoneNumber.setText(extraPhoneNumber);
                    scannerReportExtraPhoneNumberLayout.setVisibility(View.VISIBLE);
                }
                else
                    scannerReportExtraPhoneNumberLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageButton callReporterExtraNumber = findViewById(R.id.call_extra_number_button);
        callReporterExtraNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callReporter(scannerReportExtraPhoneNumber.getText().toString());
            }
        });

        buttonEnlist = findViewById(R.id.scannerAvailable);
        buttonUnenlist = findViewById(R.id.scannerCancelEnlistment);
        scannerOnTheWay = findViewById(R.id.scannerOnTheWay);
        enlistedScannerLayout = findViewById(R.id.enlistedScannerLayout);

        buttonEnlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.addToPotentialScanners(userId);
                if (Objects.equals(report.getStatus(), "NEW"))
                    report.reportUpdateStatus("SCANNER_ENLISTED", new ReportListActivity.MyCallBackClass() {
                        @Override
                        public void execute() {
                            refreshUI();
                        }
                    });
                else
                    refreshUI();
            }

        });

        buttonUnenlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String status = report.getStatus();
                if (Objects.equals(report.getAssignedScanner(), userId) &&
                        (status.equals("MANAGER_ASSIGNED_SCANNER") || status.equals("SCANNER_ON_THE_WAY"))) {

                    scannerCanceledPopUp();
                }
                else
                    cancelScanner();
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

        final LinearLayout managerInChargeLayout = findViewById(R.id.managerInChargeLayout);
        final TextView managerInChargeName = findViewById(R.id.managerInChargeName);

        DatabaseReference managerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("managerInCharge");
        managerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentManagerId = dataSnapshot.getValue().toString();

                if (currentManagerId.equals(""))
                    managerInChargeLayout.setVisibility(View.GONE);
                else {
                    Query mUserReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").orderByChild("id").equalTo(currentManagerId);

                    mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                User dbUser = user.getValue(User.class);
                                String managerName = dbUser.getName();
                                managerInChargeName.setText(managerName);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    managerInChargeLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestPermission(); // TODO is there a reason this is here? (Shahar)
    }

    public void scannerCanceledPopUp() {
        TextView title = new TextView(getApplicationContext());
        title.setText(R.string.scanner_not_in_charge_popup_title);
        title.setPadding(10, 50, 64, 9);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        String popupMessage;
        if (report.getStatus().equals("MANAGER_ASSIGNED_SCANNER"))
            popupMessage = getString(R.string.scanner_cancel_after_being_assigned);
        else
            popupMessage = getString(R.string.scanner_cancel_after_reporting_on_the_way);

        new AlertDialog.Builder(this).setMessage(popupMessage)
                .setCustomTitle(title)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ImageView scannerReportImage = findViewById(R.id.scannerReportImage);
                        scannerReportImage.setVisibility(View.GONE);
                        cancelScanner();
                    }
                }).create().show();
    }

    public void cancelScanner() {
        if (Objects.equals(report.getAssignedScanner(), userId)) {
            report.reportUpdateAssignedScanner("");
            report.reportUpdateStatus("NEW", null);
        }

        report.subtrectFromPotentialScanners(userId);
        report.setScannerEnlisted(false);

        refreshUI();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(report.getLatitude(), report.getLongitude());
        mMap.addMarker(new MarkerOptions().position(location).title("מיקום הדיווח")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        setShareIntent();

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setType("text/plain");

        if (imagePath != null) {
            Uri photoURI = FileProvider.getUriForFile(ReportViewScannerActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    new File(imagePath));
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            shareIntent.setType("image/jpeg");
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "יצאתי לסרוק כלב אבוד ברחוב " + report.getAddress() + " דרך אפליקציית הסורקים" +
                "\n" +
                "רוצה לדווח גם?" +
                " הורד את האפליקיה https://play.google.com/store/apps/details?id=il.ac.tau.cloudweb17a.hasorkim");
        mShareActionProvider.setShareIntent(shareIntent);
    }

    private void saveImage(Bitmap image) {
        String savedImagePath = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            File imageFile = new File(storageDir, imageFileName);
            savedImagePath = imageFile.getAbsolutePath();
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imagePath = savedImagePath;
    }

    public void callReporter(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        if (callIntent.resolveActivity(getPackageManager()) != null)
            startActivity(callIntent);
    }
}



