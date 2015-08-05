package br.org.igarape.igarapetest.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import br.org.igarape.igarapetest.LocalBroadcastDictionary;
import br.org.igarape.igarapetest.MainActivity;

public class LocationService extends Service implements LocationListener
{
    private Context mContext;
    private LocationManager mLm;

    private String mGPSProvider;
    private String mNetworkProvider;

    private Integer mProviderUpdateInterval = 2000;
    private Integer mMinumumGPSDistance = 0;

    private LocalBroadcastManager mLbm;
    private Location mLastRegisteredLocation;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent i, int flags, int startId)
    {
        mContext = LocationService.this;
        mLbm = LocalBroadcastManager.getInstance(mContext);
        mLm = (LocationManager) getSystemService(LOCATION_SERVICE);
        bootstrap();

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        mLm.removeUpdates(this);
        super.onDestroy();
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if( isBetterLocation(mLastRegisteredLocation, location) )
        {
            Intent toActivity = new Intent(mContext, MainActivity.class);
            toActivity.setAction(LocalBroadcastDictionary.ACTION_NEW_LOCATION);
            toActivity.putExtra(LocalBroadcastDictionary.EXTRA_NEW_LOCATION, location);
            mLbm.sendBroadcast(toActivity);

            mLastRegisteredLocation = location;
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        if(provider.equals( LocationManager.GPS_PROVIDER))
            mGPSProvider = null;
        else if( provider.equals( LocationManager.NETWORK_PROVIDER ) )
            mNetworkProvider = null;
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        if( provider.equals( LocationManager.GPS_PROVIDER ) )
        {
            mGPSProvider = LocationManager.GPS_PROVIDER;
            mLm.requestLocationUpdates(mGPSProvider, mProviderUpdateInterval,
                    mMinumumGPSDistance, this);
        }
        else if( provider.equals( LocationManager.NETWORK_PROVIDER ) )
        {
            mNetworkProvider = LocationManager.NETWORK_PROVIDER;
            mLm.requestLocationUpdates(mNetworkProvider, mProviderUpdateInterval,
                    mMinumumGPSDistance, this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void bootstrap()
    {
        if(mLm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            mGPSProvider = LocationManager.GPS_PROVIDER;
            mLm.requestLocationUpdates(mGPSProvider, mProviderUpdateInterval,
                    mMinumumGPSDistance, this);
        }
        if(mLm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        {
            mNetworkProvider = LocationManager.NETWORK_PROVIDER;
            mLm.requestLocationUpdates(mNetworkProvider, mProviderUpdateInterval,
                    mMinumumGPSDistance, this );
        }

        if( mGPSProvider == null && mNetworkProvider == null )
            Log.e("LocationService", "No providers available");
    }

    /**
     * Decide if new location is better than older by following some basic criteria.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    private boolean isBetterLocation(Location oldLocation, Location newLocation)
    {
        int TIME_THREASHOLD = 1000 * 10; //10 seconds has passed
        int DISTANCE_THREASHOLD = 15; //New location is 15m distant from the new one

        // If there is no old location, the new location is better
        if( oldLocation == null )
            return true;

        // Check whether the new location fix is newer or older
        long timeDelta = newLocation.getTime() - oldLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_THREASHOLD;
        boolean isSignificantlyOlder = timeDelta < -TIME_THREASHOLD;
        boolean isNewer = timeDelta > 0;

        // If it's been more than TIME_THREASHOLD seconds since the current location, use the
        // new location, because the user has likely moved. If the new location is more than
        // TIME_THREASHOLD seconds older, it must be worse.
        if (isSignificantlyNewer)
            return true;
        else if (isSignificantlyOlder)
            return false;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - oldLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > DISTANCE_THREASHOLD;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                oldLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate)
            return true;
        else if (isNewer && !isLessAccurate)
            return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
            return true;

        return false;
    }

    private boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null)
            return provider2 == null;

        return provider1.equals(provider2);
    }
}