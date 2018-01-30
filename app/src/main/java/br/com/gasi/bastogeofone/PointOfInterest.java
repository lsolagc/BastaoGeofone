package br.com.gasi.bastogeofone;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Resende on 25/01/2018.
 */

public class PointOfInterest {

    private LatLng coordinates;
    private String address;

    public LatLng getCoordinates(){
        return coordinates;
    }

    public void setCoordinates(LatLng coord){
        coordinates = coord;
    }

    public String getAddress(){
        return address;
    }

    public void setAddress(String add){
        address = add;
    }

    public PointOfInterest(){
        coordinates = null;
        address = null;
    }

    public String toString(){
        return address+" (Lat: "+coordinates.latitude+"; Lng: "+coordinates.longitude+")";
    }

}
