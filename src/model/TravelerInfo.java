package model;

public class TravelerInfo {

    private String fullName;
    private int age;
    private String id;

    public TravelerInfo(String fullName, int age, String id) {

        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("full name cannot be blank or null");

        }

        if (age < 0 ) {
            throw new IllegalArgumentException("Age cannot be negative");
        }

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Traveler id cannot be null or blank");
        }

        this.fullName = fullName;
        this.age = age;
        this.id = id.trim();
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return fullName + " (" + age + ", id=" + id + ")";
    }

}