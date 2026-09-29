package com.moveo.flight.api.service;

import com.moveo.flight.api.dto.ReservationDTO;
import com.moveo.flight.api.exception.ReservationOverlapException;
import com.moveo.flight.api.model.Route;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightOption;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.ReservationStatus;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.repository.RouteRepository;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Service responsible for reservation management
 * and reservation-related business rules.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final RouteRepository routeRepository;
    private final OperationLogService operationLogService;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            FlightRepository flightRepository,
            RouteRepository routeRepository,
            OperationLogRepository operationLogRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.routeRepository = routeRepository;
        this.operationLogService =
                new OperationLogService(operationLogRepository);
    }

    /**
     * Creates a new reservation.
     */
    public Reservation createReservation(
            Long userId,
            Long flightId,
            Long routeId,
            Reservation reservation
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found"
                ));

        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Route not found"
                ));

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String authenticatedEmail = authentication.getName();

        User authenticatedUser = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found with email: "
                                + authenticatedEmail
                ));

        // A client may only create a reservation for their own account.
        if (!authenticatedUser.getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Authenticated user cannot make a reservation for this user"
            );
        }

        if (reservation.getStartDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation startDate must not be null"
            );
        }

        if (reservation.getStartDate().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Reservation startDate must not be in the past"
            );
        }

        Integer estimatedDuration = route.getEstimatedDuration();

        if (estimatedDuration == null || estimatedDuration <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route estimated duration is invalid"
            );
        }

        LocalDateTime calculatedEndDate =
                reservation.getStartDate().plusMinutes(estimatedDuration);

        reservation.setEndDate(calculatedEndDate);

        if (reservation.getNbPassengers() == null
                || reservation.getNbPassengers() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Number of passengers must be greater than zero"
            );
        }

        if (flight.getCapacity() != null
                && reservation.getNbPassengers() > flight.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Number of passengers exceeds the aircraft capacity"
            );
        }

        if (reservation.getFlightOption() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight option must be provided"
            );
        }

        boolean overlap =
                reservationRepository.existsOverlappingReservation(
                        flightId,
                        reservation.getStartDate(),
                        reservation.getEndDate()
                );

        if (overlap) {
            throw new ReservationOverlapException(
                    "This flight is already reserved during the selected time slot."
            );
        }

        reservation.setUser(user);
        reservation.setFlight(flight);
        reservation.setRoute(route);

        BigDecimal totalPrice = calculateTotalPrice(
                flight,
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getFlightOption()
        );

        /*
         * totalPrice is currently stored as a Long in Reservation.
         * The decimal result is therefore rounded before persistence.
         */
        reservation.setTotalPrice(
                totalPrice.setScale(0, RoundingMode.HALF_UP).longValue()
        );

        reservation.setStatus(ReservationStatus.PENDING);

        Reservation saved = reservationRepository.save(reservation);

        operationLogService.logAction(
                "CREATE_RESERVATION",
                "Reservation",
                saved.getId(),
                authenticatedEmail,
                "Created reservation for flightId = "
                        + saved.getFlight().getId()
        );

        return saved;
    }

    /**
     * Estimates the price without creating a reservation.
     */
    public BigDecimal estimateTotalPrice(
            Long flightId,
            LocalDateTime startDate,
            Route route,
            int nbPassengers,
            FlightOption flightOption
    ) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flight not found"
                ));

        if (startDate == null || route == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Start date and route must not be null"
            );
        }

        if (flightOption == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight option must be provided"
            );
        }

        if (nbPassengers <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Number of passengers must be greater than zero"
            );
        }

        if (flight.getCapacity() != null
                && nbPassengers > flight.getCapacity()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Number of passengers exceeds the aircraft capacity"
            );
        }

        Integer estimatedDuration = route.getEstimatedDuration();

        if (estimatedDuration == null || estimatedDuration <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Route estimated duration is invalid"
            );
        }

        LocalDateTime endDate =
                startDate.plusMinutes(estimatedDuration);

        return calculateTotalPrice(
                flight,
                startDate,
                endDate,
                flightOption
        );
    }

    /**
     * Applies the common pricing formula.
     *
     * Base price = hourly aircraft price × duration in hours.
     *
     * VOL_SIMPLE = base price
     * MOV_BY = base price + 20%
     * MOV_FAST = base price + 40%
     */
    private BigDecimal calculateTotalPrice(
            Flight flight,
            LocalDateTime startDate,
            LocalDateTime endDate,
            FlightOption flightOption
    ) {
        if (flight.getPricePerHour() == null
                || flight.getPricePerHour().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight price per hour is invalid"
            );
        }

        long durationMinutes =
                Duration.between(startDate, endDate).toMinutes();

        if (durationMinutes <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Flight duration must be greater than zero"
            );
        }

        BigDecimal durationHours =
                BigDecimal.valueOf(durationMinutes)
                        .divide(
                                BigDecimal.valueOf(60),
                                4,
                                RoundingMode.HALF_UP
                        );

        BigDecimal pricePerHour = flight.getPricePerHour();

        BigDecimal baseCost =
                pricePerHour.multiply(durationHours);

        BigDecimal multiplier;

        switch (flightOption) {
            case MOV_BY:
                multiplier = BigDecimal.valueOf(1.20);
                break;

            case MOV_FAST:
                multiplier = BigDecimal.valueOf(1.40);
                break;

            case VOL_SIMPLE:
                multiplier = BigDecimal.ONE;
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Unsupported flight option"
                );
        }

        return baseCost
                .multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Returns a reservation by its ID.
     */
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found with id " + id
                ));
    }

    /**
     * Returns all reservations belonging to one user.
     */
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        return reservationRepository.findByUserId(userId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority ->
                        authority.equals("ROLE_ADMIN")
                                || authority.equals("ADMIN")
                );
    }

    private void verifyReservationOwnershipOrAdmin(
            Reservation reservation
    ) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String authenticatedEmail = authentication.getName();

        User authenticatedUser = userRepository
                .findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user not found with email: "
                                + authenticatedEmail
                ));

        boolean owner =
                reservation.getUser() != null
                        && authenticatedUser.getId()
                        .equals(reservation.getUser().getId());

        if (!owner && !isAdmin(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Not authorized to modify this reservation"
            );
        }
    }

    /**
     * Cancels a pending reservation.
     */
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found"
                ));

        verifyReservationOwnershipOrAdmin(reservation);

        if (!ReservationStatus.PENDING.equals(
                reservation.getStatus()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Can only cancel a pending reservation"
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());

        Reservation saved =
                reservationRepository.save(reservation);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        operationLogService.logAction(
                "UPDATE_RESERVATION",
                "Reservation",
                saved.getId(),
                authentication.getName(),
                "Cancelled reservation for flightId = "
                        + saved.getFlight().getId()
        );

        return toDTO(saved);
    }

    /**
     * Closes a confirmed reservation.
     */
    public ReservationDTO closeReservation(Long reservationId) {
        Reservation reservation = reservationRepository
                .findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found"
                ));

        verifyReservationOwnershipOrAdmin(reservation);

        if (!ReservationStatus.CONFIRMED.equals(
                reservation.getStatus()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Can only close a confirmed reservation"
            );
        }

        reservation.setStatus(ReservationStatus.CLOSED);
        reservation.setUpdatedAt(LocalDateTime.now());

        Reservation saved =
                reservationRepository.save(reservation);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        operationLogService.logAction(
                "UPDATE_RESERVATION",
                "Reservation",
                saved.getId(),
                authentication.getName(),
                "Closed reservation for flightId = "
                        + saved.getFlight().getId()
        );

        return toDTO(saved);
    }

    /**
     * Returns all reservations for the administration area.
     */
    public List<ReservationDTO> getAllReservations() {
        return reservationRepository.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Reservation::getCreatedAt
                        ).reversed()
                )
                .map(this::toDTO)
                .toList();
    }

    /**
     * Converts a Reservation entity into a ReservationDTO.
     */
    public ReservationDTO toDTO(Reservation reservation) {
        User user = reservation.getUser();
        Flight flight = reservation.getFlight();
        Route destination = reservation.getRoute();

        return new ReservationDTO(
                reservation.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getTotalPrice(),
                reservation.getStatus(),
                user != null ? user.getId() : null,
                user != null ? user.getEmail() : null,
                flight != null ? flight.getId() : null,
                flight != null ? flight.getModel() : null,
                destination != null
                        ? destination.getStartPoint()
                        : null,
                destination != null
                        ? destination.getEndPoint()
                        : null,
                destination != null
                        ? destination.getDistance()
                        : null,
                destination != null
                        ? destination.getEstimatedDuration()
                        : null
        );
    }
}
