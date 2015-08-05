package br.org.igarape.igarapetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import br.org.igarape.igarapetest.services.LocationService;

public class MainActivity extends AppCompatActivity {

    Context mContext;
    LocalBroadcastManager mBroadcastManager;

    private TextView tv_lat;
    private TextView tv_lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getBaseContext();
        mBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        registerBroadcasts();

        tv_lat = (TextView) findViewById(R.id.tv_lat);
        tv_lng = (TextView) findViewById(R.id.tv_lng);

        startService(new Intent(MainActivity.this, LocationService.class));
        Toast.makeText(this, "Attempting to get location...", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void registerBroadcasts()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocalBroadcastDictionary.ACTION_NEW_LOCATION);
        mBroadcastManager.registerReceiver(mLocBroadcastReceiver, filter);
    }

    private void unregisterBroadcasts()
    {
        mBroadcastManager.unregisterReceiver(mLocBroadcastReceiver);
    }

    @Override
    protected void onDestroy()
    {
        unregisterBroadcasts();

        super.onDestroy();
    }

    public BroadcastReceiver mLocBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent i)
        {
            String action = i.getAction();

            if (action.equals(LocalBroadcastDictionary.ACTION_NEW_LOCATION))
            {
                Parcelable p = i.getParcelableExtra(LocalBroadcastDictionary.EXTRA_NEW_LOCATION);
                if(p != null)
                {
                    tv_lat.setText(" " + ((Location) p).getLatitude());
                    tv_lng.setText(" " + ((Location) p).getLongitude());
                }

                //You can get other available Location related data from the Location object
            }
        }
    };
}
