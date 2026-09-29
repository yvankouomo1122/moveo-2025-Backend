package com.moveo.flight.api.dto;
import java.util.List;
import java.math.BigDecimal;
public class DashboardSummaryDTO {
    private Long activeFlights;
    private Long totalFlights;
    private List<DashboardRouteDTO> availableRoutes;
    private Long totalReservations;
    private Long totalClients;
    private List<DashboardLatestReservationDTO> latestReservations;
    private List<DashboardRouteDTO> paidRoutes;
    private DashboardMostChosenFlightDTO mostChosenPaidFlight;
    private Long reservationsThisMonth;
    private Long paidReservationsCount;
    private BigDecimal paidReservationsSum;

    public DashboardSummaryDTO() {}

    public DashboardSummaryDTO(Long activeFlights, Long totalFlights, List<DashboardRouteDTO> availableRoutes, Long totalReservations, Long totalClients, List<DashboardLatestReservationDTO> latestReservations, List<DashboardRouteDTO> paidRoutes, DashboardMostChosenFlightDTO mostChosenPaidFlight, Long reservationsThisMonth, Long paidReservationsCount, BigDecimal paidReservationsSum){
        this.activeFlights = activeFlights;
        this.totalFlights = totalFlights;
        this.availableRoutes = availableRoutes;
        this.totalReservations = totalReservations;
        this.totalClients = totalClients;
        this.latestReservations = latestReservations;
        this.paidRoutes = paidRoutes;
        this.mostChosenPaidFlight = mostChosenPaidFlight;
        this.reservationsThisMonth = reservationsThisMonth;
        this.paidReservationsCount = paidReservationsCount;
        this.paidReservationsSum = paidReservationsSum;
    }

    public Long getActiveFlights(){
        return activeFlights;
    }

    public void setActiveFlights(Long activeFlights){
        this.activeFlights = activeFlights;
    }

    public Long getTotalFlights(){
        return totalFlights;
    }

    public void setTotalFlights(Long totalFlights){
        this.totalFlights = totalFlights;
    }

    public List<DashboardRouteDTO> getAvailableRoutes(){
        return availableRoutes;
    }

    public void setAvailableRoutes(List<DashboardRouteDTO> availableRoutes){
        this.availableRoutes = availableRoutes;
    }

    public Long getTotalReservations(){
        return totalReservations;
    }

    public void setTotalReservations(Long totalReservations){
        this.totalReservations = totalReservations;
    }

    public Long getTotalClients(){
        return totalClients;
    }

    public void setTotalClients(Long totalClients){
        this.totalClients = totalClients;
    }

    public List<DashboardLatestReservationDTO> getLatestReservations(){
        return latestReservations;
    }

    public void setLatestReservations(List<DashboardLatestReservationDTO> latestReservations){
        this.latestReservations = latestReservations;
    }

    public List<DashboardRouteDTO> getPaidRoutes(){
        return paidRoutes;
    }

    public void setPaidRoutes(List<DashboardRouteDTO> paidRoutes){
        this.paidRoutes = paidRoutes;
    }

    public DashboardMostChosenFlightDTO getMostChosenPaidFlight(){
        return mostChosenPaidFlight;
    }

    public void setMostChosenPaidFlight(DashboardMostChosenFlightDTO mostChosenPaidFlight){
        this.mostChosenPaidFlight = mostChosenPaidFlight;
    }

    public Long getReservationsThisMonth() {
        return reservationsThisMonth;
    }

    public void setReservationsThisMonth(Long reservationsThisMonth) {
        this.reservationsThisMonth = reservationsThisMonth;
    }

    public Long getPaidReservationsCount() {
        return paidReservationsCount;
    }

    public void setPaidReservationsCount(Long paidReservationsCount) {
        this.paidReservationsCount = paidReservationsCount;
    }

    public BigDecimal getPaidReservationsSum() {
        return paidReservationsSum;
    }

    public void setPaidReservationsSum(BigDecimal paidReservationsSum) {
        this.paidReservationsSum = paidReservationsSum;
    }
}
