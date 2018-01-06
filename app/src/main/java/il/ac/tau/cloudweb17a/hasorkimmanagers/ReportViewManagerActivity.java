package il.ac.tau.cloudweb17a.hasorkimmanagers;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.LinearLayout;
        import android.widget.ListView;
        import android.widget.PopupWindow;
        import android.widget.TextView;

        import java.util.ArrayList;

public class ReportViewManagerActivity extends BaseActivity {

    private LayoutInflater layoutInflater;
    private ViewGroup thisContainer;
    private PopupWindow popupWindow;

    private Report report;
    private Boolean isManager;
    private String userId;
    private Bitmap bitmap;

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

        ArrayList<Scanner> scannerList = new ArrayList<>();

        scannerList.add(new Scanner("אלה",0.3));
        scannerList.add(new Scanner("חיים",0.8));
        scannerList.add(new Scanner("עופרי",1.4));
        scannerList.add(new Scanner("מעיין",1.5));
        scannerList.add(new Scanner("יוסי",2.2));
        scannerList.add(new Scanner("אמיר",2.7));


        ScannerAdapter adapter = new ScannerAdapter(
                this,
                R.layout.scanner_list_item,
                scannerList
        );
        ListView listView = (ListView) findViewById(R.id.list_view_scanners);
        listView.setAdapter(adapter);

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
        finish();

    }
}









