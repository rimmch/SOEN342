package repository;

import model.*;
import parser.CSVRouteParser;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RouteCatalogue maintains a collection of Route objects and provides search functionality.
 * 
 * This class represents the Route Catalog from the domain model (Iteration 3).
 * It can load routes from CSV files and find routes matching search criteria.
 * 
 * According to the domain model:
 * - Route Catalog contains a List<Route>
 * - Provides loadFromCsv(path: String) method
 * - Provides getAllRoutes() method
 */
public class RouteCatalogue {
    private List<Route> routes;
    private CSVRouteParser parser;

    /**
     * Constructs an empty RouteCatalogue.
     */
    public RouteCatalogue() {
        this.routes = new ArrayList<>();
        this.parser = new CSVRouteParser();
    }

    /**
     * Loads routes from a CSV file into the catalogue.
     * This corresponds to the domain model's loadFromCsv method.
     * 
     * @param filePath path to the CSV file containing route data
     * @throws IOException if the file cannot be read
     */
    public void loadRoutesFromCSV(String filePath) throws IOException {
        this.routes = parser.parseRoutes(filePath);
    }

    /**
     * Finds routes matching the given search criteria.
     * 
     * @param criteria the search criteria to match against
     * @return a list of routes that match all specified criteria
     */
    public List<Route> find(SearchCriteria criteria) {
        if (criteria == null) {
            return new ArrayList<>();
        }
        
        return routes.stream()
            .filter(route -> matchesCriteria(route, criteria))
            .collect(Collectors.toList());
    }

    /**
     * Checks if a route matches all specified criteria.
     * 
     * @param route the route to check
     * @param criteria the search criteria
     * @return true if the route matches all criteria, false otherwise
     */
    private boolean matchesCriteria(Route route, SearchCriteria criteria) {
        // Match departure station (using equals() which compares by code)
        if (criteria.getDepartureStation() != null &&
            !route.getDepartureStation().equals(criteria.getDepartureStation())) {
            return false;
        }

        // Match arrival station (using equals() which compares by code)
        if (criteria.getArrivalStation() != null &&
            !route.getArrivalStation().equals(criteria.getArrivalStation())) {
            return false;
        }

        // Match operating day
        if (criteria.getDepartureDate() != null &&
            !route.getDayPattern().isOperatingOn(criteria.getDepartureDate().getDayOfWeek())) {
            return false;
        }

        // Match preferred departure time (within 2 hours)
        if (criteria.getPreferredTime() != null) {
            LocalTime routeTime = route.getDepartureTime();
            LocalTime preferredTime = criteria.getPreferredTime();
            long timeDiff = Math.abs(routeTime.toSecondOfDay() - preferredTime.toSecondOfDay());
            if (timeDiff > 7200) { // 2 hours in seconds
                return false;
            }
        }

        // Match preferred train type
        if (criteria.getPreferredTrainType() != null &&
            route.getTrainType() != criteria.getPreferredTrainType()) {
            return false;
        }

        // Match maximum price (based on ticket class preference)
        if (criteria.getMaxPrice() != null) {
            Money routePrice;
            if (criteria.getTicketClass() != null) {
                // Use the specified ticket class
                routePrice = (criteria.getTicketClass() == TicketClass.FIRST_CLASS) ?
                    route.getPriceFirstClass() : route.getPriceSecondClass();
            } else {
                // Default to second class if no preference specified
                routePrice = route.getPriceSecondClass();
            }
            
            // Check if route price is within budget
            if (routePrice.getAmount().compareTo(criteria.getMaxPrice().getAmount()) > 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns all routes in the catalogue.
     * This corresponds to the domain model's getAllRoutes() method.
     * 
     * @return a copy of all routes in the catalogue
     */
    public List<Route> getAllRoutes() {
        return new ArrayList<>(routes);
    }

    /**
     * Adds a route to the catalogue.
     * 
     * @param route the route to add
     */
    public void addRoute(Route route) {
        if (route != null) {
            routes.add(route);
        }
    }

    /**
     * Removes all routes from the catalogue.
     */
    public void clearRoutes() {
        routes.clear();
    }

    /**
     * Returns the number of routes in the catalogue.
     * 
     * @return the count of routes
     */
    public int getRouteCount() {
        return routes.size();
    }

    /**
     * Checks if the catalogue is empty.
     * 
     * @return true if there are no routes, false otherwise
     */
    public boolean isEmpty() {
        return routes.isEmpty();
    }
}
