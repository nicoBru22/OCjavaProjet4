package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

	private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
	private static ParkingSpotDAO parkingSpotDAO;
	private static TicketDAO ticketDAO;
	private static DataBasePrepareService dataBasePrepareService;

	@Mock
	private static InputReaderUtil inputReaderUtil;

	@BeforeAll
	private static void setUp() throws Exception {
		parkingSpotDAO = new ParkingSpotDAO();
		parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;

		ticketDAO = new TicketDAO();
		ticketDAO.dataBaseConfig = dataBaseTestConfig;
		dataBasePrepareService = new DataBasePrepareService();

		assertThat(ticketDAO.dataBaseConfig).isNotNull();
		System.out.println("config du ticket :" + ticketDAO.dataBaseConfig);
	}

	@BeforeEach
	private void setUpPerTest() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

		ticketDAO.dataBaseConfig = dataBaseTestConfig;

		dataBasePrepareService.clearDataBaseEntries();
	}

	@AfterAll
	private static void tearDown() {

	}

	@Test
	@DisplayName("Test Intégration Entrée Parking")
	public void testParkingACarIntegration() throws Exception {
		System.out.println("TicketDAO Config: " + ticketDAO.dataBaseConfig);
		ParkingSpotDAO spyParkingSpotDAO = spy(parkingSpotDAO);
		ParkingService parkingService = new ParkingService(inputReaderUtil, spyParkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
		assertThat(inputReaderUtil.readSelection()).isEqualTo(1);
		assertThat(inputReaderUtil.readVehicleRegistrationNumber()).isEqualTo("ABCDEF");

		verify(spyParkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		verify(spyParkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));

		try {
			PreparedStatement ps = ticketDAO.dataBaseConfig.getConnection()
					.prepareStatement("SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ?");
			ps.setString(1, "ABCDEF");
			ResultSet rs = ps.executeQuery();

			boolean hasNext = rs.next();

			System.out.println("ticket ce qu'est rs.next() : " + hasNext);

			if (hasNext) {
				System.out.println("ticket ce qu'est rs.getString() :" + rs.getString("VEHICLE_REG_NUMBER"));
				System.out.println("ID: " + rs.getInt("ID") + " | PARKING_NUMBER: " + rs.getInt("PARKING_NUMBER")
						+ " | VEHICLE_REG_NUMBER: " + rs.getString("VEHICLE_REG_NUMBER"));
			} else {
				System.out.println("Aucune ligne trouvée avec ce VEHICLE_REG_NUMBER.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try {
			PreparedStatement ps = ticketDAO.dataBaseConfig.getConnection()
					.prepareStatement("SELECT * FROM parking WHERE AVAILABLE = ?");
			ps.setBoolean(1, false);
			ResultSet rs = ps.executeQuery();

			if (rs.next()) { // Vérifie si une ligne existe
				System.out.println("ID: " + rs.getInt("PARKING_NUMBER") + " | AVAILABLE: " + rs.getBoolean("AVAILABLE")
						+ " | TYPE: " + rs.getString("TYPE"));

				// Vérifie les conditions sur le parking
				assertThat(rs.getBoolean("AVAILABLE")).isEqualTo(false); // Vérifier que le parking est maintenant
																			// occupé
			} else {
				System.out.println("Aucune ligne trouvée avec la condition de disponibilité.");
			}
		} catch (SQLException e) {
			e.printStackTrace(); // Gérer l'exception (par exemple, l'afficher ou la relancer)
		}
	}

	@Test
	@DisplayName("Test Intégration Sortie Parking")
	public void testParkingLotExit() throws Exception {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();

		parkingService.processExitingVehicle();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(inputReaderUtil, times(2)).readVehicleRegistrationNumber();

		try (PreparedStatement ps = ticketDAO.dataBaseConfig.getConnection()
				.prepareStatement("SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ?")) {
			ps.setString(1, "ABCDEF");
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String vehicleRegNumber = rs.getString("VEHICLE_REG_NUMBER");
				double price = rs.getDouble("PRICE");
				Timestamp inTime = rs.getTimestamp("IN_TIME");
				Timestamp outTime = rs.getTimestamp("OUT_TIME");

				System.out.println("Données du ticket récupérées :");
				System.out.println("VEHICLE_REG_NUMBER: " + vehicleRegNumber);
				System.out.println("PRICE: " + price);
				System.out.println("IN_TIME: " + inTime);
				System.out.println("OUT_TIME: " + outTime);

				assertThat(vehicleRegNumber).isEqualTo("ABCDEF");
				assertThat(price).isEqualTo(0);
			} else {
				fail("Aucune donnée trouvée pour le véhicule avec le numéro ABCDEF.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Erreur lors de la vérification des données dans la base.");
		}
	}

	@Test
	@DisplayName("Test Intégration Sortie Parking utilisateur régulier")
	public void testParkingLotExitRecurringUser() throws Exception {
		ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();

		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();

		parkingService.processIncomingVehicle();
		parkingService.processExitingVehicle();

		verify(inputReaderUtil, times(3)).readSelection();
		verify(inputReaderUtil, times(6)).readVehicleRegistrationNumber();

		try (PreparedStatement ps = ticketDAO.dataBaseConfig.getConnection()
				.prepareStatement("SELECT * FROM ticket WHERE VEHICLE_REG_NUMBER = ?")) {
			ps.setString(1, "ABCDEF");
			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				String vehicleRegNumber = rs.getString("VEHICLE_REG_NUMBER");
				double price = rs.getDouble("PRICE");
				Timestamp inTime = rs.getTimestamp("IN_TIME");
				Timestamp outTime = rs.getTimestamp("OUT_TIME");

				System.out.println("Données du ticket récupérées :");
				System.out.println("VEHICLE_REG_NUMBER: " + vehicleRegNumber);
				System.out.println("PRICE: " + price);
				System.out.println("IN_TIME: " + inTime);
				System.out.println("OUT_TIME: " + outTime);

				assertThat(vehicleRegNumber).isEqualTo("ABCDEF");
				assertThat(price).isEqualTo(0);
			} else {
				fail("Aucune donnée trouvée pour le véhicule avec le numéro ABCDEF.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
			fail("Erreur lors de la vérification des données dans la base.");
		}
	}
}
