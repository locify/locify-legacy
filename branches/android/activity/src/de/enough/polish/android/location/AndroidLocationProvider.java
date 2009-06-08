//#condition polish.android
package de.enough.polish.android.location;

import android.content.Context;
import android.location.LocationManager;
import android.os.Looper;
import de.enough.polish.android.midlet.MIDlet;

public class AndroidLocationProvider extends LocationProvider {

	private final class LocationUpdateThread extends Thread {
		private Looper looper;

		public LocationUpdateThread(String name) {
			super(name);
		}

		public void run() {
			Looper.prepare();
			this.looper = Looper.myLooper();
			Looper.loop();
		}

		public Looper getLooper() {
			while(this.looper == null) {
				try {
					Thread.sleep(30);
					//#debug
					//# System.out.println("Waited for 30ms for the looper to prepare.");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return this.looper;
		}
	}

	public static final int ACCURACY_THRESHOLD = 50;
	public static final int DEFAULT_POWER_REQIREMENT = android.location.Criteria.POWER_MEDIUM;
	public static final int DEFAULT_MINIMAL_LOCATION_UPDATES = 60000;
	public static final float DEFAULT_MINIMAL_LOCATION_DISTANCE = 100; // meters
	
	private static LocationManager locationManager;
	private static AndroidLocationProvider instance;

	private String currentProvider;
	private AndroidLocationListenerAdapter currentLocationListener;
	private LocationUpdateThread locationUpdateThread;
	private final String providerByCritera;
	private int currentMinTime;
	private float currentMinDist;
	
	public static AndroidLocationProvider getAndroidLocationProviderInstance(Criteria meCriteria) {
		//#debug
		//# System.out.println("getAndroidLocationProviderInstance.");
		if(locationManager == null) {
			locationManager = (LocationManager)MIDlet.midletInstance.getSystemService(Context.LOCATION_SERVICE);
		}
		int powerRequirement;
		int preferredPowerConsumption = meCriteria.getPreferredPowerConsumption();
		switch(preferredPowerConsumption) {
			case Criteria.NO_REQUIREMENT: powerRequirement = DEFAULT_POWER_REQIREMENT; break;
			case Criteria.POWER_USAGE_HIGH: powerRequirement = android.location.Criteria.POWER_HIGH; break;
			case Criteria.POWER_USAGE_MEDIUM: powerRequirement = android.location.Criteria.POWER_MEDIUM; break;
			case Criteria.POWER_USAGE_LOW: powerRequirement = android.location.Criteria.POWER_LOW; break;
			default: throw new IllegalArgumentException("The power consumption must be one of Critiera.NO_REQUIREMENT, Criteria.POWER_USAGE_HIGH, Criteria.POWER_USAGE_MEDIUM or Criteria.POWER_USAGE_LOW.");
		}
		boolean altitudeRequired = meCriteria.isAltitudeRequired();
		boolean bearingRequired = meCriteria.isSpeedAndCourseRequired();
		boolean speedRequired = meCriteria.isSpeedAndCourseRequired();
		boolean costAllowed = meCriteria.isAllowedToCost();
		int accuracy;
		int horizontalAccuracy = meCriteria.getHorizontalAccuracy();
		if(horizontalAccuracy < ACCURACY_THRESHOLD) {
			accuracy = android.location.Criteria.ACCURACY_FINE;
		} else {
			accuracy = android.location.Criteria.ACCURACY_COARSE;
		}
		android.location.Criteria criteria = new android.location.Criteria();
		criteria.setAccuracy(accuracy);
		criteria.setSpeedRequired(speedRequired);
		criteria.setAltitudeRequired(altitudeRequired); 
		criteria.setBearingRequired(bearingRequired); 
		criteria.setCostAllowed(costAllowed); 
		criteria.setPowerRequirement(powerRequirement);
		String bestProvider = locationManager.getBestProvider(criteria, true);
		if(bestProvider == null) {
			return null;
		}
		// TODO: What about already registered listeners in the instance?
		if(instance == null) {
			instance = new AndroidLocationProvider(bestProvider);
		} else {
			instance.setProviderName(bestProvider);
		}
		return instance;
	}

	public AndroidLocationProvider(String provider) {
		this.currentProvider = provider;
		this.providerByCritera = provider;
		this.locationUpdateThread = new LocationUpdateThread("LocationUpdateThread");
		this.locationUpdateThread.start();
	}
	
	@Override
	public Location getLocation(int timeout) throws LocationException,InterruptedException {
		android.location.Location location = locationManager.getLastKnownLocation(this.currentProvider);
		if(location == null) {
			//#debug warn
			//# System.out.println("AndroidLocationProvider.getLocation():null received from LocationManager.getLastKnownLocation.");
			return null;
		}
		Location newLocation = new Location(location);
		LocationProvider.setLastKnownLocation(newLocation);
		//#debug
		//# System.out.println("AndroidLocationProvider.getLocation():"+newLocation);
		return newLocation;
	}

	@Override
	public int getState() {
		boolean enabled = locationManager.isProviderEnabled(this.currentProvider);
		int state;
		if(enabled) {
			state = LocationProvider.AVAILABLE;
		} else {
			state = LocationProvider.TEMPORARILY_UNAVAILABLE;
		}
		//#debug
		//# System.out.println("AndroidLocationProvider.getState:"+state);
		return state;
	}

	@Override
	public void reset() {
		if(this.currentLocationListener != null) {
			locationManager.removeUpdates(this.currentLocationListener);
		}
	}

	@Override
	public void setLocationListener(de.enough.polish.android.location.LocationListener listener, int interval, int timeout, int maxAge) {
		//#debug
		//# System.out.println("Setting a location listener:"+listener);
		if(this.currentLocationListener == null) {
			this.currentLocationListener = new AndroidLocationListenerAdapter(this);
		}
		if(listener == null) {
			// Remove the listener.
			locationManager.removeUpdates(this.currentLocationListener);
			return;
		}
		if(interval == -1) {
			this.currentMinTime = DEFAULT_MINIMAL_LOCATION_UPDATES;
		} else {
			this.currentMinTime = interval;
		}
		this.currentLocationListener.setListener(listener);
		this.currentMinDist = DEFAULT_MINIMAL_LOCATION_DISTANCE;
		registerLocationListener();
	}

	/**
	 * Set a new provider and also retargets a listener if one is present. Do not confuse the adapter listener with the user provided listener.
	 * @param providerName
	 */
	void setProviderName(String providerName) {
		android.location.LocationProvider locationProvider = locationManager.getProvider(providerName);
		if(locationProvider == null) {
			// Provider not supported, like NETWORK in the emulator.
			return;
		}
		this.currentProvider = providerName;
		if(this.currentLocationListener != null && this.currentLocationListener.getListener() != null) {
			//#debug
			//# System.out.println("setProviderName:"+providerName+" and also retargeting the listener");
			registerLocationListener();
		}
	}

	private void registerLocationListener() {
		// TODO: Promote the looper variable to a field.
		Looper looper = this.locationUpdateThread.getLooper();
		locationManager.requestLocationUpdates(this.currentProvider,this.currentMinTime,this.currentMinDist ,this.currentLocationListener,looper);
	}

	String getLocationProviderName() {
		return this.currentProvider;
	}

	String getProviderByCritera() {
		return this.providerByCritera;
	}
	
}
