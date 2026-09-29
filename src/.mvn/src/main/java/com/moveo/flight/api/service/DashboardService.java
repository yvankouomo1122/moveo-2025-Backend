package com.moveo.flight.api.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.moveo.flight.api.dto.DashboardClientDTO;
import com.moveo.flight.api.dto.DashboardMostChosenFlightDTO;
import com.moveo.flight.api.dto.DashboardSummaryDTO;
import com.moveo.flight.api.dto.FlightDTO;
import com.moveo.flight.api.dto.DashboardLatestReservationDTO;
import com.moveo.flight.api.dto.DashboardRouteDTO;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.repository.DestinationRepository;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.repository.UserRepository;

@Service
public class DashboardService {
    private final FlightRepository flightRepository;
    private final DestinationRepository destinationRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;

    public DashboardService(FlightRepository flightRepository, DestinationRepository destinationRepository,
        ReservationRepository reservationRepository, UserRepository userRepository){
            this.flightRepository = flightRepository;
            this.destinationRepository = destinationRepository;
            this.reservationRepository = reservationRepository;
            this.userRepository = userRepository;
        }
    
    public DashboardSummaryDTO getDashboardSummary(){
        long totalFlights = flightRepository.count();
        long activeFlights = flightRepository.countByStatus(FlightStatus.AVAILABLE);
        
        List<DashboardRouteDTO> availableRoutes = destinationRepository.findAll()
        .stream()
        .map(destination -> new DashboardRouteDTO(
            destination.getStartPoint(),
            destination.getEndPoint()
        ))
        .collect(Collectors.toList());

        long totalReservations = reservationRepository.countByStartDate(LocalDateTime.now());
        long totalClients = userRepository.countByRoles(UserRole.USER);
        
        List<DashboardLatestReservationDTO> latestReservations = reservationRepository.findTop5ByOrderByStartDateDesc()
        .stream()
        .map(this::toLatestReservationDTO)
        .collect(Collectors.toList());

        List<DashboardRouteDTO> paidRoutes = getPaidRoutes();
        DashboardMostChosenFlightDTO mostChosenPaidFlight = getMostChosenPaidFlight();

        return new DashboardSummaryDTO(
            activeFlights,
            totalFlights,
            availableRoutes,
            totalReservations,
            totalClients,
            latestReservations,
            paidRoutes,
            mostChosenPaidFlight
        );
    }

    private DashboardLatestReservationDTO toLatestReservationDTO(Reservation reservation){
        String clientName = reservation.getUser() != null ? reservation.getUser().getFullName() : null;
        String destination = null;
        if (reservation.getDestination() != null){
            destination = reservation.getDestination().getStartPoint()
            + " - "
            + reservation.getDestination().getEndPoint();
        }
        return new DashboardLatestReservationDTO(
            clientName,
            destination,
            reservation.getStartDate(),
            reservation.getNbPassengers(),
            reservation.getStatus() != null ? reservation.getStatus().name() : null
        );
    }

    private List<DashboardRouteDTO> getPaidRoutes(){
        List<Object[]> rows = reservationRepository.findPaidDestinationStats();
        List<DashboardRouteDTO> paidRoutes = new ArrayList<>();

        for (Object[] row : rows) {
            paidRoutes.add(new DashboardRouteDTO(
                (String) row[1],
                (String) row[2],
                ((Long) row[3])
            ));
        }

        return paidRoutes;
    }

    private DashboardMostChosenFlightDTO getMostChosenPaidFlight(){
        List<Object[]> rows = reservationRepository.findMostChosenPaidFlight();

        if (rows.isEmpty()) {
            return null;
        }

        Object[] row = rows.get(0);
        DashboardMostChosenFlightDTO flight = new DashboardMostChosenFlightDTO();
        flight.setId((Long) row[0]);
        flight.setModel((String) row[1]);
        flight.setCapacity((Integer) row[2]);
        flight.setStatus((com.moveo.flight.api.model.FlightStatus) row[3]);
        flight.setPricePerHour((Long) row[4]);
        flight.setImages((String) row[5]);
        flight.setFlightCount((Long) row[6]);
        return flight;
    }

    private FlightDTO toFlightDTO(Flight flight){
        return new FlightDTO(
            flight.getId(),
            flight.getModel(),
            flight.getCapacity(),
            flight.getStatus(),
            flight.getPricePerHour(),
            flight.getImages()
        );
    }

    public List<FlightDTO> getAllFlights(){
        return flightRepository.findAll()
        .stream()
        .map(this::toFlightDTO)
        .collect(Collectors.toList());
    }

    public List<DashboardClientDTO> getClients(){
        return userRepository.findDashboardClients();
    }
}
