package command;

import data.Account;
import data.ReportRequest;
import database.DatabaseController;

import java.io.FileWriter;
import java.io.*;
import java.sql.*;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class ImportReportRequest implements ServerCommand {
    private File reportFile;

    @Override
    public Message execute(Object param, Account account) {
        /**
         * Executes the command to import report requests and generate reports.
         *
         * @param param   The ReportRequest object containing the report request details.
         * @param account The account associated with the report request.
         * @return A message indicating the result of the import and report generation operation.
         */
        if (!(param instanceof ReportRequest)) {
            return new Message("paramFailed", "parameter is not ReportRequest type");
        }

        //------------------------------------------------
        //-----------------CHECK--------------------------
        ArrayList<String> stringArrayList = new ArrayList<>();
        //---------------------------------------------------
        ReportRequest reportRequest = (ReportRequest) param;

        DatabaseController DB = new DatabaseController();
        String queryToRun;
        String fileName;
        String ParkCapacity;
        String queryToSort = "SELECT guided_order FROM `order` ORDER BY guided_order ASC";
        String fileQuery = "INSERT INTO files (file_name,file_data) VALUES (?,?)";

        String queryToSearchParkID = "SELECT park_id_pk,capacity FROM park WHERE park_name = ?";
        int park_id = 0;
        int capacity = 0;
        int totalVisitor = 0;
        LocalDate fromDate = reportRequest.dates.get(0);
        LocalDate toDate = reportRequest.dates.get(1);

        if (reportRequest.capacityReport) {
            fileName = "capacity_Report_" + reportRequest.areaOfReport +"_"+LocalDate.now()+"_"+LocalTime.now().getHour()+ ".txt";
            reportFile = new File(fileName);
            queryToRun = "SELECT current_visitors FROM park WHERE park_id_pk = ?";
        } else {
            fileName = "by_group_report_" + reportRequest.areaOfReport +"_"+LocalDate.now()+"_"+LocalTime.now().getHour()+".txt";
            reportFile = new File(fileName);
            queryToRun = "SELECT * FROM `order` WHERE park_id_fk = ? AND visit_date BETWEEN ? AND ?";
        }
        try {
            FileWriter writer = new FileWriter(reportFile);
            PreparedStatement pstmt = DB.getConnection().prepareStatement(queryToSort);
            pstmt.execute();

            pstmt = DB.getConnection().prepareStatement(queryToSearchParkID);
            pstmt.setString(1, reportRequest.areaOfReport);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                park_id = rs.getInt("park_id_pk");
                capacity = rs.getInt("capacity");
            }


            pstmt = DB.getConnection().prepareStatement(queryToRun);
            pstmt.setInt(1, park_id);
            if (reportRequest.capacityReport){
                try {
                    while (fromDate.isBefore(toDate)) {
                        LocalTime startingTime = LocalTime.of(7,0);
                        while (startingTime.isBefore(LocalTime.of(20,0))) {
                            rs = pstmt.executeQuery();
                            if (rs.next()) {
                                totalVisitor = rs.getInt("current_visitors");
                                if (totalVisitor < capacity) {
                                    ParkCapacity = "Not Full Park";
                                } else {
                                    ParkCapacity = "Full Park";
                                }
                                String printString = String.format("Date: %s, Time: %s,Park: %s, Visitors Number: %d, Capacity: %s, Park Status: %s", fromDate.toString(),startingTime.toString(), reportRequest.areaOfReport, totalVisitor, capacity, ParkCapacity);
                                writer.write(printString);
                                writer.write(System.lineSeparator());
                            }
                            startingTime = startingTime.plusMinutes(30);
                        }
                        fromDate = fromDate.plusDays(1);
                    }
                }catch (DateTimeException e){
                    e.printStackTrace();
                    return new Message("DateContinuationFailed");
                }
            }else {
                pstmt.setDate(2, Date.valueOf(fromDate));
                pstmt.setDate(3, Date.valueOf(toDate));

                rs = pstmt.executeQuery();
                while (rs.next()) {
                    String orderID = rs.getString("order_id_pk");
                    String guidedOrder;
                    int accountID = rs.getInt("account_id");
                    LocalDate visitDate = rs.getDate("visit_date").toLocalDate();
                    LocalTime visitTime = rs.getTime("visit_time").toLocalTime();
                    visitTime = visitTime.minusHours(2);
                    int groupSize = rs.getInt("number_of_visitors");
                    if (rs.getBoolean("guided_order")) {
                        guidedOrder = "Guided";
                    } else if (groupSize <= 5 && groupSize > 1) {
                        guidedOrder = "Small Group";
                    } else {
                        guidedOrder = "Individual";
                    }

                    String printString = String.format("OrderID: %s, Customer ID: %d, Park: %s, Order Date: %s, Order Time: %s, Visitors Number: %d, Group: %s", orderID, accountID, reportRequest.areaOfReport, visitDate.toString(), visitTime.toString(), groupSize, guidedOrder);
                    stringArrayList.add(printString);
//                    writer.write(printString);
//                    writer.write(System.lineSeparator());
                }

                for (String s:stringArrayList){
                    writer.write(s);
                    writer.write(System.lineSeparator());
                }
                writer.close();
            }
            FileInputStream fis = new FileInputStream(reportFile);
            byte[] fileData = new byte[(int) reportFile.length()];
            fis.read(fileData);

            pstmt = DB.getConnection().prepareStatement(fileQuery);
            pstmt.setString(1, fileName);
            pstmt.setBytes(2, fileData);

            pstmt.executeUpdate();
            fis.close();
            return new Message("FileSucceed");


        } catch (SQLException | IOException e) {
            e.printStackTrace();
            return new Message("Error", "File OR SQL failed");
        }
    }

}
