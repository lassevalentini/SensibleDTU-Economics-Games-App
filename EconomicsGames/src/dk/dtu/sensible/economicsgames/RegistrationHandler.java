package dk.dtu.sensible.economicsgames;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import dk.dtu.sensible.economicsgames.R;
import dk.dtu.sensible.economicsgames.util.UrlHelper;

//import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.message.BasicNameValuePair;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.conn.scheme.Scheme;
//import org.apache.http.conn.scheme.SchemeRegistry;
//import org.apache.http.conn.ssl.SSLSocketFactory;
//import org.apache.http.conn.ssl.X509HostnameVerifier;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.impl.conn.SingleClientConnManager;
//import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
//import javax.net.ssl.TrustManager;
//import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


/**
 * Created with IntelliJ IDEA.
 * User: piotr
 * Date: 7/24/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class RegistrationHandler extends Service {

    private static boolean started = false;
    private int mStartMode = Service.START_STICKY;
    private static final String TAG = "AUTH_RegistrationHandler";

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTimeMs";

    private static final String SHOW_REGISTRATION_REMINDER = "dk.dtu.imm.sensibleeconomics.show_registration_reminder";

    public static final String SHARED_PREFERENCES_NAME = "sensible_auth";
    public static final String PROPERTY_SENSIBLE_TOKEN = "sensible_token";
    private static final String PROPERTY_SENSIBLE_REFRESH_TOKEN = "sensible_refresh_token";
    private static final String PROPERTY_SENSIBLE_TOKEN_TIMEOUT = "sensible_token_timeout";
    public static final String PROPERTY_SENSIBLE_CODE = "sensible_code";
    private static final int NOTIFICATION_ID = 156315435;
    public static final long DAY = 24 * 60 * 60 * 1000;

    /**
     * Default lifespan (7 days) of a reservation until it is considered expired.
     */
    public static final long REGISTRATION_EXPIRY_TIME_MS = 7 * DAY;




    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    Context context;

    String regid;

    Handler mHandler = new Handler();

    BroadcastReceiver br = null;
    PendingIntent pi;
    AlarmManager am;

    public enum SensibleRegistrationStatus {
        NOT_REGISTERED_NO_CODE, NOT_REGISTERED_HAS_CODE, REGISTERED_EXPIRED, REGISTERED
    }
    
    public static Object registrationLock = new Object();

    private static final String CODE_TO_TOKEN_URL = SharedConstants.DOMAIN_URL + "sensible-dtu/authorization_manager/connector_economics/auth/token/";
    
    private static final String REFRESH_TOKEN_URL = SharedConstants.DOMAIN_URL + "sensible-dtu/authorization_manager/connector_economics/auth/refresh_token/";
    private static final String SET_GCM_ID_URL = SharedConstants.DOMAIN_URL + "sensible-dtu/authorization_manager/connector_economics/auth/gcm/";



    public IBinder onBind(Intent intent) {
        return null;
    }

    public static SharedPreferences getAuthPreferences(Context context)  {
        return context.getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!started) {
            started = true;
            Log.d(TAG, "STARTED!");
            context = getApplicationContext();

            regid = getRegistrationId(context);
            Log.d(TAG, "RegId: " + regid);
            if (regid.length() == 0) {
                Log.d(TAG, "will register in the background");
                registerBackground();
            } else {
                Log.d(TAG, "Already registered at GCM");
                handleRegistration();
            }
            //setGcmId();
        } else {
            Log.d(TAG, "already running, not starting");
        }
        return mStartMode;
    }

    private void handleRegistration() {

        SensibleRegistrationStatus status = getSensibleRegistrationStatus(context);
        Log.d(TAG, "Handling registration: " + status);
        switch (status) {
            case NOT_REGISTERED_NO_CODE:
                setupNotification();
                startAuthActivity();
                started = false;
                break;
            case NOT_REGISTERED_HAS_CODE:
                obtainToken();
                break;
            case REGISTERED_EXPIRED:
                refreshToken();
                break;
            case REGISTERED:
                cancelNotification();
                started = false;
                break;
        }
    }

    private void startAuthActivity() {
        Intent dialogIntent = new Intent(getBaseContext(), AuthActivity.class);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(dialogIntent);
    }
    private void setupNotification() {
        if (br == null) {
            br = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    sendNotification("Register your phone!",
                            "Touch to register your phone with the "+SharedConstants.STUDY_NAME+" experiment");
                }
            };
            registerReceiver(br, new IntentFilter(SHOW_REGISTRATION_REMINDER) );
            pi = PendingIntent.getBroadcast(this, 0, new Intent(SHOW_REGISTRATION_REMINDER),
                    PendingIntent.FLAG_CANCEL_CURRENT);
            am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        }
        sendNotification("Register your phone!",
                "Touch to register your phone with the "+SharedConstants.STUDY_NAME+" experiment");
        am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pi);
    }

    private void sendNotification(String title, String msg) {
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, AuthActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.green_logo5)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static SensibleRegistrationStatus getSensibleRegistrationStatus(Context context) {
        long timeout = getAuthPreferences(context).getLong(PROPERTY_SENSIBLE_TOKEN_TIMEOUT, 0l);
        if (timeout == 0l) {
            if (getSensibleCode(context).length() > 0) {
                return SensibleRegistrationStatus.NOT_REGISTERED_HAS_CODE;
            } else {
                return SensibleRegistrationStatus.NOT_REGISTERED_NO_CODE;
            }
        } else if (System.currentTimeMillis() + DAY > timeout) {
            Log.d(TAG, "Expiring in: " + (timeout - System.currentTimeMillis()));
            return SensibleRegistrationStatus.REGISTERED_EXPIRED;
        }
        else  {
            Log.d(TAG, "Expiring in: " + (timeout - System.currentTimeMillis()));
            return SensibleRegistrationStatus.REGISTERED;
        }
    }

    public static String getSensibleToken(Context context) {
        final SharedPreferences systemPrefs = getAuthPreferences(context);
        return systemPrefs.getString(PROPERTY_SENSIBLE_TOKEN, "");
    }

    private String getSensibleRefreshToken() {
        final SharedPreferences systemPrefs = getAuthPreferences(context);
        return systemPrefs.getString(PROPERTY_SENSIBLE_REFRESH_TOKEN, "");
    }

    private String getSensibleCode() {
        final SharedPreferences systemPrefs = getAuthPreferences(context);
        Log.d(TAG, "Code from shared prefs: " + systemPrefs.getString(PROPERTY_SENSIBLE_CODE, ""));
        return systemPrefs.getString(PROPERTY_SENSIBLE_CODE, "");
    }

    private static String getSensibleCode(Context context) {
        return getAuthPreferences(context).getString(PROPERTY_SENSIBLE_CODE, "");
    }


    public String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.length() == 0) {
            Log.v(TAG, "Registration not found");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion || isGcmRegistrationExpired(context)) {
            Log.v(TAG, "App version changed or registration expired");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(AuthActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean isGcmRegistrationExpired(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        // checks if the information is not stale
        long expirationTime =
                prefs.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1);
        return System.currentTimeMillis() > expirationTime;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration id, app versionCode, and expiration time in the
     * application's shared preferences.
     */
    private void registerBackground() {
        new AsyncTask<Void, Void, String>() {
            //@Override
            protected void onPostExecute(String msg) {
                Log.v(TAG, msg);
                //mDisplay.append(msg + "\n");
            }

            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Log.d(TAG, "doing it in the background...");
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(Secret.SENDER_ID);
                    Log.d(TAG, "Device registered, registration id=" + regid);
                    msg = "Device registered, registration id=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the message
                    // using the 'from' address in the message.

                    // Save the regid - no need to register again.
                    setRegistrationId(context, regid);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            SensibleRegistrationStatus status = getSensibleRegistrationStatus(context);
                            if (status == SensibleRegistrationStatus.REGISTERED) {
                                setGcmId();
                            } else {
                                handleRegistration();
                            }
                        }
                    });
                    //mHandler.post(new ServerRegistrar(context, regid));
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration id, app versionCode, and expiration time in the
     * application's {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration id
     */
    private void setRegistrationId(Context context, String regId) {

        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.v(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        long expirationTime = System.currentTimeMillis() + REGISTRATION_EXPIRY_TIME_MS;

        Log.v(TAG, "Setting registration expiry time to " +
                new Timestamp(expirationTime));
        editor.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, expirationTime);
        editor.commit();
    }


    private void obtainToken() {
        Log.d(TAG, "obtaining Token");
        postData(CODE_TO_TOKEN_URL, getSensibleCode());
    }

    private void refreshToken() {
        Log.d(TAG, "refreshing Token");
        postData(REFRESH_TOKEN_URL, getSensibleRefreshToken());
    }

    private void setGcmId() {
        Log.d(TAG, "settings Gcm ID Token");
        postData(SET_GCM_ID_URL, getSensibleToken(context));
    }

    private void postData(String api_url, String extra_param) {
        new AsyncTask<String, Integer, Double>() {

            @Override
            protected Double doInBackground(String... strings) {
            	
                try {
                    Map<String, String> postData = new HashMap<String, String>();
                    
                    postData.put("client_id", Secret.CLIENT_ID);
                    postData.put("client_secret", Secret.CLIENT_SECRET);
                    
                    TelephonyManager tm = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
                    String imei = tm.getDeviceId();
                    postData.put("device_id", imei);
                    
                    Log.d(TAG, "Params: " + strings[0] + ", " + strings[1]);
                    if (strings[0].equals(CODE_TO_TOKEN_URL)) {
                    	postData.put("code", strings[1]);
                    } else if (strings[0].equals(REFRESH_TOKEN_URL)) {
                    	postData.put("refresh_token", strings[1]);
                    } else if (strings[0].equals(SET_GCM_ID_URL)) {
                    	postData.put("gcm_id", getRegistrationId(context));
                    	postData.put("bearer_token", strings[1]);
                    }

                    processResponse(strings[0], UrlHelper.post(strings[0], postData));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  
                } catch (ClientProtocolException e) {
                    e.printStackTrace();  
                } catch (IOException e) {
                    e.printStackTrace();  
                } finally {
                	synchronized (registrationLock) {
                		registrationLock.notifyAll();
					}
                }
                return null; 
            }
        }.execute(api_url, extra_param, null);

    }
    
    
    private void cancelNotification() {
        if (am != null) {
            am.cancel(pi);
        }
        NotificationManager mNotificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    private void processResponse(String api_url, String response) {
        //final SharedPreferences systemPrefs = MainPipeline.getSystemPrefs(context);
        final SharedPreferences systemPrefs =
                getAuthPreferences(context);
        try {
            JSONObject o = new JSONObject(response);
            if (o.has("error")) {
                Log.e(TAG, "Response contains error " + o.toString());
                if (response.contains("No such code") || response.contains("code is expired")) {
                    Log.e(TAG, "Code not found, invalidate code");
                    SharedPreferences.Editor editor = systemPrefs.edit();
                    editor.putString(PROPERTY_SENSIBLE_CODE, "");
                    editor.commit();
                    
                    handleRegistration();
                    return;
                }
            } else {
                String token = o.getString("access_token");
                long expiry = System.currentTimeMillis() + 7 * DAY;
                String refresh_token = o.getString("refresh_token");

                if (token != null) {
                    cancelNotification();
                    SharedPreferences.Editor editor = systemPrefs.edit();
                    editor.putString(PROPERTY_SENSIBLE_CODE, "");
                    editor.putString(PROPERTY_SENSIBLE_TOKEN, token);
                    editor.putString(PROPERTY_SENSIBLE_REFRESH_TOKEN, refresh_token);
                    editor.putLong(PROPERTY_SENSIBLE_TOKEN_TIMEOUT, expiry);
                    editor.commit();

                    //Add to Funf config
                    //MainPipeline.getMainConfig(context).edit().setSensibleAccessToken(token).commit();
                    started = false;
                }
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private StringBuilder inputStreamToString(InputStream is) {
        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        // Read response until the end
        try {
            while ((line = rd.readLine()) != null) {
                Log.d(TAG, line);
                total.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Return full string
        return total;
    }

}
