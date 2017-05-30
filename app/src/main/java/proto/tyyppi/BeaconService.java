package proto.tyyppi;

import android.app.Application;
import android.util.Log;

import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

/**
 * Created by otto on 30.5.2017.
 */

public class BeaconService extends Application {

    private BeaconManager beaconManager;
    private String bmajori;

    private static final BeaconRegion allBeaconsRegion = new BeaconRegion("Beacons with default Estimote UUID",
            UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());

        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
            @Override
            public void onEnteredRegion(BeaconRegion region, List<Beacon> list) {

                Beacon beaconM = list.get(0);;
                bmajori = String.valueOf(beaconM.getMajor());
                // TODO: update the UI here
                Log.d("pls", "majori: " + bmajori);

            }

            @Override
            public void onExitedRegion(BeaconRegion region) {
                // ...
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
}
