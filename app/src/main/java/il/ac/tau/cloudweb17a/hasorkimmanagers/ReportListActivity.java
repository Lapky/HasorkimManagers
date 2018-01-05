package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;


public class ReportListActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button openVetMapButton = findViewById(R.id.openReportMapButton);
        openVetMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReportListActivity.this, ReportListMapActivity.class);
                startActivity(intent);
            }
        });

        ArrayList<Report> reportList = new ArrayList<>();

        reportList.add(new Report(1, "Shahar", "1-12-2017 13:52:45", "Street Sokolov 14, City Ramat-Gan",
                "סורק אישר", "Dog looks a bit sick", 544764751, "C58",
                7, 0.3));
        reportList.add(new Report(2, "Bar", "1-12-2017 13:32:08", "Street Arlozorov 51, City Tel-Aviv",
                "חיפוש סורק", "", 503724771, "",
                4, 1.5));
        reportList.add(new Report(3, "Chan", "1-12-2017 07:01:12", "Street Hod 33, City Arad",
                "סורק אישר", "Dog is in my yard", 544999701, "S4",
                1, 189.4));
        reportList.add(new Report(4, "Boris", "29-11-2017 13:09:16", "Street Tpuach 18, City Yesod Hamahla",
                "סורק - שווא", "Dog is sad", 523864011, "N1",
                3, 233.3));
        reportList.add(new Report(5, "Momo", "29-11-2017 18:18:59", "Street Shlavim 27, City Petach Tikva",
                "נמסר", "", 524710723, "E12",
                13, 6.7));
        reportList.add(new Report(6, "Gamba", "28-11-2017 19:48:36", "Street Sokolov 4, City Kiryat-Bialic",
                "מחכה לאיסוף", "I love dogs", 544444891, "N10",
                6, 94.8));

        ReportAdapter adapter = new ReportAdapter(
                this,
                R.layout.report_list_item,
                reportList
        );
        ListView listView = findViewById(R.id.list_view_reports);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                if (position == 0) {
                    Intent intent = new Intent(ReportListActivity.this, ReportViewScannerActivity.class);
                    startActivity(intent);
                }
                if (position == 1) {
                    Intent intent = new Intent(ReportListActivity.this, ReportViewManagerActivity.class);
                    startActivity(intent);
                }


            }
        });
    }

}
