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

import java.util.Date;

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
        parkingService.processExitingVehicle();

        verify(ticketDAO, times(1)).getTicket("ABCDEF");
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
    }

	// test où tout se déroule comme il est attendu
	@Test
	@DisplayName("Processus d'entrée du véhicule")
	public void testProcessIncomingVehicle() throws Exception {
		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
		when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
		when(ticketDAO.isRegularUser("ABCDEF")).thenReturn(true);

		parkingService.processIncomingVehicle();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(inputReaderUtil, times(1)).readVehicleRegistrationNumber();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
		verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
		verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
		verify(ticketDAO, times(1)).isRegularUser("ABCDEF");
	}

	// test dans le cas où la méthode updateTicket() de ticketDAO renvoie false lors
	// de l’appel de processExitingVehicle()
	@Test
	@DisplayName("Processus de sortie si le ticket n'est pas update")
	public void processExitingVehicleTestUnableUpdate() {
		doReturn(false).when(ticketDAO).updateTicket(any(Ticket.class));

	    parkingService.processExitingVehicle();

	    verify(ticketDAO, times(1)).getTicket("ABCDEF");
	    verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
	    verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
	}

	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat l’obtention
	// d’un spot dont l’ID est 1 et qui est disponible.
	@Test
	@DisplayName("Test récupération du numéro de parking si disponible")
	public void testGetNextParkingNumberIfAvailable() {
		ParkingType parkingType = ParkingType.CAR;

		when(inputReaderUtil.readSelection()).thenReturn(1);
		when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(1);

		parkingService.getNextParkingNumberIfAvailable();

		verify(inputReaderUtil, times(1)).readSelection();
		verify(parkingSpotDAO, times(1)).getNextAvailableSlot(parkingType);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot disponible (la méthode renvoie null).
	@Test
	public void testGetNextParkingNumberIfAvailable_ParkingFull() {
	    // Simuler que le parking est complet en retournant 0
		ParkingType parkingType = ParkingType.CAR;
		when(inputReaderUtil.readSelection()).thenReturn(1);
	    when(parkingSpotDAO.getNextAvailableSlot(parkingType)).thenReturn(-1);

	    // Appeler la méthode
	    parkingService.getNextParkingNumberIfAvailable();

	    // Vérifier que le DAO a bien été appelé une fois
	    verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
	}

	
	
	
	
	// test de l’appel de la méthode getNextParkingNumberIfAvailable() avec pour
	// résultat aucun spot (la méthode renvoie null) car l’argument saisi par
	// l’utilisateur concernant le type de véhicule est erroné (par exemple,
	// l’utilisateur a saisi 3).
	@Test
	public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

		when(inputReaderUtil.readSelection()).thenReturn(3);

	    // Appeler la méthode
	    parkingService.getNextParkingNumberIfAvailable();


	    verify(inputReaderUtil, times(1)).readSelection();

	}

}
