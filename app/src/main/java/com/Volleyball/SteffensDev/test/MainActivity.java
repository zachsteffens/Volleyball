package com.Volleyball.SteffensDev.test;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable;


public class MainActivity extends FragmentActivity {


    ///*******************Variables and settings for Google Cloud Messaging
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //WCF Service endpoint
    private final static String VOLLEYBALL_SERVICE_URI = "http://10.0.2.2/AndroidWCFService/VolleyballSvc.svc/GetUserInfo/?key=";


    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     * https://console.developers.google.com/
     */
    String SENDER_ID = "1009765801874";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";



    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    Context context;

    String regid;
    //////////////////////////end of variables and settings for Google Cloud Messaging

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    CustomDrawerAdapter adapter;

    List<DrawerItem> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //setup for cloud messaging
        context = getApplicationContext();

        if(checkPlayServices()){
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if(regid.isEmpty()){
                registerInBackground();
            }

        } else {
            Log.i(TAG,"No valid Google Play Services APK found");
        }

        //end of setup for cloud messaging



        //////////////testing notifications
        // prepare intent which is triggered if the
// notification is selected

        /*Intent intent = new Intent(this, NotificationReceiver.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);*/

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

// build notification
// the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("New mail from " + "test@gmail.com")
                .setContentText("Subject")
                .setSmallIcon(R.drawable.ic_action_search)
                .setContentIntent(contentIntent)
                .setAutoCancel(true).build();
                //.addAction(R.drawable.icon, "Call", pIntent)
                //.addAction(R.drawable.icon, "More", pIntent)
                //.addAction(R.drawable.icon, "And more", pIntent).build();


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);




        ///////////////end testing notifications

        // Initializing
        dataList = new ArrayList<DrawerItem>();
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        //dataList.add(new DrawerItem("Message", R.drawable.ic_action_email));
        //dataList.add(new DrawerItem("Likes", R.drawable.ic_action_good));
        //dataList.add(new DrawerItem("Games", R.drawable.ic_action_gamepad));
        //dataList.add(new DrawerItem("Lables", R.drawable.ic_action_labels));
        //dataList.add(new DrawerItem("Search", R.drawable.ic_action_search));
        //dataList.add(new DrawerItem("Cloud", R.drawable.ic_action_cloud));
        //dataList.add(new DrawerItem("Camara", R.drawable.ic_action_camera));
        //dataList.add(new DrawerItem("Video", R.drawable.ic_action_video));
        dataList.add(new DrawerItem("Groups", R.drawable.ic_action_group));
        //dataList.add(new DrawerItem("Import & Export",
         //       R.drawable.ic_action_import_export));
        //dataList.add(new DrawerItem("About", R.drawable.ic_action_about));
        dataList.add(new DrawerItem("Settings", R.drawable.ic_action_settings));
        //dataList.add(new DrawerItem("Help", R.drawable.ic_action_help));

        adapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(adapter);

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open,
                R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            SelectItem(0);
        }



        new AsyncTask<Void, Void, String> () {
            @Override
            protected String doInBackground(Void...params) {
                DefaultHttpClient client = new DefaultHttpClient();

                HttpGet request = new HttpGet(VOLLEYBALL_SERVICE_URI + "123456");

                request.setHeader("Accept","application/json");
                request.setHeader("Content-type", "application/json");

                HttpEntity entity = null;
                HttpResponse response = null;
                try {
                    response = client.execute(request);
                    entity = response.getEntity();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(entity.getContentLength() !=0){
                    Reader VolleyballReader = null;
                    try {
                        VolleyballReader = new InputStreamReader(entity.getContent());

                        char[] buffer = new char[(int) response.getEntity().getContentLength()];

                        VolleyballReader.read(buffer);
                        VolleyballReader.close();

                        String result = new String(buffer);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                }
                return "sent message";
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(), msg,
                        Toast.LENGTH_LONG).show();
                //mDisplay.append(msg + "\n");
            }

        }.execute(null,null,null);



        /*///////call echo server for push message
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "I'm Sorry Dave");
                    data.putString("my_action",
                            "com.google.android.gcm.demo.app.ECHO_NOW");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Toast.makeText(getApplicationContext(), msg,
                        Toast.LENGTH_LONG).show();
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);*/


    }

    public void SelectItem(int possition) {

        Fragment fragment = null;
        Bundle args = new Bundle();
        switch (possition) {
            case 0:
                fragment = new FragmentOne();
                args.putString(FragmentOne.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 1:
                fragment = new FragmentTwo();
                args.putString(FragmentTwo.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            /*case 2:
                fragment = new FragmentThree();
                args.putString(FragmentThree.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 3:
                fragment = new FragmentOne();
                args.putString(FragmentOne.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 4:
                fragment = new FragmentTwo();
                args.putString(FragmentTwo.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 5:
                fragment = new FragmentThree();
                args.putString(FragmentThree.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 6:
                fragment = new FragmentOne();
                args.putString(FragmentOne.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 7:
                fragment = new FragmentTwo();
                args.putString(FragmentTwo.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 8:
                fragment = new FragmentThree();
                args.putString(FragmentThree.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 9:
                fragment = new FragmentOne();
                args.putString(FragmentOne.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 10:
                fragment = new FragmentTwo();
                args.putString(FragmentTwo.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentTwo.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 11:
                fragment = new FragmentThree();
                args.putString(FragmentThree.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentThree.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;
            case 12:
                fragment = new FragmentOne();
                args.putString(FragmentOne.ITEM_NAME, dataList.get(possition)
                        .getItemName());
                args.putInt(FragmentOne.IMAGE_RESOURCE_ID, dataList.get(possition)
                        .getImgResID());
                break;*/
            default:
                break;
        }

        fragment.setArguments(args);
        FragmentManager frgManager = getSupportFragmentManager();
        frgManager.beginTransaction().replace(R.id.content_frame, fragment)
                .commit();

        mDrawerList.setItemChecked(possition, true);
        setTitle(dataList.get(possition).getItemName());
        mDrawerLayout.closeDrawer(mDrawerList);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return false;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            SelectItem(position);

        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();


    }


    /****
     * tutorial found at http://developer.android.com/google/gcm/client.html#sample-register
     */

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
    * or CCS to send messages to your app. Not needed for this demo since the
    * device sends upstream messages to a server that echoes back the message
    * using the 'from' address in the message.
            */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    private boolean checkPlayServices() {
        int playAvailable = isGooglePlayServicesAvailable(this);
        if(playAvailable != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(playAvailable)){
                GooglePlayServicesUtil.getErrorDialog(playAvailable,this,10).show();
            } else {
                Log.i(TAG, "This Device is not supported.");
                finish();
            }
            return false;
        }
        return true;

    }


    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }


}
