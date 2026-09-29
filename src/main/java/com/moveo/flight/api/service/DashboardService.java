package com.moveo.flight.api.service;
import java.math.BigDecimal;
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
import com.moveo.flight.api.model.RoutePoint;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightStatus;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.UserRole;
import com.moveo.flight.api.repository.RouteRepository;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.repository.UserRepository;
@Service
public class DashboardService {
    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    public DashboardService(FlightRepository flightRepository, RouteRepository routeRepository,
        ReservationRepository reservationRepository, UserRepository userRepository){
            this.flightRepository = flightRepository;
            this.routeRepository = routeRepository;
            this.reservationRepository = reservationRepository;
            this.userRepository = userRepository;
        }

    public DashboardSummaryDTO getDashboardSummary(){
        long totalFlights = flightRepository.count();
        long activeFlights = flightRepository.countByStatus(FlightStatus.AVAILABLE);

        List<DashboardRouteDTO> availableRoutes = routeRepository.findAll()
        .stream()
        .map(destination -> new DashboardRouteDTO(
            destination.getStartPoint(),
            destination.getEndPoint()
        ))
        .collect(Collectors.toList());
        long totalReservations = reservationRepository.count();
        long totalClients = userRepository.countByRoles(UserRole.USER);

        List<DashboardLatestReservationDTO> latestReservations = reservationRepository.findTop5ByOrderByStartDateDesc()
        .stream()
        .map(this::toLatestReservationDTO)
        .collect(Collectors.toList());
        List<DashboardRouteDTO> paidRoutes = getPaidRoutes();
        DashboardMostChosenFlightDTO mostChosenPaidFlight = getMostChosenPaidFlight();
        // compute reservations for current month
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);
        Long reservationsThisMonth = reservationRepository.countByCreatedAtBetween(startOfMonth, startOfNextMonth);
        // number of paid reservations
        Long paidReservationsCount = reservationRepository.countPaidReservations();
        java.math.BigDecimal paidReservationsSum = reservationRepository.sumPaidReservations();
        return new DashboardSummaryDTO(
            activeFlights,
            totalFlights,
            availableRoutes,
            totalReservations,
            totalClients,
            latestReservations,
            paidRoutes,
            mostChosenPaidFlight,
            reservationsThisMonth,
            paidReservationsCount,
            paidReservationsSum
        );
    }
    private DashboardLatestReservationDTO toLatestReservationDTO(Reservation reservation){
        String clientName = reservation.getUser() != null ? reservation.getUser().getFullName() : null;
        String destination = null;
        if (reservation.getRoute() != null){
            destination = reservation.getRoute().getStartPoint()
            + " - "
            + reservation.getRoute().getEndPoint();
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
        List<Object[]> rows = reservationRepository.findPaidRouteStats();
        List<DashboardRouteDTO> paidRoutes = new ArrayList<>();
        for (Object[] row : rows) {
            paidRoutes.add(new DashboardRouteDTO(
                (RoutePoint) row[1],
                (RoutePoint) row[2],
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
        flight.setPricePerHour((BigDecimal) row[4]);
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
