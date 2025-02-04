package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

@DisplayName("test du calculateur de prix")
public class FareCalculatorServiceTest {

	private static FareCalculatorService fareCalculatorService;
	
	private Ticket ticket;

	@BeforeAll
	private static void setUp() {
		fareCalculatorService = new FareCalculatorService();
	}

	@BeforeEach
	private void setUpPerTest() {
		ticket = new Ticket();
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une voiture")
	public void calculateFareCar() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setDiscount(false);
		ticket.setId(1);
		ticket.setVehicleRegNumber("ABCDEF");
		
		fareCalculatorService.calculateFare(ticket);
		
		System.out.println("test : " + ticket.getPrice());
		
		System.out.println(Fare.CAR_RATE_PER_HOUR);
		
		assertThat(Fare.CAR_RATE_PER_HOUR).isEqualTo(ticket.getPrice());
		assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une moto")
	public void calculateFareBike() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertThat(Fare.BIKE_RATE_PER_HOUR).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une véhicule inconnu")
	public void calculateFareUnkownType() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("Vérifie si la sortie du parking est avant l'entrée")
	public void calculateFareBikeWithFutureInTime() {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une moto qui reste moins d'1 heure")
	public void calculateFareBikeWithLessThanOneHourParkingTime() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		assertThat(Fare.BIKE_RATE_PER_HOUR).isEqualTo(1);
		assertThat(0.75 * Fare.BIKE_RATE_PER_HOUR).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une voiture qui reste moins d'1 heure")
	public void calculateFareCarWithLessThanOneHourParkingTime() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		assertThat(Fare.CAR_RATE_PER_HOUR).isEqualTo(1.5);
		assertThat(0.75 * Fare.CAR_RATE_PER_HOUR).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une moto qui reste plus d'un jour")
	public void calculateFareCarWithMoreThanADayParkingTime() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));// 24 hours parking time should give 24 *
																			// parking fare per hour
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		assertThat(24 * Fare.CAR_RATE_PER_HOUR).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une voiture qui reste moins de 30 minutes")
	public void calculateFareCarWithLessThan30minutesParkingTimeDescription() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (20 * 60 * 1000));// 20 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		assertThat(0.0).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une moto qui reste moins de 30 minutes")
	public void calculateFareBikeWithLessThan30minutesParkingTimeDescription() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (30 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		fareCalculatorService.calculateFare(ticket);
		
		assertThat(0.0).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une voiture avec réduction de 5%")
	public void calculateFareCarWithDiscountDescription() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));// 45 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setDiscount(true);
		
		fareCalculatorService.calculateFare(ticket);
		
		assertThat((0.75 * Fare.CAR_RATE_PER_HOUR) * 95 / 100).isEqualTo(ticket.getPrice());
	}

	@Test
	@Tag("calculateur de prix")
	@DisplayName("calcul prix du ticket pour une moto qui reste avec réduction de 5%")
	public void calculateFareBikeWithDiscountDescription() throws Exception {
		Date inTime = new Date();
		inTime.setTime(System.currentTimeMillis() - (75 * 60 * 1000));// 75 minutes parking time should give 3/4th
																		// parking fare
		Date outTime = new Date();
		ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

		ticket.setInTime(inTime);
		ticket.setOutTime(outTime);
		ticket.setParkingSpot(parkingSpot);
		ticket.setDiscount(true);
		fareCalculatorService.calculateFare(ticket);
		assertThat((1.25 * Fare.BIKE_RATE_PER_HOUR) * 95 / 100).isEqualTo(ticket.getPrice());
	}

}
