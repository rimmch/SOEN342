package model;

import java.time.LocalTime;
import java.util.List;

// iteration1
public class Connection {
    private List<Route> routes;
    private LocalTime totalDepartureTime;
    private LocalTime totalArrivalTime;
    private Money totalPriceFirstClass;
    private Money totalPriceSecondClass;
    private int totalDurationMinutes;
    

    public Connection(List<Route> routes) {
        if (routes == null || routes.isEmpty()){
            throw new IllegalArgumentException("Connection needs a minimum of one route");
      }
        
        
        this.routes = List.copyOf(routes);
        calculateTotalTimes();
        calculateTotalPrices();
        calculateTotalDuration();
    }

    private void calculateTotalTimes() {
        if (!routes.isEmpty()) {
            this.totalDepartureTime = routes.get(0).getDepartureTime();
            this.totalArrivalTime = routes.get(routes.size() - 1).getArrivalTime();
        }
    }

    private void calculateTotalPrices() {
        if (!routes.isEmpty()) {
            Money firstClassTotal = new Money(routes.get(0).getPriceFirstClass().getAmount(),
                                            routes.get(0).getPriceFirstClass().getCurrency());
            Money secondClassTotal = new Money(routes.get(0).getPriceSecondClass().getAmount(),
                                             routes.get(0).getPriceSecondClass().getCurrency());

            for (int i = 1; i < routes.size(); i++) {
                firstClassTotal = firstClassTotal.add(routes.get(i).getPriceFirstClass());
                secondClassTotal = secondClassTotal.add(routes.get(i).getPriceSecondClass());
            }

            this.totalPriceFirstClass = firstClassTotal;
            this.totalPriceSecondClass = secondClassTotal;
        }
    }

    private void calculateTotalDuration() {
       
       int minutes = 0;
       for (Route r : routes) {
           minutes += r.getDurationMinutes();
       }

        for (int i = 0; i < routes.size() -1; i++) {
            var arr = routes.get(i).getArrivalTime();
            var dep = routes.get(i+1).getDepartureTime();
            int gap = (int) java.time.Duration.between(arr,dep).toMinutes();
            if (gap <= 0) {
                gap += 24*60;
            }
            minutes += gap;  
        }  
        this.totalDurationMinutes = minutes;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public String getFormattedTotalDuration() {
        int h = totalDurationMinutes / 60, m = totalDurationMinutes % 60;
        return String.format("%dh %02dm", h, m);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public LocalTime getTotalDepartureTime() {
        return totalDepartureTime;
    }

    public LocalTime getTotalArrivalTime() {
        return totalArrivalTime;
    }

    public Money getTotalPriceFirstClass() {
        return totalPriceFirstClass;
    }

    public Money getTotalPriceSecondClass() {
        return totalPriceSecondClass;
    }

    public int getNumberOfTransfers() {
        return routes.size() - 1;
    }
}
