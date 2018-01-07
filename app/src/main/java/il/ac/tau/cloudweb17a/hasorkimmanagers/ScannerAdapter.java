package il.ac.tau.cloudweb17a.hasorkimmanagers;


import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ScannerAdapter extends ArrayAdapter<Scanner> {
    private final int listLayout;

    public ScannerAdapter(Context context,
                         int listLayout,
                         ArrayList<Scanner> Scanners) {
        super(context, listLayout, Scanners);
        this.listLayout = listLayout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Scanner scanner = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(listLayout, parent, false);
        }
        // Lookup view for data population
        LinearLayout scannerLayout = (LinearLayout) convertView.findViewById(R.id.scannerLayout);
        TextView scannerName = (TextView) convertView.findViewById(R.id.scanner_name);
        TextView scannerId = (TextView) convertView.findViewById(R.id.scannerId);
        TextView distance = (TextView) convertView.findViewById(R.id.scanner_distance);
        Button sendScanner = (Button) convertView.findViewById(R.id.sendScanner);

        scannerName.setText(scanner.getUserId());
        scannerId.setText(scanner.getUserId());
        distance.setText(scanner.getDuration() + " Min");
        // Return the completed view to render on screen

        if (scanner.getIsAssignedScanner()){
            sendScanner.setText("סורק נבחר");
            scannerLayout.setBackgroundColor(Color.YELLOW);
        }
        return convertView;
    }


}