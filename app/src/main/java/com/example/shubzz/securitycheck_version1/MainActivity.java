package com.example.shubzz.securitycheck_version1;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class MainActivity extends AppCompatActivity {

    public Map<String, String> myMap = new HashMap<>();
    public Map<String, String> guardA = new HashMap<>();
    public Map<String, String> guardB = new HashMap<>();
    public Map<String, String> guardC = new HashMap<>();
    public int flag = 0;
    private int time;
    private String guardName="";
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Calendar calobj = Calendar.getInstance();
    String location="";


    private BroadcastReceiver broadcastReceiver;
    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    Log.d("asd",""+intent.getExtras().get("coordinates"));

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }
    }
    private void enable_buttons() {

        Intent i =new Intent(getApplicationContext(),GPS_Service.class);
        startService(i);

    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }




    public String getMacId()
    {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected())
        {
            return null;
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.v("wifi",wifiInfo.getBSSID());
        return wifiInfo.getBSSID();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

        //start gps service
        if(!runtime_permissions())
            enable_buttons();

//        Button bun = (Button) findViewById(R.id.bun);
//        bun.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                Intent i = new Intent(MainActivity.this,TakeAttendance.class);
//                startActivity(i);
//            }
//        });

        ImageView img2 = (ImageView) findViewById(R.id.imageView);
        img2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(flag == 0)
                {
                    Toast.makeText(getApplicationContext(),"Not Connected to a Guard Post",Toast.LENGTH_SHORT).show();
                }
                else if(flag == 1)
                {
                    // find the guard
                    String s = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
                    s = s.substring(0,2);
                    time = Integer.parseInt(s);
                    Log.v("fuck hour", String.valueOf(time));

                    if(time >= 0 && time < 8)
                    {
                        Log.v("fuck","inside");
                        guardName = guardC.get(location);
                    }
                    else if(time >= 8 && time < 16)
                    {
                        guardName = guardA.get(location);
                    }
                    else if(time >= 16 && time < 24)
                    {
                        guardName = guardB.get(location);
                    }

                    Log.v("fuck",guardName);

                    Intent i = new Intent(MainActivity.this,TakeAttendance.class);
                    i.putExtra("gaurdname",guardName);
                    i.putExtra("location",location);
                    startActivity(i);
                }

            }
        });



        //make map of csv file
        readCSV();

        readFromFirebase();

        //https://stackoverflow.com/questions/29533934/correct-way-to-run-a-continuously-called-method-in-own-thread?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

        new Thread(new Runnable()
        {
            TextView t = (TextView) findViewById(R.id.check);
            ImageView img = (ImageView) findViewById(R.id.imageView);
            TextView t2 = (TextView) findViewById(R.id.multiAutoCompleteTextView2);
            TextView t3 = (TextView) findViewById(R.id.multiAutoCompleteTextView);

            @Override
            public void run()
            {
                while(true)
                {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            String xyz = getMacId();

                            if(xyz != null && !xyz.isEmpty())
                            {
                                xyz = xyz.substring(0, xyz.length() - 1);

                                location = myMap.get(xyz);


                                if (myMap.containsKey(xyz))
                                {
                                    //here i am connected
                                    flag = 1;
                                    t.setText(location);
                                    t2.setText("CONNECTED");
                                    t3.setText("GOOD JOB");
                                    img.setImageResource(R.drawable.ellipse1);

                                }
                                else
                                {
                                    flag = 0;
                                    t.setText("No mapped location");
                                    t3.setText("Go to a GuardPost");
                                    t2.setText("DISCONNECTED");
                                    img.setImageResource(R.drawable.disconnected);
                                }
                            }
                            else
                            {
                                //this means he is not in the wifi zone

                                flag = 0;
                                t2.setText("DISCONNECTED");
                                t3.setText("Go to a GuardPost");
                                t.setText("NO Location");
                                img.setImageResource(R.drawable.disconnected);
                            }


                        }
                    });

                }
            }
        }).start();



    }

    private void readFromFirebase()
    {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.show();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("timetable").child("gaurdPostings");

        databaseReference.addChildEventListener(new ChildEventListener()

        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot2, String s2)
            {
                mappingGuard g = dataSnapshot2.getValue(mappingGuard.class);
                Log.v("fuck you",g.getGAURD1());
                guardA.put(g.getPOST(),g.getGAURD1());
                guardB.put(g.getPOST(),g.getGaurd2());
                guardC.put(g.getPOST(),g.getGAURD3());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot2, String s2)
            {


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot2) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot2, String s2) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError2)
            {

            }
        });
        progressDialog.dismiss();
    }

    private void readCSV()
    {
        InputStream is = getResources().openRawResource(R.raw.mapping);

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";

        try
        {
            while((line = reader.readLine()) != null)
            {
                String[] tokens = line.split(",");

                String t = tokens[4].substring(0, tokens[4].length() - 1);
                myMap.put(t,tokens[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
