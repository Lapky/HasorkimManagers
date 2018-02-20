package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

public class MyReportActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        RecyclerView mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        ProgressBar mProgressBar = findViewById(R.id.report_list_progress_bar);

        RecyclerView.Adapter mAdapter = new MyReportAdapter(mProgressBar,mRecyclerView, getApplicationContext());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);

        Button backToNewReports = findViewById(R.id.going_to_reports_btn);
        backToNewReports.setVisibility(View.GONE);

        findViewById(R.id.reports_list_layout).setVisibility(View.VISIBLE);
    }
}
