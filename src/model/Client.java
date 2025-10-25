package model;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.Collectors;

    public class Client {
        private String lastName;
        private String id;
        private List<Trip> trips;

        public Client(String lastName, String id) {
            this.lastName = lastName;
            this.id = id;
            this.trips = new ArrayList<>();
        }

        public void addTrip(Trip trip) {
            trips.add(trip);
        }

        public String getLastName() {
            return lastName;
        }

        public String getId() {
            return id;
        }

        public List<Trip> getCurrentTrips() {
            return trips.stream()
                    .filter(Trip::isFuture)
                    .collect(Collectors.toList());
        }

        public List<Trip> getPastTrips() {
            return trips.stream()
                    .filter(Trip::isPast)
                    .collect(Collectors.toList());
        }

        public List<Trip> getAllTrips() {
            return new ArrayList<>(trips);
        }
    }