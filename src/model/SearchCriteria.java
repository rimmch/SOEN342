package model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * SearchCriteria encapsulates search parameters for finding train routes and connections.
 * This class follows the Criteria/Query Object pattern to encapsulate search parameters.
 * 
 * All fields are optional except for the basic constructor parameters (departure, arrival, date).
 * Optional criteria can be set using setter methods for filtering and refinement.
 */
public class SearchCriteria {
    private Station departureStation;
    private Station arrivalStation;
    private LocalDate departureDate;
    private LocalTime preferredTime;
    private TrainType preferredTrainType;
    private Money maxPrice;
    private TicketClass ticketClass;

    /**
     * Default constructor - all fields are null/optional.
     */
    public SearchCriteria() {
    }

    /**
     * Constructor with required search parameters.
     * 
     * @param departureStation the origin station
     * @param arrivalStation the destination station
     * @param departureDate the desired travel date
     */
    public SearchCriteria(Station departureStation, Station arrivalStation, LocalDate departureDate) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureDate = departureDate;
    }

    // Getters and Setters

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

    public TicketClass getTicketClass() {
        return ticketClass;
    }

    public void setTicketClass(TicketClass ticketClass) {
        this.ticketClass = ticketClass;
    }

    /**
     * Checks if this criteria has all required fields set.
     * 
     * @return true if departure station, arrival station, and date are all set
     */
    public boolean isValid() {
        return departureStation != null && 
               arrivalStation != null && 
               departureDate != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SearchCriteria{");
        sb.append("from=").append(departureStation != null ? departureStation.getName() : "null");
        sb.append(", to=").append(arrivalStation != null ? arrivalStation.getName() : "null");
        sb.append(", date=").append(departureDate);
        if (preferredTime != null) {
            sb.append(", preferredTime=").append(preferredTime);
        }
        if (preferredTrainType != null) {
            sb.append(", trainType=").append(preferredTrainType);
        }
        if (maxPrice != null) {
            sb.append(", maxPrice=").append(maxPrice);
        }
        if (ticketClass != null) {
            sb.append(", ticketClass=").append(ticketClass);
        }
        sb.append("}");
        return sb.toString();
    }
}
