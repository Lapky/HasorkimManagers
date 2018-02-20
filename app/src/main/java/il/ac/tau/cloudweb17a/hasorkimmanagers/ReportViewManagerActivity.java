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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
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
import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;

public class ReportViewManagerActivity extends AppCompatActivity {


    private Report report;
    private ShareActionProvider mShareActionProvider;
    final String TAG = ReportViewManagerActivity.class.getSimpleName();
    private String imagePath = null;
    private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 102;
    ArrayList<Scanner> scannerList;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view_manager);

        report = (Report) getIntent().getSerializableExtra("Report");
        userId = getUser().getId();
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
        String reportTime = report.getStartTimeAsString();
        reportTime = (reportTime.substring(6, reportTime.length()) + " ," + reportTime.substring(0, 5));
        managerReportOpenTime.setText(reportTime);

        TextView managerReportReporterName = findViewById(R.id.managerReportReporterName);
        managerReportReporterName.setText(report.getReporterName());

        final TextView managerReportPhoneNumber = findViewById(R.id.managerReportPhoneNumber);
        managerReportPhoneNumber.setText(report.getPhoneNumber());

        ImageButton callReporterNumber = findViewById(R.id.call_number_button);
        callReporterNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callReporter(managerReportPhoneNumber.getText().toString());
            }
        });

        final LinearLayout managerReportExtraPhoneNumberLayout = findViewById(R.id.managerReportExtraPhoneNumberLayout);
        final TextView managerReportExtraPhoneNumber = findViewById(R.id.managerReportExtraPhoneNumber);

        DatabaseReference statusExtraPhoneNumber = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("extraPhoneNumber");

        statusExtraPhoneNumber.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String extraPhoneNumber = dataSnapshot.getValue(String.class);
                if ((extraPhoneNumber != null) && (!extraPhoneNumber.isEmpty())) {
                    managerReportExtraPhoneNumber.setText(extraPhoneNumber);
                    managerReportExtraPhoneNumberLayout.setVisibility(View.VISIBLE);
                }
                else
                    managerReportExtraPhoneNumberLayout.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ImageButton callReporterExtraNumber = findViewById(R.id.call_extra_number_button);
        callReporterExtraNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callReporter(managerReportExtraPhoneNumber.getText().toString());
            }
        });

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.managerReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.managerReportExtraText);
            closedReportExtraText.setText(comments);
            commentsLayout.setVisibility(View.VISIBLE);
        }

        TextView managerReportIsDogWithReporter = findViewById(R.id.isDogWithReporter);
        Boolean isDogWithReporter = report.getIsDogWithReporter();
        if (isDogWithReporter)
            managerReportIsDogWithReporter.setText(R.string.dog_is_with_reporter);
        else
            managerReportIsDogWithReporter.setText(R.string.dog_is_not_with_reporter);

        if (report.getImageUrl() != null) {
            ImageView managerReportImage = findViewById(R.id.managerReportImage);
            managerReportImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(report.getImageUrl()).into(managerReportImage);
        }
        else {
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    0,
                    3f
            );
            ScrollView scroll = findViewById(R.id.report_details_scroll_view);
            scroll.setLayoutParams(param);
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

        final TextView managerInChargeName = findViewById(R.id.manager_in_charge_name);
        final Button setManager = findViewById(R.id.setManager);
        setManager.setVisibility(View.VISIBLE);
        final Button deleteManager = findViewById(R.id.deleteManager);
        deleteManager.setVisibility(View.GONE);
        deleteManager.setVisibility(View.GONE);

        DatabaseReference managerRef = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("managerInCharge");
        managerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String currentManagerId = dataSnapshot.getValue().toString();

                if (!report.getManagerInCharge().equals(currentManagerId))
                    report.setManagerInCharge(currentManagerId);

                if (currentManagerId.equals(""))
                    managerInChargeName.setText("");
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
                }

                if (currentManagerId.equals(userId)) {
                    setManager.setVisibility(View.GONE);
                    deleteManager.setVisibility(View.VISIBLE);
                }
                else {
                    setManager.setVisibility(View.VISIBLE);
                    deleteManager.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        requestPermission(); // TODO is there a reason this is here? (Shahar)
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
            LinearLayout managingReportLayout = findViewById(R.id.managing_report);
            managerButtons.setVisibility(View.GONE);
            availableScannersList.setVisibility(View.GONE);
            managingReportLayout.setVisibility(View.GONE);

            String managerInCharge = report.getManagerInCharge();
            if ((managerInCharge != null) && (!managerInCharge.isEmpty())) {
                LinearLayout managerInChargeLayout = findViewById(R.id.managing_report_after_closed_or_deleted);
                final TextView managerOfReport = findViewById(R.id.manager_that_was_in_charge_name);

                Query mUserReference = FirebaseDatabase.getInstance().getReference()
                        .child("users").orderByChild("id").equalTo(managerInCharge);

                mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot user : dataSnapshot.getChildren()) {
                            User dbUser = user.getValue(User.class);
                            String managerName = dbUser.getName();
                            managerOfReport.setText(managerName);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                managerInChargeLayout.setVisibility(View.VISIBLE);
            }

            final TextView closing_or_cancellation_reason = findViewById(R.id.closing_or_cancellation_reason);
            TextView closing_or_cancellation_headline = findViewById(R.id.closing_or_cancellation_headline);

            if (status.equals("CLOSED")) {
                closing_or_cancellation_headline.setText(R.string.closing_reason_headline);
                String reasonForClosing = report.getCancellationText();

                if ((reasonForClosing == null) || reasonForClosing.isEmpty())
                    closing_or_cancellation_reason.setText(R.string.closing_reason_was_not_entered);
                else
                    closing_or_cancellation_reason.setText(reasonForClosing);
            }
            else {
                closing_or_cancellation_headline.setText(R.string.cancellation_reason_headline);
                String reasonForCancelling = report.getCancellationText();

                if ((reasonForCancelling == null) || reasonForCancelling.equals(""))
                    closing_or_cancellation_reason.setText(R.string.cancelling_reason_was_not_entered);
                else
                    closing_or_cancellation_reason.setText(reasonForCancelling);

                TextView cancellerType = findViewById(R.id.cancellation_user_type);
                String canceller = report.getCancellationUserType();

                if ((canceller != null) && (!canceller.isEmpty())) {
                    cancellerType.setText(canceller);
                    LinearLayout report_cancelled_by = findViewById(R.id.report_canceled_by);
                    report_cancelled_by.setVisibility(View.VISIBLE);
                }
            }

            LinearLayout closing_or_cancellation_reason_layout = findViewById(R.id.closed_or_cancelled_linear_layout);
            closing_or_cancellation_reason_layout.setVisibility(View.VISIBLE);
        }
    }

    public void OnSetManageReportButtonClick(View v) {
        String managerInCharge = report.getManagerInCharge();

        if (managerInCharge.equals(userId))
            return;

        if (managerInCharge.equals(""))
            report.reportUpdateManagerInCharge(userId);
        else {
            TextView title = new TextView(this);
            title.setText(R.string.attention);
            title.setPadding(10, 50, 64, 9);
            title.setTextColor(Color.BLACK);
            title.setTextSize(20);

            new AlertDialog.Builder(this).setMessage(R.string.replacing_manager_message)
                    .setCustomTitle(title)
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    }).setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    report.reportUpdateManagerInCharge(userId);
                }
            }).create().show();
        }
    }

    public void OnDeleteManageReportButtonClick(View v) {
        TextView title = new TextView(this);
        title.setText(R.string.attention);
        title.setPadding(10, 50, 64, 9);
        title.setTextColor(Color.BLACK);
        title.setTextSize(20);

        new AlertDialog.Builder(this).setMessage(R.string.deleting_manager_message)
                .setCustomTitle(title)
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                report.reportUpdateManagerInCharge("");
            }
        }).create().show();
    }

    public void OnCloseReportButtonClick(View v) {
        String managerInCharge = report.getManagerInCharge();

        if (!managerInCharge.equals(userId))
            managerNotInChargePopup();
        else {
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
                    .setPositiveButton(getResources().getString(R.string.go_back_in_dialog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.close_report_in_dialog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String closeReportText = editText.getText().toString();

                            report.reportUpdateClosingText(closeReportText);
                            report.setStatus("CLOSED");
                            report.reportUpdateStatus("CLOSED", null);
                        }
                    })
                    .create().show();
        }
    }

    public void OnCancelReportButtonClick(View v) {
        String managerInCharge = report.getManagerInCharge();

        if (!managerInCharge.equals(userId))
            managerNotInChargePopup();
        else {
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
                    .setPositiveButton(getResources().getString(R.string.go_back_in_dialog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel_report_in_dialog), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String cancelReportText = editText.getText().toString();

                            report.reportUpdateCancellationText(cancelReportText);
                            report.reportUpdateCancellationManagerType();
                            report.setStatus("CANCELED");
                            report.reportUpdateStatus("CANCELED", null);
                        }
                    })
                    .create().show();
        }
    }

    public void managerNotInChargePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReportViewManagerActivity.this);
        LayoutInflater inflater = ReportViewManagerActivity.this.getLayoutInflater();

        TextView title = new TextView(getApplicationContext());
        title.setText(R.string.manager_not_in_charge_popup_title);
        title.setPadding(10, 50, 64, 9);
        title.setTextColor(Color.BLACK);
        title.setTextSize(22);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.manager_not_in_charge_pop, null));
        builder.setCustomTitle(title);
        builder.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        builder.create().show();
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
                view.setBackgroundColor(getResources().getColor(R.color.deleteScannerColor));
            } else {
                report.reportUpdateAssignedScanner("");
                sendScanner.setText(R.string.choose_scanner);
                report.setStatus("NEW");
                report.reportUpdateStatus("NEW", null);
                view.setBackgroundColor(getResources().getColor(R.color.setScannerColor));
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

    public void callReporter(String number) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
        if (callIntent.resolveActivity(getPackageManager()) != null)
            startActivity(callIntent);
    }
}









