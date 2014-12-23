package edu.brandeis.cystant;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class LocationService implements LocationListener {
	protected LocationManager locationManager;
	protected LocationListener locationListener;
	protected Context context;
	String lat;
	String provider;
	protected String latitude, longitude;
	protected boolean gps_enabled, network_enabled;
	protected Criteria getspeed, getlocation;
	MessageListener listener;
	protected boolean speedrequired, locationrequired;
	Location lastlocation;
	

	public static final String CUR_SPEED = "CurrentSpeed";
	public static final String CUR_LOC = "CurrentLocation";
	public static final String LOC_CHANGE = "LocationChange";

	public void start(final Context ctx, MessageListener msg) {

		this.context = ctx;
		this.listener = msg;
		locationManager = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
		getlocation = new Criteria();
		getlocation.setAccuracy(Criteria.ACCURACY_FINE);
		getlocation.setSpeedRequired(false);
		getlocation.setCostAllowed(true);
		getspeed = new Criteria();
		getspeed.setAccuracy(Criteria.ACCURACY_FINE);
		getspeed.setSpeedRequired(true);
		getspeed.setCostAllowed(true);

	}

	public void monitorupdate() {
		locationManager.requestLocationUpdates(0, 0, getlocation, this, null);
	}

	public void stopmonitor() {
		locationManager.removeUpdates(this);
	}

	public void requireSpeed() {
		speedrequired = true;
		locationManager.requestSingleUpdate(getspeed, this, null);
	}

	public void requireLocation() {
		locationrequired = true;
		locationManager.requestSingleUpdate(getlocation, this, null);
	}

	@Override
	public void onLocationChanged(Location location) {

		Log.w("Location", "Location:" + location.getLatitude() + ", Longitude:"
				+ location.getLongitude());
		SendMessage("LocationData", location);
		if (locationrequired) {
			new GetAddressTask(this.context).execute(location);
			locationrequired = false;
		}
		Log.w("ZZH",String.format("Speed %f", location.getSpeed()));
		if (speedrequired && location.hasSpeed()) {
			SendMessage(CUR_SPEED, location.getSpeed());
			speedrequired = false;
			Log.w("ZZH","Yes Speed");
		}
	}

	protected void SendMessage(String type, Object param) {
		listener.OnMessage(type, param);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.w("Location", "disable " + provider);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.w("Location", "enable " + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.w("Location", "status");
	}

	private class GetAddressTask extends AsyncTask<Location, Void, Address> {
		Context mContext;

		public GetAddressTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Address doInBackground(Location... params) {
			// TODO Auto-generated method stub
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
			// Get the current location from the input parameter list
			Location loc = params[0];
			// Create a list to contain the result address
			List<Address> addresses = null;
			try {
				//
				// Return 1 address.
				//
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e1) {
				Log.e("LocationSampleActivity",
						"IO Exception in getFromLocation()");
				return null;
			} catch (IllegalArgumentException e2) {
				// Error message to post in the log
				String errorString = "Illegal arguments "
						+ Double.toString(loc.getLatitude()) + " , "
						+ Double.toString(loc.getLongitude())
						+ " passed to address service";
				Log.e("LocationSampleActivity", errorString);
				return null;
			}
			// If the reverse geocode returned an address
			if (addresses != null && addresses.size() > 0) {
				// Get the first address
				Address address = addresses.get(0);
				//
				// Format the first line of address (if available),
				// city, and country name.
				//
				for (Address addr : addresses) {
					Log.w("Location", String.format(
							"%s, %s, %s",
							// If there's a street address, add it
							addr.getMaxAddressLineIndex() > 0 ? addr
									.getAddressLine(0) : "",
							// Locality is usually a city
							addr.getLocality(),
							// The country of the address
							addr.getCountryName()));
				}
				// Return the text
				return address;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(Address result) {
			if (result == null)
				SendMessage("ERROR", "Error Getting Address");
			else
				SendMessage(CUR_LOC, result);
		}

	}
}
