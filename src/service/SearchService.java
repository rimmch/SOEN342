package service;

import model.*;
import repository.RouteCatalogue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchService {
    private RouteCatalogue routeCatalogue;

    public SearchService(RouteCatalogue routeCatalogue) {
        this.routeCatalogue = routeCatalogue;
    }

    public List<Route> searchRoutes(SearchCriteria criteria) {
        return routeCatalogue.find(criteria);
    }

    public List<Route> searchAndSort(SearchCriteria criteria, SortKey sortKey) {
        List<Route> routes = searchRoutes(criteria);
        return sortRoutes(routes, sortKey);
    }

    public List<Route> sortRoutes(List<Route> routes, SortKey sortKey) {
        List<Route> sortedRoutes = new ArrayList<>(routes);

        switch (sortKey) {
            case DEPARTURE_TIME:
                sortedRoutes.sort(Comparator.comparing(Route::getDepartureTime));
                break;
            case ARRIVAL_TIME:
                sortedRoutes.sort(Comparator.comparing(Route::getArrivalTime));
                break;
            case DURATION:
                sortedRoutes.sort(Comparator.comparing(this::calculateDuration));
                break;
            case PRICE_FIRST_CLASS:
                sortedRoutes.sort(Comparator.comparing(route -> route.getPriceFirstClass().getAmount()));
                break;
            case PRICE_SECOND_CLASS:
                sortedRoutes.sort(Comparator.comparing(route -> route.getPriceSecondClass().getAmount()));
                break;
            case DEPARTURE_STATION:
                sortedRoutes.sort(Comparator.comparing(route -> route.getDepartureStation().getName()));
                break;
            case ARRIVAL_STATION:
                sortedRoutes.sort(Comparator.comparing(route -> route.getArrivalStation().getName()));
                break;
            case TRAIN_TYPE:
                sortedRoutes.sort(Comparator.comparing(Route::getTrainType));
                break;
        }

        return sortedRoutes;
    }

    public List<Route> filterByTrainType(List<Route> routes, TrainType trainType) {
        List<Route> filtered = new ArrayList<>();
        for (Route route : routes) {
            if (route.getTrainType() == trainType) {
                filtered.add(route);
            }
        }
        return filtered;
    }

    public List<Route> filterByMaxPrice(List<Route> routes, Money maxPrice, ClassType classType) {
        List<Route> filtered = new ArrayList<>();
        for (Route route : routes) {
            Money routePrice = (classType == ClassType.FIRST) ?
                route.getPriceFirstClass() : route.getPriceSecondClass();

            if (routePrice.getAmount().compareTo(maxPrice.getAmount()) <= 0) {
                filtered.add(route);
            }
        }
        return filtered;
    }

    private long calculateDuration(Route route) {
        return route.getArrivalTime().toSecondOfDay() - route.getDepartureTime().toSecondOfDay();
    }
}