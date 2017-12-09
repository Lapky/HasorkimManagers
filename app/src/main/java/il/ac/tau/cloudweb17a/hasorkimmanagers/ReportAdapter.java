package il.ac.tau.cloudweb17a.hasorkimmanagers;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ReportAdapter extends ArrayAdapter<Report> {
    private final int listLayout;

    public ReportAdapter(Context context,
                         int listLayout,
                         ArrayList<Report> Reports) {
        super(context, listLayout, Reports);
        this.listLayout = listLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Report report = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(listLayout, parent, false);
        }
        // Lookup view for data population
        TextView reportStatus = (TextView) convertView.findViewById(R.id.report_status);
        TextView distance = (TextView) convertView.findViewById(R.id.distance);
        TextView numberScanners = (TextView) convertView.findViewById(R.id.numberScanners);
        TextView reportAddress = (TextView) convertView.findViewById(R.id.report_address);
        TextView reportTime = (TextView) convertView.findViewById(R.id.report_time);

        // Populate the data into the template view using the data object

        reportStatus.setText(report.getStatus());
        distance.setText(report.getDistanceStr() + "Km");
        numberScanners.setText(report.getAvailableScannersStr());
        reportAddress.setText(report.getAddress());
        reportTime.setText(report.getStartTime());
        // Return the completed view to render on screen
        return convertView;
    }

}