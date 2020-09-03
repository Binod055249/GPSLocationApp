package com.example.gpslocationapp;

import android.location.Location;

public class TaxiManager  {

    private Location destinationLocation;

    public void setDestinationLocation(Location destinationLocation){

        this.destinationLocation=destinationLocation;
    }

    public float returnTheDistanceToDestinationLocationInMeters(Location currentLocation){

        if(currentLocation!=null && destinationLocation!=null){

           return currentLocation.distanceTo(destinationLocation);
        }else{

            return -100.0f;
        }
    }

    public String returnTheMilesBetweenCurrentLocationAndDestinationLocation
            (Location currentLocation,int metersPerMile){

        int miles=(int)(returnTheDistanceToDestinationLocationInMeters(currentLocation)/metersPerMile);

        if(miles==1){
            return "1 Mile";
        }else if(miles>1){

            return miles+" Miles";
        }else {

            return "No Miles";
        }
    }

    public String returnTimeLeftToGetToDestinationLocation
            (Location currentLocation, float milesPerHour,int metersPerMile){

        float distanceInMeter=returnTheDistanceToDestinationLocationInMeters(currentLocation);
        float timeLeft=distanceInMeter/(milesPerHour*metersPerMile);

        String timeResult="";

        int timeLeftInHours=(int)timeLeft;

        if(timeLeftInHours==1){

            timeResult+="1 Hour ";

        }else if(timeLeftInHours>1){

            timeResult+=timeLeftInHours+" Hours ";
        }

        int minutesLeft=(int)((timeLeft - timeLeftInHours)*60);

        if(minutesLeft==1){

            timeResult+="1 Minute";

        }else if(minutesLeft>1){

            timeResult += minutesLeft + " Minutes";
        }

        if(timeLeftInHours<=0 && minutesLeft<=0){

            timeResult="Less than a minute is left to get to the destination Location";
        }

        return timeResult;
    }
}
