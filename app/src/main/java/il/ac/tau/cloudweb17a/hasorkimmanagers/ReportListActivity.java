package il.ac.tau.cloudweb17a.hasorkimmanagers;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;


import static il.ac.tau.cloudweb17a.hasorkimmanagers.User.getUser;


public class ReportListActivity extends BaseActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    User user;
    Activity activity;
    TextView dText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_report_list);

        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        activity = this;

        //setting up a user objext for the list
        user = getUser();

        dText = findViewById(R.id.dText);
        dText.setText(String.valueOf(user.getIsManager()));

        user.checkCreds(new MyCallBackClass(){
            @Override
            public void execute() {
                RecyclerView.Adapter mAdapter = new ReportAdapter(false,user.getIsManager(), getApplicationContext(),activity);
                mRecyclerView.addItemDecoration(new DividerItemDecoration(activity, LinearLayoutManager.VERTICAL));
                mRecyclerView.setAdapter(mAdapter);


                dText.setText(String.valueOf(user.getIsManager()));
            }
        });


    }

    public interface MyCallBackClass {
        void execute();
    }


}
