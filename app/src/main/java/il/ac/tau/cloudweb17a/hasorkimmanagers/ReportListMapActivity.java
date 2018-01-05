package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class ReportListMapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list_map);


        Button openVetListButton = findViewById(R.id.openReportListButton);
        openVetListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReportListMapActivity.this, ReportListActivity.class);
                startActivity(intent);
            }
        });
    }
}