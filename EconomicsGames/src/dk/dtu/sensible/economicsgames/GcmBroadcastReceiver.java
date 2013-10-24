package dk.dtu.sensible.economicsgames;

/**
 * Created with IntelliJ IDEA.
 * User: piotr
 * Date: 7/18/13
 * Time: 4:27 PM
 * To change this template use File | Settings | File Templates.
 */

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import dk.dtu.sensible.economicsgames.R;

/**
 * Handling of GCM messages.
 */
public class GcmBroadcastReceiver extends BroadcastReceiver {
    static final String TAG = "AUTH_AuthActivity_broadcastReceiver";
    public static final String EVENT_MSG_RECEIVED = "EVENT_MSG_RECEIVED";
    
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;
    Context ctx;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received: " + intent.getExtras().toString());
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        ctx = context;
        Bundle extras = intent.getExtras();
        String messageType = gcm.getMessageType(intent);
        
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
//        	Intent notifyIntent = new Intent(EVENT_MSG_RECEIVED);
//        	notifyIntent.putExtras(extras);
//        	LocalBroadcastManager.getInstance(context).sendBroadcast(notifyIntent);
        	
        	// TODO: Add game to db, get GameActivity working with this
        	// TODO: Change classes based on the extras in the gcm message?
        	// TODO: add "code won" notification and layout
        	String msgType = intent.getExtras().getString("type");
            Log.d(TAG, "msg-type: " + msgType);
        	if (msgType.equalsIgnoreCase("economics-pgg-init")) {
        		sendNotification(extras.getString("title"), extras.getString("body"), GameActivity.class);
        		
        	} else if (msgType.equalsIgnoreCase("auth")) {
	            sendNotification(extras.getString("title"), "", AuthActivity.class);
	            
        	}
        	
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
        	// 
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        	// Not gonna happen - we are not using an xmpp server 
        }
        setResultCode(Activity.RESULT_OK);
    }

    // Put the GCM message into a notification and post it.
    private void sendNotification(String title, String msg, Class activity) {
        mNotificationManager = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
                new Intent(ctx, activity), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(ctx)
                        .setSmallIcon(R.drawable.green_logo5)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
