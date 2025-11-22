USE train_system;

-- Insert STATION data
INSERT INTO STATION (name, city, country, code) VALUES
('Gare du Nord', 'Paris', 'France', 'PAR'),
('St Pancras International', 'London', 'United Kingdom', 'LON'),
('Hauptbahnhof', 'Berlin', 'Germany', 'BER');

-- Insert ROUTE data
INSERT INTO ROUTE (origin_station_id, destination_station_id, departure_time, arrival_time, train_type, first_class_price, second_class_price) VALUES
(1, 2, '08:15:00', '10:30:00', 'TGV', 150.00, 80.00),
(2, 3, '14:00:00', '20:45:00', 'ICE', 200.00, 120.00),
(1, 3, '09:00:00', '18:30:00', 'THALYS', 180.00, 100.00);

-- Insert ROUTE_DAY data
INSERT INTO ROUTE_DAY (route_id, day_of_week) VALUES
(1, 1), -- Paris to London: Monday
(1, 2), -- Paris to London: Tuesday
(1, 3), -- Paris to London: Wednesday
(2, 1), -- London to Berlin: Monday
(2, 3), -- London to Berlin: Wednesday
(2, 5), -- London to Berlin: Friday
(3, 1), -- Paris to Berlin: Monday
(3, 2), -- Paris to Berlin: Tuesday
(3, 3); -- Paris to Berlin: Wednesday
