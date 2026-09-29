package com.moveo.flight.api.service;

import com.moveo.flight.api.model.ReservationStatus;
import com.moveo.flight.api.dto.ReservationDTO;
import com.moveo.flight.api.model.Destination;
import com.moveo.flight.api.model.Flight;
import com.moveo.flight.api.model.FlightOption;
import com.moveo.flight.api.model.OperationLog;
import com.moveo.flight.api.model.Reservation;
import com.moveo.flight.api.model.User;
import com.moveo.flight.api.repository.DestinationRepository;
import com.moveo.flight.api.repository.FlightRepository;
import com.moveo.flight.api.repository.OperationLogRepository;
import com.moveo.flight.api.repository.ReservationRepository;
import com.moveo.flight.api.repository.UserRepository;
import com.moveo.flight.api.exception.ReservationOverlapException;
import com.moveo.flight.api.service.OperationLogService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.time.Duration;

/**
 * Service for managing reservations.
 * Contains the main business logic.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final FlightRepository flightRepository;
    private final DestinationRepository destinationRepository;
    private final OperationLogRepository logRepository;
    private final OperationLogService operationLogService;

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            FlightRepository flightRepository,
            DestinationRepository destinationRepository,
            OperationLogRepository logRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.flightRepository = flightRepository;
        this.destinationRepository = destinationRepository;
        this.logRepository = logRepository;
        this.operationLogService = new OperationLogService(logRepository);
    }

    /** Create a new reservation */
    public Reservation createReservation(Long userId, Long flightId, Long destinationId, Reservation reservation) {
        // Fetch user and flight
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));
        Destination destination = destinationRepository.findById(destinationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found with email: " + authenticatedEmail));
        if (authenticatedUser.getId() != user.getId()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authenticated user cannot make reservation for this user");
        }

        if (reservation.getStartDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reservation startDate must not be null");
        }
        Integer estimatedDuration = destination.getEstimatedDuration();
        if (estimatedDuration == null || estimatedDuration <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destination estimated duration is invalid");
        }

        LocalDateTime calculatedEndDate = reservation.getStartDate().plusMinutes(estimatedDuration);
        reservation.setEndDate(calculatedEndDate);

        if (reservation.getNbPassengers() == null || reservation.getNbPassengers() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of passengers must be provided and greater than zero");
        }
        if (reservation.getFlightOption() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight option must be provided");
        }

        // Check if the flight is free for this given timeline
        boolean overlap = reservationRepository.existsOverlappingReservation(flightId, reservation.getStartDate(), reservation.getEndDate());
        if (overlap){
            throw new ReservationOverlapException("This flight is already reserved during the selected time slot.");
        }
        // Set relations
        reservation.setUser(user);
        reservation.setFlight(flight);
        reservation.setDestination(destination);

        // Price Calculation
        long reservation_duration = Duration.between(reservation.getStartDate(), reservation.getEndDate()).toHours();
        BigDecimal flightPerHour = BigDecimal.valueOf(flight.getPricePerHour());
        int nbPassengers = reservation.getNbPassengers();
        FlightOption flightOption = reservation.getFlightOption();

        BigDecimal baseCost = flightPerHour.multiply(BigDecimal.valueOf(reservation_duration));
        BigDecimal passengerCost = BigDecimal.valueOf(150).multiply(BigDecimal.valueOf(nbPassengers));
        BigDecimal subtotal = baseCost.add(passengerCost);

        BigDecimal multiplier = BigDecimal.ONE;
        if (flightOption == FlightOption.WITH_PILOT){
            multiplier = BigDecimal.valueOf(1.20);
        } else if (flightOption == FlightOption.EXPRESS) {
            multiplier = BigDecimal.valueOf(1.40);
        }

        BigDecimal totalPrice = subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
        reservation.setTotalPrice(totalPrice.longValue());
        reservation.setStatus(ReservationStatus.PENDING);
        Reservation saved = reservationRepository.save(reservation);
        operationLogService.logAction("CREATE_RESERVATION", "Reservation", saved.getId(), authenticatedEmail, "Created reservation for flightId = " + saved.getFlight().getId());
        return saved;
    }

    /* Get a price estimation for the reservation */
    public BigDecimal estimateTotalPrice(Long flightId, LocalDateTime startDate, LocalDateTime endDate, int nbPassengers, FlightOption flightOption){
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date and end date must not be null");
        }
        if (!endDate.isAfter(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }
        long reservation_duration = Duration.between(startDate, endDate).toHours();
        BigDecimal flightPerHour = BigDecimal.valueOf(flight.getPricePerHour());

        BigDecimal baseCost = flightPerHour.multiply(BigDecimal.valueOf(reservation_duration));
        BigDecimal passengerCost = BigDecimal.valueOf(150).multiply(BigDecimal.valueOf(nbPassengers));
        BigDecimal subtotal = baseCost.add(passengerCost);

        BigDecimal multiplier = BigDecimal.ONE;
        if (flightOption == FlightOption.WITH_PILOT){
            multiplier = BigDecimal.valueOf(1.20);
        } else if (flightOption == FlightOption.EXPRESS) {
            multiplier = BigDecimal.valueOf(1.40);
        }

        return subtotal.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    /** Get reservation by ID */
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reservation not found with id " + id));
    }

    /** Get all reservations for a user */
    public List<ReservationDTO> getReservationsByUser(Long userId) {
        List<Reservation> reservations = reservationRepository.findByUserId(userId);
        return reservations.stream()
                .map(this::toDTO)
                .toList(); // Convert Entity to DTO
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals("ADMIN"));
    }

    private void verifyReservationOwnershipOrAdmin(Reservation reservation) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authenticated user not found with email: " + authenticatedEmail));

        boolean isOwner = reservation.getUser() != null && authenticatedUser.getId().equals(reservation.getUser().getId());
        boolean isAdmin = isAdmin(authentication);

        if (!isOwner && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to modify this reservation");
        }
    }

    /** Cancel a reservation */
    public ReservationDTO cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        verifyReservationOwnershipOrAdmin(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("UPDATE_RESERVATION", "Reservation", saved.getId(), authenticatedEmail, "Updated reservation for flightId = " + saved.getFlight().getId());
        return toDTO(saved);
    }

    /** Close a reservation */
    public ReservationDTO closeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        verifyReservationOwnershipOrAdmin(reservation);
        reservation.setStatus(ReservationStatus.CLOSED);
        reservation.setUpdatedAt(LocalDateTime.now());
        Reservation saved = reservationRepository.save(reservation);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String authenticatedEmail = authentication.getName();
        operationLogService.logAction("UPDATE_RESERVATION", "Reservation", saved.getId(), authenticatedEmail, "Updated reservation for flightId = " + saved.getFlight().getId());
        return toDTO(saved);
    }

    /** Get all reservations (admin use) */
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
            .sorted(Comparator.comparing(Reservation::getCreatedAt).reversed())
            .map(this::toDTO)
            .toList(); // Convert Entity to DTO (sorted by createdAt desc)
    }

    public ReservationDTO toDTO(Reservation reservation) {
    User user = reservation.getUser();
    Flight flight = reservation.getFlight();
    Destination destination = reservation.getDestination();

    return new ReservationDTO(
        reservation.getId(),
        reservation.getStartDate(),
        reservation.getEndDate(),
        reservation.getTotalPrice(),
        reservation.getStatus(),
        (user != null) ? user.getId() : null,
        (user != null) ? user.getEmail() : null,
        (flight != null) ? flight.getId() : null,
        (flight != null) ? flight.getModel() : null,
        (destination != null) ? destination.getStartPoint() : null,
        (destination != null) ? destination.getEndPoint() : null,
        (destination != null) ? destination.getDistance() : null,
        (destination != null) ? destination.getEstimatedDuration() : null
    );
    }
}
