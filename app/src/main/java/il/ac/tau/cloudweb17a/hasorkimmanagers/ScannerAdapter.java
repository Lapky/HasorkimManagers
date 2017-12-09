package il.ac.tau.cloudweb17a.hasorkimmanagers;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
        TextView scannerName = (TextView) convertView.findViewById(R.id.scanner_name);
        TextView distance = (TextView) convertView.findViewById(R.id.scanner_distance);

        // Populate the data into the template view using the data object

        scannerName.setText(scanner.getName());
        distance.setText(scanner.getDistanceStr() + "Km");
        // Return the completed view to render on screen
        return convertView;
    }

}