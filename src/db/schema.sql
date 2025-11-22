CREATE DATABASE IF NOT EXISTS train_system;
USE train_system;

-- STATION table
CREATE TABLE STATION (
    station_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL UNIQUE
);

-- ROUTE table
CREATE TABLE ROUTE (
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    origin_station_id INT NOT NULL,
    destination_station_id INT NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    train_type VARCHAR(50) NOT NULL,
    first_class_price DECIMAL(8,2) NOT NULL,
    second_class_price DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (origin_station_id) REFERENCES STATION(station_id) ON DELETE RESTRICT,
    FOREIGN KEY (destination_station_id) REFERENCES STATION(station_id) ON DELETE RESTRICT
);

-- ROUTE_DAY table
CREATE TABLE ROUTE_DAY (
    route_id INT NOT NULL,
    day_of_week TINYINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    PRIMARY KEY (route_id, day_of_week),
    FOREIGN KEY (route_id) REFERENCES ROUTE(route_id) ON DELETE RESTRICT
);

-- CLIENT table
CREATE TABLE CLIENT (
    client_id INT PRIMARY KEY AUTO_INCREMENT,
    nameFirst VARCHAR(100) NOT NULL,
    nameLast VARCHAR(100) NOT NULL,
    gov_id VARCHAR(50) NOT NULL UNIQUE,
    age INT NOT NULL
);

-- CONNECTION table
CREATE TABLE CONNECTION (
    connection_id INT PRIMARY KEY AUTO_INCREMENT,
    total_duration_min INT NOT NULL,
    total_price DECIMAL(8,2) NOT NULL,
    legs_count INT NOT NULL
);

-- CONNECTION_LEG table
CREATE TABLE CONNECTION_LEG (
    connection_id INT NOT NULL,
    seq_no INT NOT NULL,
    route_id INT NOT NULL,
    leg_duration_min INT NOT NULL,
    leg_price DECIMAL(8,2) NOT NULL,
    PRIMARY KEY (connection_id, seq_no),
    FOREIGN KEY (connection_id) REFERENCES CONNECTION(connection_id) ON DELETE RESTRICT,
    FOREIGN KEY (route_id) REFERENCES ROUTE(route_id) ON DELETE RESTRICT
);

-- TRIP table
CREATE TABLE TRIP (
    trip_id INT PRIMARY KEY AUTO_INCREMENT,
    client_id INT NOT NULL,
    connection_id INT NOT NULL,
    booking_date DATE NOT NULL,
    travel_date DATE NOT NULL,
    class_type VARCHAR(10) NOT NULL CHECK (class_type IN ('FIRST', 'SECOND')),
    price DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (client_id) REFERENCES CLIENT(client_id) ON DELETE RESTRICT,
    FOREIGN KEY (connection_id) REFERENCES CONNECTION(connection_id) ON DELETE RESTRICT
);
