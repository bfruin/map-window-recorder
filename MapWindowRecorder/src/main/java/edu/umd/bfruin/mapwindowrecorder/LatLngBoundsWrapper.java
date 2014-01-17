package edu.umd.bfruin.mapwindowrecorder;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;

/**
 * Created by Brendan on 1/15/14.
 *
 * This wrapper allows for Serialization of LatLngBounds
 */
public class LatLngBoundsWrapper implements Serializable {
    private double southwestLatitude;
    private double southwestLongitude;
    private double northeastLatitude;
    private double northeastLongitude;

    public LatLngBoundsWrapper (LatLngBounds latLngBounds) {
        southwestLatitude = latLngBounds.southwest.latitude;
        southwestLongitude = latLngBounds.southwest.longitude;
        northeastLatitude = latLngBounds.northeast.latitude;
        northeastLongitude = latLngBounds.northeast.longitude;
    }

    public LatLngBounds getLatLngBounds () {
        return new LatLngBounds(new LatLng(southwestLatitude, southwestLongitude), new LatLng(northeastLatitude, northeastLongitude));
    }
}
