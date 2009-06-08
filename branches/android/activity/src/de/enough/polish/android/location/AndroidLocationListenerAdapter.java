//#condition polish.android
package de.enough.polish.android.location;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class AndroidLocationListenerAdapter implements LocationListener {

	private de.enough.polish.android.location.LocationListener listener;
	private final AndroidLocationProvider locationProvider;

	public AndroidLocationListenerAdapter(AndroidLocationProvider locationProvider) {
		this.locationProvider = locationProvider;
	}

	public void onLocationChanged(Location location) {
		//#debug
		//# System.out.println("onLocationChanged:"+location);
		if(this.listener == null) {
			//#debug warn
			//# System.out.println("No listener to inform about location change");
			return;
		}
		if(location == null) {
			System.out.println("AndroidLocationListenerAdapter.onLocationChanged:location is null.");
			return;
		}
		de.enough.polish.android.location.Location wrappedLocation = new de.enough.polish.android.location.Location(location);
		LocationProvider.setLastKnownLocation(wrappedLocation);
		this.listener.locationUpdated(this.locationProvider, wrappedLocation);
	}

	public void onProviderDisabled(String providerName) {
		//#debug
		//# System.out.println("onProviderDisabled:"+providerName);
		// When the GPS provider is disabled, switch to the NETWORK provider.
		if( ! LocationManager.GPS_PROVIDER.equals(providerName)) {
			return;
		}
		this.locationProvider.setProviderName(LocationManager.NETWORK_PROVIDER);
	}

	public void onProviderEnabled(String providerName) {
		//#debug
		//# System.out.println("onProviderEnabled:"+providerName);
		// The provider which was requested by criteria became available.
		String providerByCritera = this.locationProvider.getProviderByCritera();
		if( ! providerName.equals(providerByCritera)) {
			return;
		}
		this.locationProvider.setProviderName(providerByCritera);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		//#debug
		//# System.out.println("onStatusChanged:provider:"+provider+".Status:"+status);
		int translatedStatus;
		switch(status) {
		case android.location.LocationProvider.AVAILABLE:
			translatedStatus = LocationProvider.AVAILABLE;
			onProviderEnabled(provider);
			break;
		case android.location.LocationProvider.OUT_OF_SERVICE:
			translatedStatus = LocationProvider.OUT_OF_SERVICE;
			break;
			
		case android.location.LocationProvider.TEMPORARILY_UNAVAILABLE:
			translatedStatus = LocationProvider.TEMPORARILY_UNAVAILABLE;
			onProviderDisabled(provider);
			break;
		default: throw new IllegalArgumentException("LocationProvider Status change '"+status+"' not recognized.");
		}
		this.listener.providerStateChanged(this.locationProvider, translatedStatus);
	}
	
	public void setListener(de.enough.polish.android.location.LocationListener locationListener) {
		this.listener = locationListener;
	}

	public de.enough.polish.android.location.LocationListener getListener() {
		return this.listener;
	}
	
}
