package model;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.Collectors;

    public class Client {
        private final String lastName;
        private final String id;
        private final List<Trip> trips;

        public Client(String lastName, String id) {

            if (lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Last name cannot be null or blank");
            }

            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Client id cannot be null or blank");
           
            }
            

            
            this.lastName = lastName;
            this.id = id;
            this.trips = new ArrayList<>();
        }

        public void addTrip(Trip trip) {
            if (trip == null) {
               throw new IllegalArgumentException("Trip cannot be null");
            }
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

        @Override
    public String toString() {
        return "Client{" +
               "id='" + id + '\'' +
               ", lastName='" + lastName + '\'' +
               ", trips=" + trips.size() +
               '}';
        }
        
    }

 
