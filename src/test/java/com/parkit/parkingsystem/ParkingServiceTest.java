package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

	private static ParkingService parkingService;

	@Mock
	private static InputReaderUtil inputReaderUtil;
	@Mock
	private static ParkingSpotDAO parkingSpotDAO;
	@Mock
	private TicketDAO ticketDAO;

	@BeforeEach
	private void setUpPerTest() {
		try {

			ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
			Ticket ticket = new Ticket();
			ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
			ticket.setParkingSpot(parkingSpot);
			ticket.setVehicleRegNumber("ABCDEF");

			lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
			lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
			lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
			lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

			parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to set up test mock objects");
		}
	}

	
	
	
	
	@Test
	@DisplayName("Test de sortie de véhicule")
	public void processExitingVehicleTest() {
		ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    
	    String messageSystem = "Recorded out-time for vehicle number:ABCDEF";
	    String messageSystemElse = "Unable to update ticket information. Error occurred";
		
		parkingService.processExitingVehicle();
		
		String consoleOutput = outContent.toString().trim();


		verify(ticketDAO, times(1)).getTicket("ABCDEF");
		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		
		assertThat(consoleOutput).contains(messageSystem);
		assertThat(consoleOutput).doesNotContain(messageSystemElse);
	}

	// test où tout se déroule comme il est attendu
	@Test
	@DisplayName("Processus d'entrée du véhicule")
	public void testProcessIncomingVehicle() throws Exception {
	    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    
	    String messageSystem = "Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%";
	    
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
		when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
		when(ticketDAO.isRegularUser("ABCDEF")).thenReturn(true);

		parkingService.processIncomingVehicle();
		
		String consoleOutput = outContent.toString().trim();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(ticketDAO, times(1)).isRegularUser("ABCDEF");
		
		assertThat(consoleOutput).contains(messageSystem);
	}

	// test dans le cas où la méthode updateTicket() de ticketDAO renvoie false lors
	// de l’appel de processExitingVehicle()
	@Test
	@DisplayName("Processus de sortie si le ticket n'est pas update")
	public void processExitingVehicleTestUnableUpdate() {
	    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	    System.setOut(new PrintStream(outContent));
	    
		String messageSystem = "Unable to update ticket information. Error occurred";
		
		doReturn(false).when(ticketDAO).updateTicket(any(Ticket.class));

		parkingService.processExitingVehicle();
		
		String consoleOutput = outContent.toString().trim();

		verify(ticketDAO, times(1)).getTicket("ABCDEF");
		verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
		verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
		
		assertThat(consoleOutput).contains(messageSystem);
		
		
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat l’obtention
	// d’un spot dont l’ID est 1 et qui est disponible.
	@Test
	@DisplayName("Test récupération du numéro de parking si disponible")
	public void testGetNextParkingNumberIfAvailable() {
		ParkingType parkingType = ParkingType.CAR;
		
		ParkingSpot parkingSpotTest = new ParkingSpot(1, ParkingType.CAR, true);

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(parkingType);
		
		System.out.println("le parkingSpot : " + parkingSpot);
		
		assertThat(parkingSpot).isInstanceOfAny(ParkingSpot.class);
		assertThat(parkingSpot).isEqualTo(parkingSpotTest);
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot disponible (la méthode renvoie null).
	@Test
	public void testGetNextParkingNumberIfAvailable_ParkingFull() {
		ParkingType parkingType = ParkingType.CAR;
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(-1);

		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		
		assertThat(parkingSpot).isNull();
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot (la méthode renvoie null) car l’argument saisi par
	// l’utilisateur concernant le type de véhicule est erroné (par exemple,
	// l’utilisateur a saisi 3).
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

		when(inputReaderUtil.readSelection()).thenReturn(3);

		// Appeler la méthode
		ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, times(1)).readSelection();
		
		assertThat(parkingSpot).isNull();

	}

}
