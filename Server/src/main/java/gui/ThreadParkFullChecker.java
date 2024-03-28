package gui;
import database.DatabaseController;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadParkFullChecker implements Runnable {
    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        int minutes = now.getMinute();
        if(minutes == 0 || minutes == 30|| minutes == 59 || minutes == 29){
            while(!(DatabaseConnection.isConnected))
            {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            checkAndUpdateParkCapacity();
        }
    }

    private void checkAndUpdateParkCapacity() {
        // This method will be called every 30 minutes

        DatabaseController dbController = new DatabaseController();
        LocalTime now = LocalTime.now();
        LocalDate today = LocalDate.now();
        try {
            // Fetch all park ids
            PreparedStatement parkIdsStatement = dbController.getConnection().prepareStatement("SELECT park_id_pk FROM `park`");
            ResultSet parkIdsResult = dbController.executeQuery(parkIdsStatement);

            while (parkIdsResult.next()) {
                int parkId = parkIdsResult.getInt("park_id_pk");

                // Fetch current visitors and capacity for the park
                PreparedStatement parkInfoStatement = dbController.getConnection().prepareStatement("SELECT current_visitors, capacity FROM `park` WHERE park_id_pk = ?");
                parkInfoStatement.setInt(1, parkId);
                ResultSet parkInfoResult = dbController.executeQuery(parkInfoStatement);

                if (parkInfoResult.next()) {
                    int currentVisitors = parkInfoResult.getInt("current_visitors");
                    int capacity = parkInfoResult.getInt("capacity");

                    // Fetch all orders for the current park for today
                    PreparedStatement ordersStatement = dbController.getConnection().prepareStatement("SELECT visit_time, exit_time, number_of_visitors FROM `order` WHERE park_id_fk = ? AND visit_date = ? ");
                    ordersStatement.setInt(1, parkId);
                    ordersStatement.setDate(2, java.sql.Date.valueOf(today.plusDays(1)));
                    ResultSet ordersResult = dbController.executeQuery(ordersStatement);

                    while (ordersResult.next()) {
                        LocalTime visitTime = ordersResult.getTime("visit_time").toLocalTime().minusHours(2);
                        LocalTime exitTime = ordersResult.getTime("exit_time").toLocalTime().minusHours(2);
                        int visitors = (Integer) ordersResult.getInt("number_of_visitors");

                        if (now.getMinute()==(visitTime.getMinute()) && (now.getHour())==(visitTime.getHour())) {
                            currentVisitors+=visitors;
                        }

                        if (now.getMinute()==(exitTime.getMinute()) && (now.getHour())==(exitTime.getHour())) {
                            currentVisitors-=visitors;
                        }
                    }

                    // Update current visitors and capacity offset in the database
                    PreparedStatement updateStatement = dbController.getConnection().prepareStatement("UPDATE `park` SET current_visitors = ?, capacity_offset = ? WHERE park_id_pk = ?");
                    updateStatement.setInt(1, currentVisitors);
                    updateStatement.setInt(2, capacity - currentVisitors);
                    updateStatement.setInt(3, parkId);
                    dbController.executeUpdate(updateStatement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
