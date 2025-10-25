package model;

public class TravelerInfo {
    private String name;
    private int age;
    private String id;

    public TravelerInfo(String name, int age, String id) {
        this.name = name;
        this.age = age;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getId() {
        return id;
    }
}