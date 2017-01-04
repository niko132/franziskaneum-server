package de.franziskaneum;

import android.content.Intent;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import de.franziskaneum.vplan.VPlan;
import de.franziskaneum.vplan.VPlanManager;
import de.franziskaneum.vplan.VPlanNotificationManager;

import static de.franziskaneum.vplan.VPlanNotificationManager.ACTION_NEW_VPLAN_AVAILABLE;

/**
 * Created by Niko on 22.12.2016.
 */

public class FranzFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String from = remoteMessage.getFrom();

        if (from.equalsIgnoreCase("/topics/vplan")) {
            Map<String, String> data = remoteMessage.getData();
            String key = "new_vplan_available";
            if (data.containsKey(key) && data.get(key).equalsIgnoreCase("true")) {
                VPlanManager.getInstance().getVPlanAsync(VPlanManager.Mode.IF_MODIFIED, new FranzCallback() {
                    @Override
                    public void onCallback(int status, Object... objects) {
                        if (Status.OK == status && objects.length > 1 && objects[1] != null) {
                            VPlan vplan = (VPlan) objects[1];

                            Intent broadcastIntent = new Intent(ACTION_NEW_VPLAN_AVAILABLE);
                            broadcastIntent.putExtra(VPlan.EXTRA_VPLAN, vplan);
                            sendBroadcast(broadcastIntent);

                            VPlanNotificationManager.getInstance().makeNotificationAsync(vplan);
                        } else
                            VPlanNotificationManager.getInstance().makeNotificationAsync(VPlanNotificationManager.Mode.DOWNLOAD);
                    }
                });
            }
        }
    }
}
