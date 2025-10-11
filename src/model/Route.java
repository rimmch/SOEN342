package model;

import java.time.LocalTime;

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

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
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

    @Override
    public String toString() {
        return routeId + ": " + departureStation.getName() + " -> " + arrivalStation.getName() +
               " (" + departureTime + " - " + arrivalTime + ")";
    }
}