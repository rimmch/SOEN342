/*package repository;

import model.*;
import parser.CSVRouteParser;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RouteCatalogue {
    private List<Route> routes;
    private CSVRouteParser parser;

    public RouteCatalogue() {
        this.routes = new ArrayList<>();
        this.parser = new CSVRouteParser();
    }

    public void loadRoutesFromCSV(String filePath) throws IOException {
        this.routes = parser.parseRoutes(filePath);
    }

    public List<Route> find(SearchCriteria criteria) {
        return routes.stream()
            .filter(route -> matchesCriteria(route, criteria))
            .collect(Collectors.toList());
    }

    private boolean matchesCriteria(Route route, SearchCriteria criteria) {
        if (criteria.getDepartureStation() != null &&
            !route.getDepartureStation().getName().equalsIgnoreCase(criteria.getDepartureStation().getName())) {
            return false;
        }

        if (criteria.getArrivalStation() != null &&
            !route.getArrivalStation().getName().equalsIgnoreCase(criteria.getArrivalStation().getName())) {
            return false;
        }


        if (criteria.getDepartureDate() != null &&
            !route.getDayPattern().isOperatingOn(criteria.getDepartureDate().getDayOfWeek())) {
            return false;
        }


        if (criteria.getPreferredTime() != null) {
            LocalTime routeTime = route.getDepartureTime();
            LocalTime preferredTime = criteria.getPreferredTime();
            long timeDiff = Math.abs(routeTime.toSecondOfDay() - preferredTime.toSecondOfDay());
            if (timeDiff > 7200) { // 2 hours in seconds
                return false;
            }
        }

        return true;
    }

    public List<Route> getAllRoutes() {
        return new ArrayList<>(routes);
    }

    public void addRoute(Route route) {
        if (route != null) {
            routes.add(route);
        }
    }

    public void clearRoutes() {
        routes.clear();
    }

    public int getRouteCount() {
        return routes.size();
    }
}

 */