package order;
import java.sql.*;
import java.time.LocalTime;

import account.DB.CRUD;
import account.DB.SqlConnection;

public abstract class Order {

    private String parkID;
    private LocalTime enterTime;
    private LocalTime  exitTime;  // need to add this method too
    private String email;
    private int amountOfVisitors;
    private double price;
    private String PhoneNumber;
    private int OrderNumber = 0;
    private static CRUD crud;

    static {
        try {
            crud = new CRUD();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Order(String parkID, String email, int amountOfVisitors, String PhoneNumber,LocalTime time) throws NumberOutOfBoundException, SQLException {
        this.parkID = parkID;
        this.email = email;
        this.amountOfVisitors= amountOfVisitors;
        this.PhoneNumber = PhoneNumber;
        this.OrderNumber+=1; // every order has its own number
        this.enterTime = time;
    }

    public String getParkID() {
        return parkID;
    }

    public LocalTime getEnterTime() {
        return enterTime;
    }

    public LocalTime getExitTime() {
        return exitTime;
    }

    public String getEmail() {
        return email;
    }

    public int getOrderNumber(){
        return OrderNumber;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getAmountOfVisitors() {
        return amountOfVisitors;
    }

    public double calculatePrice(double discount_percentage) throws NumberOutOfBoundException{
        this.price=(amountOfVisitors*80)*discount_percentage;
        return this.price;
    } // get visitors amount to calculate the correct price

    public static void LoadToDB(int OrderNumber, String parkID, LocalTime enterTime, int amountOfVisitors, String phoneNumber) throws SQLException {
        String queryToRun = "INSERT INTO `order` (OrderNumber, ParkName, TimeOfVisit, NumberOfVisitors, TelephoneNumber) VALUES ('" +
                OrderNumber + "', '" + parkID + "', '" + enterTime + "', " + amountOfVisitors + ", '" + phoneNumber + "')";
        crud.insertData(queryToRun);
    }
}
