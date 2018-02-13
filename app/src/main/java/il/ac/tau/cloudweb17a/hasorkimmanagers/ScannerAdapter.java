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


    public ScannerAdapter(Context context, ArrayList<Scanner> Scanners) {
        super(context, 0, Scanners);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Scanner scanner = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scanner_list_item, parent, false);
        }
        // Lookup view for data population
        LinearLayout scannerLayout = convertView.findViewById(R.id.scannerLayout);
        TextView scannerName = convertView.findViewById(R.id.scanner_name);
        TextView scannerId = convertView.findViewById(R.id.scannerId);
        TextView distance = convertView.findViewById(R.id.scanner_distance);
        Button sendScanner = convertView.findViewById(R.id.sendScanner);

        scannerName.setText(scanner.getName());
        scannerId.setText(scanner.getUserId());
        distance.setText(scanner.getDuration());
        sendScanner.setBackgroundColor(getContext().getResources().getColor(R.color.setScannerColor));
        // Return the completed view to render on screen

        if (scanner.isAssignedScanner()) {
            sendScanner.setBackgroundColor(getContext().getResources().getColor(R.color.deleteScannerColor));
            sendScanner.setText(R.string.scanner_was_chosen);
        }

        return convertView;
    }
}