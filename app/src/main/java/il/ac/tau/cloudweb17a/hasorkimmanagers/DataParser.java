package il.ac.tau.cloudweb17a.hasorkimmanagers;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataParser {

    private static final String TAG = DataParser.class.getSimpleName();


    static List<Integer> getDistances(String jsonData) {

        List<Integer> distances = new ArrayList<>();

        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray jsonArray = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject element = (JSONObject) (jsonArray.get(i));
                int distance = element.getJSONObject("distance").getInt("value");
                distances.add(Math.round(distance / 1000));
            }

            return distances;

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}