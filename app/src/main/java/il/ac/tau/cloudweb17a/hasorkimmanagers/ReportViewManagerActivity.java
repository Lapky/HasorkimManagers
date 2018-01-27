package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.Manifest;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class ReportViewManagerActivity extends AppCompatActivity {


    private Report report;
    private ShareActionProvider mShareActionProvider;
    final String TAG = ReportViewManagerActivity.class.getSimpleName();
    private String imagePath = null;

    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102;

    ArrayList<Scanner> scannerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view_manager);

        report = (Report) getIntent().getSerializableExtra("Report");

        scannerList = new ArrayList<>();
        final ScannerAdapter adapter = new ScannerAdapter(this, scannerList);

        ListView listView = findViewById(R.id.list_view_scanners);
        listView.setAdapter(adapter);

        //String reportStatus = report.getStatus();

        final TextView managerReportStatus = findViewById(R.id.managerReportStatus);
        managerReportStatus.setText(report.statusInHebrew());

        TextView managerReportLocation = findViewById(R.id.managerReportLocation);
        managerReportLocation.setText(report.getAddress());

        TextView managerReportOpenTime = findViewById(R.id.managerReportOpenTime);
        managerReportOpenTime.setText(report.getStartTimeAsString());

        TextView managerReportReporterName = findViewById(R.id.managerReportReporterName);
        managerReportReporterName.setText(report.getReporterName());

        TextView managerReportPhoneNumber = findViewById(R.id.managerReportPhoneNumber);
        managerReportPhoneNumber.setText(report.getPhoneNumber());

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.managerReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.managerReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

        if (report.getImageUrl() != null) {
            ImageView managerReportImage = findViewById(R.id.managerReportImage);
            managerReportImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(report.getImageUrl()).into(managerReportImage);
        }

        DatabaseReference statusManagerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("status");

        statusManagerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String status = dataSnapshot.getValue(String.class);
                report.setStatus(status);
                managerReportStatus.setText(report.statusInHebrew());
                updateInactiveReport(report.getStatus());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        scannerList = new ArrayList<>();

        DatabaseReference listRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("potentialScanners");
        listRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();

                DatabaseReference assignedScannerRef = FirebaseDatabase.getInstance()
                        .getReference("reports").child(report.getId()).child("assignedScanner");

                assignedScannerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        report.setAssignedScanner(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {

                    final String userId = messageSnapshot.getKey();
                    final String duration = messageSnapshot.getValue().toString();
                    final boolean isAssignedScanner = Objects.equals(userId, report.getAssignedScanner());

                    Query mUserReference = FirebaseDatabase.getInstance().getReference()
                            .child("users").orderByChild("id").equalTo(userId);

                    mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                User dbUser = user.getValue(User.class);
                                adapter.add(new Scanner(userId, dbUser.getName(), duration, isAssignedScanner));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestPermission();
    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

    private void updateInactiveReport(String status) {
        if ((status.equals("CLOSED")) || (status.equals("CANCELED"))) {
            LinearLayout managerButtons = findViewById(R.id.viewManagerButtonsLayout);
            LinearLayout availableScannersList = findViewById(R.id.availableScannersLayout);
            managerButtons.setVisibility(View.GONE);
            availableScannersList.setVisibility(View.GONE);

            final TextView closing_or_cancellation_reason = findViewById(R.id.closing_or_cancellation_reason);
            TextView closing_or_cancellation_headline = findViewById(R.id.closing_or_cancellation_headline);

            if (status.equals("CLOSED")) {
                closing_or_cancellation_headline.setText(R.string.closing_reason_headline);
                final Query queryCancelText = FirebaseDatabase.getInstance()
                        .getReference("reports").child(report.getId()).child("closingText");

                queryCancelText.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        closing_or_cancellation_reason.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            } else {
                closing_or_cancellation_headline.setText(R.string.cancellation_reason_headline);

                final Query queryCancelText = FirebaseDatabase.getInstance()
                        .getReference("reports").child(report.getId()).child("cancellationText");

                Query queryCancelUserType = FirebaseDatabase.getInstance()
                        .getReference("reports").child(report.getId()).child("cancellationUserType");

                queryCancelText.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        closing_or_cancellation_reason.setText(dataSnapshot.getValue(String.class));

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                final TextView cancellation_user_type = findViewById(R.id.cancellation_user_type);
                queryCancelUserType.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        cancellation_user_type.setText(dataSnapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                LinearLayout report_cancelled_by = findViewById(R.id.report_canceled_by);
                report_cancelled_by.setVisibility(View.VISIBLE);
            }

            RelativeLayout closing_or_cancellation_reason_layout = findViewById(R.id.closed_or_cancelled_relative_layout);
            closing_or_cancellation_reason_layout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * The listener for the "cancel report" button
     */
    public void OnCloseReportButtonClick(View v) {
        TextView title = new TextView(ReportViewManagerActivity.this);
        final EditText editText = new EditText(ReportViewManagerActivity.this);

        title.setText(R.string.close_report_dialog);
        title.setPadding(10, 50, 64, 9);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        title.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        new AlertDialog.Builder(ReportViewManagerActivity.this)
                .setMessage(R.string.close_report_dialog_message)
                .setCustomTitle(title)
                .setView(editText)
                .setPositiveButton(getResources().getString(R.string.close_report_in_dialog), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String closeReportText = editText.getText().toString();

                        report.reportUpdateClosingText(closeReportText);
                        report.setStatus("CLOSED");
                        report.reportUpdateStatus("CLOSED", null);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.go_back_in_dialog), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create().show();
    }

    /**
     * The listener for the "cancel report" button
     */
    public void OnCancelReportButtonClick(View v) {
        TextView title = new TextView(ReportViewManagerActivity.this);
        final EditText editText = new EditText(ReportViewManagerActivity.this);

        title.setText(R.string.cancel_report_dialog);
        title.setPadding(10, 50, 64, 9);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);
        title.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

        new AlertDialog.Builder(ReportViewManagerActivity.this)
                .setMessage(R.string.cancel_report_dialog_message)
                .setCustomTitle(title)
                .setView(editText)
                .setPositiveButton(getResources().getString(R.string.cancel_report_in_dialog), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String cancelReportText = editText.getText().toString();

                        report.reportUpdateCancellationText(cancelReportText);
                        report.reportUpdateCancellationManagerType();
                        report.setStatus("CANCELED");
                        report.reportUpdateStatus("CANCELED", null);
                    }
                })
                .setNegativeButton(getResources().getString(R.string.go_back_in_dialog), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create().show();
    }

    public void sendScannerHandler(View view) {
        LinearLayout vwParentRow = (LinearLayout) view.getParent();
        TextView scanner_name = (TextView) vwParentRow.getChildAt(1);

        String scannerNameString = scanner_name.getText().toString();

        Button sendScanner = (Button) vwParentRow.getChildAt(2);

        String assignedScanner = report.getAssignedScanner();

        if (assignedScanner != null && !Objects.equals(assignedScanner, "") &&
                !Objects.equals(assignedScanner, scannerNameString)) {
            sendScanner.setError("יכול להיות רק סורק מאושר אחד");
        } else {
            if (!Objects.equals(report.getAssignedScanner(), scannerNameString)) {
                report.reportUpdateAssignedScanner(scannerNameString);
                sendScanner.setText(R.string.scanner_was_chosen);
                report.setStatus("MANAGER_ASSIGNED_SCANNER");
                report.reportUpdateStatus("MANAGER_ASSIGNED_SCANNER", null);
                view.setBackgroundColor(Color.CYAN);
            } else {
                report.reportUpdateAssignedScanner("");
                sendScanner.setText(R.string.choose_scanner);
                report.setStatus("NEW");
                report.reportUpdateStatus("NEW", null);
                view.setBackgroundColor(Color.LTGRAY);
            }
        }

        vwParentRow.refreshDrawableState();

        //Log.d(TAG, scanner_name.getText().toString());
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
            Uri photoURI = FileProvider.getUriForFile(ReportViewManagerActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    new File(imagePath));
            shareIntent.putExtra(Intent.EXTRA_STREAM, photoURI);
            shareIntent.setType("image/jpeg");
        }
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "אני מנהל דיווח על כלב אבוד ברחוב " + report.getAddress() + " דרך אפליקציית הסורקים" +
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

    /*
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
        finish();
    }
    */
}









