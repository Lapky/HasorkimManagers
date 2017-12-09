package il.ac.tau.cloudweb17a.hasorkimmanagers;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.ViewGroup;
        import android.widget.ListView;
        import android.widget.PopupWindow;

        import java.util.ArrayList;

public class ReportViewManagerActivity extends BaseActivity {

    private LayoutInflater layoutInflater;
    private ViewGroup thisContainer;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_active_report);

        layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        thisContainer = (ViewGroup) layoutInflater.inflate(R.layout.activity_report_view_manager, null);

        mDrawer.addView(thisContainer, 0);

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
        //Button cancelReportButton = findViewById(R.id.cancelReport);


        //Button whatNowInfo = findViewById(R.id.whatNowInfo);

    }
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        startActivity(new Intent(ReportViewManagerActivity.this, ReportListActivity.class));
        finish();

    }
}









