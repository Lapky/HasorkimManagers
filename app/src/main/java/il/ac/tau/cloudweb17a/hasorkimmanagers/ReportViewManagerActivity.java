package il.ac.tau.cloudweb17a.hasorkimmanagers;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.Color;
        import android.os.Bundle;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.PopupWindow;
        import android.widget.TextView;

        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.util.ArrayList;
        import java.util.Objects;

public class ReportViewManagerActivity extends BaseActivity {

    private LayoutInflater layoutInflater;
    private ViewGroup thisContainer;
    private PopupWindow popupWindow;

    private Report report;
    private Boolean isManager;
    private String userId;
    private Bitmap bitmap;

    final String TAG = "ReportViewManager";

    ArrayList<Scanner> scannerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_active_report);

        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        thisContainer = (ViewGroup) layoutInflater.inflate(R.layout.activity_report_view_manager, null);

        mDrawer.addView(thisContainer, 0);

        report = (Report) getIntent().getSerializableExtra("Report");
        isManager = (Boolean) getIntent().getSerializableExtra("isManager");
        userId = (String) getIntent().getSerializableExtra("userId");

        String reportStatus = report.getStatus();

        TextView managerReportStatus = findViewById(R.id.managerReportStatus);
        managerReportStatus.setText(report.statusInHebrew(isManager, userId));

        TextView managerReportLocation = findViewById(R.id.managerReportLocation);
        managerReportLocation.setText(report.getAddress());

        TextView managerReportOpenTime = findViewById(R.id.managerReportOpenTime);
        managerReportOpenTime.setText(report.getStartTimeAsString());

        TextView managerReportReporterName = findViewById(R.id.managerReportReporterName);
        managerReportReporterName.setText(report.getReporterName());

        TextView managerReportPhoneNumber = findViewById(R.id.managerReportPhoneNumber);
        managerReportPhoneNumber.setText(report.getPhoneNumber());

        String comments = report.getFreeText();
        if (comments != null) {
            LinearLayout commentsLayout = findViewById(R.id.managerReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.managerReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

        TextView managerReportCurrentScanner = findViewById(R.id.managerReportCurrentScanner);
        String assignedScanner = report.getAssignedScanner();
        managerReportCurrentScanner.setText(assignedScanner.substring(0, Math.min(assignedScanner.length(), 20)));

        if (report.getImageUrl() != null) {
            bitmap = report.getBitmapFromURL(report.getImageUrl());
            ImageView closedReportImage = findViewById(R.id.managerReportImage);
            closedReportImage.setImageBitmap(bitmap);
            closedReportImage.setVisibility(View.VISIBLE);
        }

        scannerList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("reports").child(report.getId()).child("potentialScanners");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot: dataSnapshot.getChildren()) {

                    String userId = messageSnapshot.getKey();
                    String duration = messageSnapshot.getValue().toString();
                    boolean isAssignedScanner = false;
                    if (Objects.equals(userId, report.getAssignedScanner())) isAssignedScanner = true;

                    scannerList.add(new Scanner(userId, duration, isAssignedScanner));
                }

                ScannerAdapter adapter = new ScannerAdapter(
                        ReportViewManagerActivity.this,
                        R.layout.scanner_list_item,
                        scannerList
                );
                ListView listView = (ListView) findViewById(R.id.list_view_scanners);
                listView.setAdapter(adapter);

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button closeReport = findViewById(R.id.closeReport);

        closeReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView closeCancelReportReason = findViewById(R.id.closeCancelReportReason);
                report.reportUpdateCancellationText(closeCancelReportReason.getText().toString());
                report.reportUpdateCancellationReporterType(isManager);
                report.reportUpdateStatus("CLOSED");
                startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
                finish();
            }

        });

        Button cancelReport = findViewById(R.id.cancelReport);

        cancelReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView closeCancelReportReason = findViewById(R.id.closeCancelReportReason);
                report.reportUpdateCancellationText(closeCancelReportReason.getText().toString());
                report.reportUpdateCancellationReporterType(isManager);
                report.reportUpdateStatus("CANCELED");
                startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
                finish();
            }

        });

    }

    public void sendScannerHandler(View view)
    {

        LinearLayout vwParentRow = (LinearLayout)view.getParent();
        TextView scanner_name = (TextView)vwParentRow.getChildAt(2);

        String scannerNameString = scanner_name.getText().toString();

        Button sendScanner = (Button)vwParentRow.getChildAt(3);

        String assignedScanner = report.getAssignedScanner();

        if (assignedScanner != null && !Objects.equals(assignedScanner, "") &&
                !Objects.equals(assignedScanner, scannerNameString)){
            sendScanner.setError("יכול להיות רק סורק מאושר אחד");
        }
        else{

            if (!Objects.equals(report.getAssignedScanner(), scannerNameString)){

                report.reportUpdateAssignedScanner(scannerNameString);

                sendScanner.setText("סורק נבחר");

                vwParentRow.setBackgroundColor(Color.YELLOW);

                report.reportUpdateStatus("MANAGER_ASSIGNED_SCANNER");
            }
            else{
                report.reportUpdateAssignedScanner("");
                sendScanner.setText("שלח");
                vwParentRow.setBackgroundColor(Color.WHITE);
                report.reportUpdateStatus("MANAGER_ENLISTED");
            }
        }


        vwParentRow.refreshDrawableState();


        //Log.d(TAG, scanner_name.getText().toString());
    }
}









