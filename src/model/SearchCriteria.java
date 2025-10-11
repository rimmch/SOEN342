package model;

import java.time.LocalDate;
import java.time.LocalTime;

public class SearchCriteria {
    private Station departureStation;
    private Station arrivalStation;
    private LocalDate departureDate;
    private LocalTime preferredTime;
    private TrainType preferredTrainType;
    private Money maxPrice;
    private ClassType classType;

    public SearchCriteria() {
    }

    public SearchCriteria(Station departureStation, Station arrivalStation, LocalDate departureDate) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureDate = departureDate;
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

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalTime getPreferredTime() {
        return preferredTime;
    }

    public void setPreferredTime(LocalTime preferredTime) {
        this.preferredTime = preferredTime;
    }

    public TrainType getPreferredTrainType() {
        return preferredTrainType;
    }

    public void setPreferredTrainType(TrainType preferredTrainType) {
        this.preferredTrainType = preferredTrainType;
    }

    public Money getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Money maxPrice) {
        this.maxPrice = maxPrice;
    }

    public ClassType getClassType() {
        return classType;
    }

    public void setClassType(ClassType classType) {
        this.classType = classType;
    }
}