package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;


public class StatisticsActivity extends AppCompatActivity {

    TextView statistics_report_this_month;
    TextView closed_successfully;
    TextView overall_open_reports;
    private View statistics_layout;
    private View statistics_progress_bar;

    ValueEventListener allReportsValueListener = new ValueEventListener() {@Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            long count = dataSnapshot.getChildrenCount();
            overall_open_reports.setText(String.valueOf(count));
            statistics_layout.setVisibility(View.VISIBLE);
            statistics_progress_bar.setVisibility(View.GONE);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics);

        statistics_report_this_month = findViewById(R.id.statistics_report_this_month);
        closed_successfully = findViewById(R.id.closed_successfully);
        overall_open_reports = findViewById(R.id.overall_open_reports);
        statistics_layout = findViewById(R.id.statistics_layout);
        statistics_progress_bar = findViewById(R.id.statistics_progress_bar);

        final DatabaseReference all_reports = FirebaseDatabase.getInstance().getReference().child("reports");
        Long pastMonth = Calendar.getInstance().getTime().getTime() - (long)(30 * 24 * 60 * 60 * 1000);


        Query this_month_query = all_reports.orderByChild("startTime").startAt(-pastMonth);
        this_month_query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                statistics_report_this_month.setText(String.valueOf(count));

                long closed_count = 0;
                for (DataSnapshot reportSS : dataSnapshot.getChildren()) {
                    Report report = reportSS.getValue(Report.class);
                    if(report.getStatus()!=null&&report.getStatus().equals("CLOSED")){
                        closed_count++;
                    }
                    closed_successfully.setText(String.valueOf(closed_count));
                    all_reports.addListenerForSingleValueEvent(allReportsValueListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
}
