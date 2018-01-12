package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Objects;

import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;

public class ReportViewScannerActivity extends AppCompatActivity {

    private Report report;
    private Boolean isManager;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_view_scanner);

        report = (Report) getIntent().getSerializableExtra("Report");
        isManager = getUser().getIsManager();
        userId = getUser().getId();

        boolean isScannerEnlisted = report.isScannerEnlisted(userId);

        Button buttonEnlist = findViewById(R.id.scannerAvailable);
        Button buttonUnenlist = findViewById(R.id.scannerCancelEnlistment);
        Button scannerOnTheWay = findViewById(R.id.scannerOnTheWay);

        String reportStatus = report.getStatus();

        if (!isScannerEnlisted){
            /*
            TextView textView = findViewById(R.id.activeReportOpenTime);
            textView.setVisibility(View.GONE);

            textView = findViewById(R.id.activeReportLocationHeadLine);
            textView.setVisibility(View.GONE);
            */

            LinearLayout linearLayout = findViewById(R.id.activeReportReporterNameLayout);
            linearLayout.setVisibility(View.GONE);

            linearLayout = findViewById(R.id.activeReportImageLayout);
            linearLayout.setVisibility(View.GONE);

        }

        TextView activeReportStatus = findViewById(R.id.activeReportStatus);
        activeReportStatus.setText(report.statusInHebrew(getUser()));

        TextView activeReportLocation = findViewById(R.id.activeReportLocation);
        activeReportLocation.setText(report.getAddress());

        TextView activeReportOpenTime = findViewById(R.id.activeReportOpenTime);
        activeReportOpenTime.setText(report.getStartTimeAsString());

        TextView activeReportArrivalTime = findViewById(R.id.activeReportArrivalTime);
        activeReportArrivalTime.setText(report.getDurationStr());

        String comments = report.getFreeText();
        if ((comments != null) && (!comments.isEmpty())) {
            LinearLayout commentsLayout = findViewById(R.id.activeReportExtraTextLayout);
            TextView closedReportExtraText = findViewById(R.id.activeReportExtraText);
            closedReportExtraText.setText(report.getFreeText());
            commentsLayout.setVisibility(View.VISIBLE);
        }

        if (!report.isOpenReport())
        {
            LinearLayout linearLayout = findViewById(R.id.activeReportReporterNameLayout);
            linearLayout.setVisibility(LinearLayout.GONE);

            linearLayout = findViewById(R.id.activeReportImageLayout);
            linearLayout.setVisibility(LinearLayout.GONE);

            buttonEnlist.setVisibility(LinearLayout.GONE);
        }
        else {
            TextView activeReportReporterName = findViewById(R.id.activeReportReporterName);
            activeReportReporterName.setText(report.getReporterName());

            TextView activeReportPhoneNumber = findViewById(R.id.activeReportPhoneNumber);
            activeReportPhoneNumber.setText(report.getPhoneNumber());

            /*
            String comments = report.getFreeText();
            if ((comments != null) && (!comments.isEmpty()) && isScannerEnlisted) {
                LinearLayout commentsLayout = findViewById(R.id.activeReportExtraTextLayout);
                TextView closedReportExtraText = findViewById(R.id.activeReportExtraText);
                closedReportExtraText.setText(report.getFreeText());
                commentsLayout.setVisibility(View.VISIBLE);
            }
            */

            if (report.getImageUrl() != null && isScannerEnlisted) {
                ImageView scannerReportImage = findViewById(R.id.scannerReportImage);
                scannerReportImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(report.getImageUrl()).into(scannerReportImage);
            }
        }
        if ((Objects.equals(reportStatus, "SCANNER_ON_THE_WAY")) || (report.isScannerEnlisted(userId))){

            buttonEnlist.setVisibility(LinearLayout.GONE);

        }
        if (report.isScannerEnlisted(userId)) buttonUnenlist.setVisibility(LinearLayout.VISIBLE);

        if (Objects.equals(report.getAssignedScanner(), userId))
            scannerOnTheWay.setVisibility(LinearLayout.VISIBLE);


        buttonEnlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.addToPotentialScanners(userId);
                if (Objects.equals(report.getStatus(), "NEW")) report.reportUpdateStatus("SCANNER_ENLISTED");

                startActivity(new Intent(ReportViewScannerActivity.this, ReportListActivity.class));
                finish();
            }

        });

        buttonUnenlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.subtrectFromPotentialScanners(userId);
                if (report.getAvailableScanners() < 1) report.reportUpdateStatus("NEW");

                startActivity(new Intent(ReportViewScannerActivity.this, ReportListActivity.class));
                finish();
            }

        });

        scannerOnTheWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report.updateOnTheWayTimestamp();
                report.reportUpdateStatus("SCANNER_ON_THE_WAY");
                startActivity(new Intent(ReportViewScannerActivity.this, ReportListActivity.class));
                finish();
            }

        });

    }
    /*
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ReportViewScannerActivity.this, ReportListActivity.class));
        finish();

    }
    */
}



