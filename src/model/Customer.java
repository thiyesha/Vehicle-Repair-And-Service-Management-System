package model;

public class Customer {
    private int id;
    private String name;
    private String phone;
    private String idCardNumber;
    private Vehicle vehicle;

    public Customer(int id, String name, String phone, String idCardNumber, Vehicle vehicle) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.idCardNumber = idCardNumber;
        this.vehicle = vehicle;
    }

    public Customer() {

    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getIdCardNumber() { return idCardNumber; }
    public Vehicle getVehicle() { return vehicle; }

    // ✅ Added setter
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }



    @Override
    public String toString(){
        return name + (vehicle != null ? " (" + vehicle.getRegistrationNumber() + ")" : "");
    }
}
