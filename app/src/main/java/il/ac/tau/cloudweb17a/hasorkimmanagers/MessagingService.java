package il.ac.tau.cloudweb17a.hasorkimmanagers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = MessagingService.class.getSimpleName();
    private double currLatitude;
    private double currLongitude;
    private String radius;
    private String lat;
    private String lon;
    private String address;
    private static final OkHttpClient client = new OkHttpClient();


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData() != null) {
            lat = remoteMessage.getData().get("lat");
            lon = remoteMessage.getData().get("long");
            address = remoteMessage.getData().get("address");

            Log.d(TAG, "Message Data lat: " + lat);
            Log.d(TAG, "Message Data long: " + lon);
            Log.d(TAG, "Message Data address: " + address);
        } else {
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        radius = prefs.getString("radius", "");

        if (prefs.getBoolean("notifications_new_message", false)) {
            if (prefs.getBoolean("notifications_radius", false)) {
                if (IsLocationPermission()) {
                    notifyIfInRadius();
                }
            } else {
                notifyToUser();
            }
        }
    }

    private void checkIfInRadius() {
        Uri.Builder urlMaps = new Uri.Builder()
                .scheme("https")
                .authority("maps.googleapis.com")
                .appendPath("maps")
                .appendPath("api")
                .appendPath("distancematrix")
                .appendPath("json")
                .appendQueryParameter("origins", (currLatitude + "," + currLongitude))
                .appendQueryParameter("destinations", (lat + "," + lon))
                .appendQueryParameter("mode", "driving")
                .appendQueryParameter("language", "iw")
                .appendQueryParameter("key", getString(R.string.google_places_key));

        Request request = new Request.Builder()
                .url(urlMaps.build().toString())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful())
                        throw new IOException("Unexpected code " + response);

                    String returnedString = responseBody.string();
                    List<Integer> distances = DataParser.getDistances(returnedString);

                    if (distances != null) {
                        if (distances.get(0) <= Integer.valueOf(radius)) {
                            notifyToUser();
                        }
                    }
                }
            }
        });
    }

    private void notifyToUser() {
        Notification n = new Notification.Builder(this)
                .setContentTitle("התקבל דיווח חדש")
                .setContentText("דיווח חדש בכתובת " + address)
                .setSmallIcon(R.drawable.dog_icon)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setPriority(Notification.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, n);
        }
    }


    private boolean IsLocationPermission() {
        Context context = this.getApplicationContext();

        return (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void notifyIfInRadius() {
        try {
            FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful() && (task.getResult() != null)) {
                        Location mLastKnownLocation = task.getResult();
                        currLatitude = mLastKnownLocation.getLatitude();
                        currLongitude = mLastKnownLocation.getLongitude();
                        checkIfInRadius();
                    } else {
                        Log.e(TAG, "Exception: " + task.getException());
                    }
                }
            });

        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
