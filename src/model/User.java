package model;

public class User {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;

    public User(int id, String username, String password, String name, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // Getters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }
}