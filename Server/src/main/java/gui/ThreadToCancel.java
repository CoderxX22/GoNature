package gui;

import database.DatabaseController;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static java.lang.Thread.sleep;

public class ThreadToCancel implements Runnable{

    /*Thread method that checks every row and IF the exit time and visit date is the same as real values
    * set this order as cancelled
    * */
    @Override
    public void run() {
        ZoneId zoneId = ZoneId.of("Asia/Jerusalem");

        synchronized(DatabaseConnection.lock) {
            while(!DatabaseConnection.isConnected) {
                try {
                    DatabaseConnection.lock.wait();
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        DatabaseController DB = new DatabaseController();
            String queryToCancel = "UPDATE `order` SET cancelled = ? WHERE exit_time <= ? AND visit_date = ? AND  paid = ? AND cancelled = ?";
            try {
                PreparedStatement pstmt = DB.getConnection().prepareStatement(queryToCancel);
                pstmt.setBoolean(1, true);
                pstmt.setTime(2, Time.valueOf(LocalTime.now(zoneId).plusHours(2)));
                pstmt.setDate(3, Date.valueOf(LocalDate.now(zoneId).plusDays(1)));
                pstmt.setBoolean(4,false);
                pstmt.setBoolean(5,false);

                int rowsUpdated = pstmt.executeUpdate();
                System.out.println(rowsUpdated + " rows updated");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

        System.out.println("cancel orders every hour");

    }
}