package com.WeatherStation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class WeatherLog {

    public static void main(String[] args) {

        // -- 1. Define your database connection strings
        // Raw data DB (SensorData)
        String rawDbUrl = "jdbc:sqlserver://localhost:1433;"
                        + "database=sensorDataDB;"
                        + "integratedSecurity=true;"
                        + "trustServerCertificate=true;";

        // Aggregated DB (DailyStats) in sensorDataCalculated
        String aggDbUrl = "jdbc:sqlserver://localhost:1433;"
                        + "database=sensorDataCalculated;"
                        + "integratedSecurity=true;"
                        + "trustServerCertificate=true;";

        // -- 2. Define the date you want to calculate stats for
        LocalDate targetDate = LocalDate.now().minusDays(1);
        String dateStr = targetDate.toString(); // e.g., "2024-12-24"

        // -- 3. Query to get min, max, avg from the raw SensorData
        String selectQuery =
            "SELECT "
            + "  MIN(Temperature0) AS MinTemp0, MAX(Temperature0) AS MaxTemp0, AVG(Temperature0) AS AvgTemp0, "
            + "  MIN(Humidity)     AS MinHumidity, MAX(Humidity)     AS MaxHumidity,     AVG(Humidity)     AS AvgHumidity, "
            + "  MIN(Temperature1) AS MinTemp1,   MAX(Temperature1) AS MaxTemp1,       AVG(Temperature1) AS AvgTemp1, "
            + "  MIN(Pressure)     AS MinPressure, MAX(Pressure)     AS MaxPressure,    AVG(Pressure)     AS AvgPressure "
            + "FROM SensorData "
            + "WHERE Timestamp >= '" + dateStr + "' "
            + "  AND Timestamp < DATEADD(day, 1, '" + dateStr + "');";

        // -- 4. Insert statement for the aggregated database
        String insertQuery =
            "INSERT INTO DailyStats (Date, "
            + "  MinTemp0, MaxTemp0, AvgTemp0, "
            + "  MinHumidity, MaxHumidity, AvgHumidity, "
            + "  MinTemp1, MaxTemp1, AvgTemp1, "
            + "  MinPressure, MaxPressure, AvgPressure) "
            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        // -- 4b. Check if there's already an entry for the day
        String checkQuery =
            "SELECT COUNT(*) AS RecordCount "
            + "FROM DailyStats "
            + "WHERE Date = ?;";

        // -- 5. Use try-with-resources to handle connections
        try (Connection rawConn = DriverManager.getConnection(rawDbUrl);
             Connection aggConn = DriverManager.getConnection(aggDbUrl))
        {
            // 5a. First check if today's stats are already calculated
            try (PreparedStatement checkStmt = aggConn.prepareStatement(checkQuery)) {
                checkStmt.setDate(1, java.sql.Date.valueOf(targetDate));
                try (ResultSet rsCheck = checkStmt.executeQuery()) {
                    if (rsCheck.next()) {
                        int count = rsCheck.getInt("RecordCount");
                        if (count > 0) {
                            System.out.println("Data for " + dateStr + " is already calculated.");
                            return; // Stop execution if record already exists
                        }
                    }
                }
            }

            // 5b. Query raw database for aggregated stats
            double minTemp0 = 0, maxTemp0 = 0, avgTemp0 = 0;
            double minHum   = 0, maxHum   = 0, avgHum   = 0;
            double minTemp1 = 0, maxTemp1 = 0, avgTemp1 = 0;
            double minPres  = 0, maxPres  = 0, avgPres  = 0;

            try (PreparedStatement selectStmt = rawConn.prepareStatement(selectQuery);
                 ResultSet rs = selectStmt.executeQuery())
            {
                if (rs.next()) {
                    minTemp0 = rs.getDouble("MinTemp0");
                    maxTemp0 = rs.getDouble("MaxTemp0");
                    avgTemp0 = rs.getDouble("AvgTemp0");
                    avgTemp0 = Math.round(avgTemp0 * 100.0) / 100.0;

                    minHum   = rs.getDouble("MinHumidity");
                    maxHum   = rs.getDouble("MaxHumidity");
                    avgHum   = rs.getDouble("AvgHumidity");
                    avgHum = Math.round(avgHum * 100.0) / 100.0;

                    minTemp1 = rs.getDouble("MinTemp1");
                    maxTemp1 = rs.getDouble("MaxTemp1");
                    avgTemp1 = rs.getDouble("AvgTemp1");
                    avgTemp1 = Math.round(avgTemp1 * 100.0) / 100.0;

                    minPres  = rs.getDouble("MinPressure");
                    maxPres  = rs.getDouble("MaxPressure");
                    avgPres  = rs.getDouble("AvgPressure");
                    avgPres = Math.round(avgPres * 100.0) / 100.0;
                } else {
                    System.out.println("No data found for date: " + dateStr);
                    return; // Exit if no data
                }
            }

            // 5c. Insert into the aggregated database if no record found
            try (PreparedStatement insertStmt = aggConn.prepareStatement(insertQuery)) {
                insertStmt.setDate(1, java.sql.Date.valueOf(targetDate));

                insertStmt.setDouble(2,  minTemp0);
                insertStmt.setDouble(3,  maxTemp0);
                insertStmt.setDouble(4,  avgTemp0);

                insertStmt.setDouble(5,  minHum);
                insertStmt.setDouble(6,  maxHum);
                insertStmt.setDouble(7,  avgHum);

                insertStmt.setDouble(8,  minTemp1);
                insertStmt.setDouble(9,  maxTemp1);
                insertStmt.setDouble(10, avgTemp1);

                insertStmt.setDouble(11, minPres);
                insertStmt.setDouble(12, maxPres);
                insertStmt.setDouble(13, avgPres);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Daily stats inserted successfully for " + dateStr);
                } else {
                    System.out.println("Failed to insert daily stats for " + dateStr);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
