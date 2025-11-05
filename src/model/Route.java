package model;

import java.time.Duration;
import java.time.LocalTime;

// for iteration1 
public class Route {
    private String routeId;
    private Station departureStation;
    private Station arrivalStation;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private TrainType trainType;
    private Money priceFirstClass;
    private Money priceSecondClass;
    private DaySet dayPattern;

    private int tripDurationMinutes;
    
    // REVOIR SI ROUTEID SHOULD BE IN PARAM
    public Route(String routeId, Station departureStation, Station arrivalStation,
                 LocalTime departureTime, LocalTime arrivalTime, TrainType trainType,
                 Money priceFirstClass, Money priceSecondClass, DaySet dayPattern) {
        this.routeId = routeId;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.trainType = trainType;
        this.priceFirstClass = priceFirstClass;
        this.priceSecondClass = priceSecondClass;
        this.dayPattern = dayPattern;

        this.tripDurationMinutes = computeDurationMinutes(departureTime, arrivalTime);
    }

    private static int computeDurationMinutes (LocalTime dep, LocalTime arr) {

        Duration d = Duration.between (dep, arr);
        if (d.isNegative() || d.isZero()) {
            d = d.plusDays(1);
        }
        return (int) d.toMinutes();
    
    }

    public int getDurationMinutes() {

        return tripDurationMinutes;
        
    }

    public String getFormattedDuration() {
        int h = tripDurationMinutes / 60, m = tripDurationMinutes % 60;
       return String.format("%dh %02dm", h, m);
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public Station getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(Station departureStation) {
        this.departureStation = departureStation;
    }

    public Station getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(Station arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public TrainType getTrainType() {
        return trainType;
    }

    public void setTrainType(TrainType trainType) {
        this.trainType = trainType;
    }

    public Money getPriceFirstClass() {
        return priceFirstClass;
    }

    public void setPriceFirstClass(Money priceFirstClass) {
        this.priceFirstClass = priceFirstClass;
    }

    public Money getPriceSecondClass() {
        return priceSecondClass;
    }

    public void setPriceSecondClass(Money priceSecondClass) {
        this.priceSecondClass = priceSecondClass;
    }

    public DaySet getDayPattern() {
        return dayPattern;
    }

    public void setDayPattern(DaySet dayPattern) {
        this.dayPattern = dayPattern;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
        this.tripDurationMinutes = computeDurationMinutes(this.departureTime, this.arrivalTime);
    }

    public LocalTime getArrivalTime() {
       return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
        this.tripDurationMinutes = computeDurationMinutes(this.departureTime, this.arrivalTime);
    }

    // to hide routeID
    public String toPublicString() {
        return String.format(
            "%s → %s | Dep %s  Arr %s | %s | 1st %s  2nd %s | Duration %s | Days %s",
            departureStation.getName(),
            arrivalStation.getName(),
            departureTime, arrivalTime,
            trainType,
            priceFirstClass, priceSecondClass,
            getFormattedDuration(),
            dayPattern
        );
    }

    @Override
    public String toString() {
        return toPublicString(); 
    }
}
