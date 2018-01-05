package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.net.Uri;
import android.util.Log;

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


    //Activity activity =this;
    //getDistanceRequest(locationList,Lat,Long,getString(R.string.google_maps_key)).enqueue(new Callback() {
    //    @Override
    //    public void onResponse(Call call, Response response) throws IOException {
    //        ResponseBody responseBody = response.body();
    //        final String distance = getDistances(responseBody.string());
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



    static public Call getDistanceRequest(List<LatLong> locationList, long currLatitude, long currLongitude, String key){

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
                .appendQueryParameter("origins", (Double.toString(currLatitude) + "," + Double.toString(currLongitude)))
                .appendQueryParameter("destinations", nearVetsID.toString())
                .appendQueryParameter("mode","driving")
                .appendQueryParameter("language", "iw")
                .appendQueryParameter("key", key);  //getString(R.string.google_maps_key)

        String currentUrlDistances = urlMaps.build().toString();
        //Log.d(TAG, currentUrlDistances);

        Request request=new Request.Builder()
                .url(currentUrlDistances)
                .build();

        return client.newCall(request);
    }

    public static String getDistances(String jsonData) {
        String distance="";
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray=null;
            if(jsonObject.getJSONArray("rows") !=null &&jsonObject.getJSONArray("rows").getJSONObject(0)!=null){
                jsonArray = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
            }
            Log.d(TAG,jsonArray.toString());
            if(jsonArray!=null &&((JSONObject) jsonArray.get(0) !=null)){
                JSONObject element = (JSONObject) jsonArray.get(0);
                if(element.getString("status")!=null&&element.getString("status").equals("ZERO_RESULTS"))
                    Log.d(TAG,"couldn't find any results");
                    return null;
                    //distance = element.getJSONObject("duration").getString("value");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return distance;
    }
}


