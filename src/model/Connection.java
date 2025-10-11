package model;

import java.time.LocalTime;
import java.util.List;

public class Connection {
    private List<Route> routes;
    private LocalTime totalDepartureTime;
    private LocalTime totalArrivalTime;
    private Money totalPriceFirstClass;
    private Money totalPriceSecondClass;

    public Connection(List<Route> routes) {
        this.routes = routes;
        calculateTotalTimes();
        calculateTotalPrices();
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