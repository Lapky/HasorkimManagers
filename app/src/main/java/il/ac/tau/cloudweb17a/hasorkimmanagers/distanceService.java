package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


/**
 * Created by hen on 04/01/2018.
 */

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class distanceService {
    private static final String TAG = "distance_service";
    private static final OkHttpClient client = new OkHttpClient();
    public static LatLong myLatLong= new LatLong();


    //Activity activity =this;
    //getDistanceRequest(locationList,Lat,Long,getString(R.string.general_key)).enqueue(new Callback() {
    //    @Override
    //    public void onResponse(Call call, Response response) throws IOException {
    //        ResponseBody responseBody = response.body();
    //        final String distance = parseJSON(responseBody.string());
    //        activity.runOnUiThread(
    //            new Runnable() {
    //                @Override
    //                public void run() {
    //                    TextView dText = activity.findViewById(R.id.dText);
    //                    dText.setText(String.valueOf(distance));
    //                }
    //            }
    //        );
    //    }
    //    @Override
    //    public void onFailure(Call call, IOException e) {
    //        //do something
    //    }
    // });



    static public Call getDistanceRequest(List<LatLong> locationList, String key){

        int listSize = locationList.size();
        StringBuilder nearVetsID = new StringBuilder();
        for (int i = 0; i < listSize; i++) {
            nearVetsID.append(String.valueOf(locationList.get(i).Lat)+"," + String.valueOf(locationList.get(i).Long));

            if (i < (listSize - 1))
                nearVetsID.append("|");
        }

        Uri.Builder urlMaps = new Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("distancematrix")
                .appendPath("json")
                .appendQueryParameter("origins", (Double.toString(myLatLong.Lat) + "," + Double.toString(myLatLong.Long)))
                .appendQueryParameter("destinations", nearVetsID.toString())
                .appendQueryParameter("mode","driving")
                .appendQueryParameter("language", "iw")
                .appendQueryParameter("key", key);  //getString(R.string.general_key)

        String currentUrlDistances = urlMaps.build().toString();
        //Log.d(TAG, currentUrlDistances);

        Request request=new Request.Builder()
                .url(currentUrlDistances)
                .build();

        Log.d(TAG, Double.toString(myLatLong.Lat) + "," + Double.toString(myLatLong.Long)+ "->" +locationList.get(0).Lat+","+locationList.get(0).Long);
        return client.newCall(request);
    }


    public static String[] parseJSON(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray=null;
            if(jsonObject.getJSONArray("rows") !=null &&jsonObject.getJSONArray("rows").getJSONObject(0)!=null){
                jsonArray = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
            }

            Log.d(TAG,jsonArray.toString());

            if(jsonArray!=null &&((JSONObject) jsonArray.get(0) !=null)){
                JSONObject element = (JSONObject) jsonArray.get(0);
                if(element.getString("status")!=null&&element.getString("status").equals("ZERO_RESULTS")) {
                    Log.d(TAG, "couldn't find any results");
                    return null;
                }
                else{
                    String[] response = new String[3];
                    response[0]= element.getJSONObject("distance").getString("text");
                    response[1]= element.getJSONObject("duration").getString("text");
                    response[2]= element.getJSONObject("distance").getString("value");
                    return response;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }




    public static void getDeviceLocation(final ReportListActivity.MyCallBackClass showList, FusedLocationProviderClient mFusedLocationProviderClient, Activity activity) {
        try {
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {

                    if (task.isSuccessful() && (task.getResult() != null)) {
                        // Set the map's camera position to the current location of the device.
                        Location mLastKnownLocation = task.getResult();
                        myLatLong.Lat = mLastKnownLocation.getLatitude();
                        myLatLong.Long = mLastKnownLocation.getLongitude();

                        Log.d(TAG, "get curr location: "+ Double.toString(myLatLong.Lat) + "," + Double.toString(myLatLong.Long));
                    }

                    showList.execute();
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}


