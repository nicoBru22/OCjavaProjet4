package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {
	
	public void calculateFare(Ticket ticket, boolean discount) throws Exception {	
		System.out.println("Appel de la méthode calculateFare");
		if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
			throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
		}
	    
		double inHour = (ticket.getInTime().getTime() / 60000);
		double outHour = (ticket.getOutTime().getTime() / 60000);

		// TODO: Some tests are failing here. Need to check if this logic is correct

		double duration = (outHour - inHour) / 60;

		if (duration <= 0.5) {
			ticket.setPrice(0);
		} else {
			if (discount) {
				switch (ticket.getParkingSpot().getParkingType()) {
				case CAR: {
					ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR) * 95 / 100);
					break;
				}
				case BIKE: {
					ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR) * 95 / 100);
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown Parking Type");
				}
			} else {			    
				switch (ticket.getParkingSpot().getParkingType()) {
				case CAR: {
					ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
					break;
				}
				case BIKE: {
					ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
					break;
				}
				default:
					throw new IllegalArgumentException("Unkown Parking Type");
				}
			}
			System.out.println("Le ticket dans la méthode calculateFare : " + ticket);
		}
	}
}