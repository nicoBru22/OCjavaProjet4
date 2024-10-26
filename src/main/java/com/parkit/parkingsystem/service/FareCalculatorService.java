package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        //utilisation de getTime() pour récupérer les données en miliseconde puis en minute
        double inHour = (ticket.getInTime().getTime()/60000);
        double outHour = (ticket.getOutTime().getTime()/60000);

        //TODO: Some tests are failing here. Need to check if this logic is correct
        
        //calcul de la durée en passant les minutes en heure
        double duration = (outHour - inHour)/60;
        
        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}