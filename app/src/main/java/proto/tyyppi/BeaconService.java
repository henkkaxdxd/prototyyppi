package proto.tyyppi;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class BeaconService extends Application {

    private String groupID = "Not Set";
    private String locationID = "Not Set";
    private String beaconLight = "on";
    private BeaconManager beaconManager;
    private String bmajori;

    private static final BeaconRegion allBeaconsRegion = new BeaconRegion("Beacons with default Estimote UUID",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());

        SharedPreferences sharedPref= getSharedPreferences("mypref", MODE_PRIVATE);
        groupID = sharedPref.getString("savedGroup", groupID);
        locationID = sharedPref.getString("savedLocation", locationID);
        beaconLight = sharedPref.getString("savedBeacon", beaconLight);

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> list) {

                Beacon beaconM = list.get(0);
                bmajori = String.valueOf(beaconM.getMajor());
                // TODO: update the UI here
                Log.d("pls", "majori: " + bmajori);
                saveStoff("major", bmajori);
                new BeaconService.JSONtask().execute("https://oven-sausage.herokuapp.com/add/1/"+bmajori+"/"+groupID+"/"+locationID);

                showNotification("Sup brah","(logged in)");

                beaconLight = "on";
                saveStoff("savedBeacon",beaconLight);
            }

            @Override
            public void onExitedRegion(BeaconRegion region) {
                new BeaconService.JSONtask().execute("https://oven-sausage.herokuapp.com/add/1/"+bmajori+"/"+null+"/"+null);
                saveStoff("major",null);
                Log.d("pls", "poistunut alueelta");

                showNotification("Bye brah","(banned)");

                beaconLight = "off";
                saveStoff("savedBeacon",beaconLight);
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(allBeaconsRegion);
                Log.d("pls", "start monitoring ");
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[] { notifyIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    public void saveStoff(String name, String addMajor){
        // Create object of SharedPreferences.
        SharedPreferences sharedPref= getApplicationContext().getSharedPreferences("mypref", MODE_PRIVATE);
        //now get Editor
        SharedPreferences.Editor editor= sharedPref.edit();
        //put your value
        editor.putString(name, addMajor);
        //commits your edits
        editor.commit();
    }

    public class JSONtask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                Log.d("pls", "connect ");

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = " ";
                while ((line = reader.readLine())!= null){
                    buffer.append(line);
                }
                buffer.toString();
                Log.d("pls", "connect2");

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("pls", "onPostExecute");
            String testi;
            if(result != null){
            testi = result;
            Log.d("pls", testi);
            }
        }
    }
}
