package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class LogInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
    }


    public void GoToReportList(View view) {
        Intent intent = new Intent(LogInActivity.this, ReportListActivity.class);
        startActivity(intent);
    }

}
