using System;
using Microsoft.Data.SqlClient;
using System.IO.Ports;

class Program
{
    static void Main(string[] args)
    {
        // Connect string to the SQL Server database
        string connectionString = "Server=ASUS\\SQLEXPRESS;Database=SensorDataDB;Integrated Security=True;TrustServerCertificate=True;";

        // Prompt the user to enter the serial port name
        //Console.WriteLine("Enter the serial port (e.g., COM3):");
        //string portName = Console.ReadLine();
        string portName = "COM3";

        int baudRate = 9600; // Must match the Arduino's baud rate

        SerialPort serialPort = new SerialPort(portName, baudRate);

        try
        {
            // Open serial port
            serialPort.Open();
            Console.WriteLine("Serial port opened. Waiting for data...");

            while (true)
            {
                // Read data from Arduino
                string inputData = serialPort.ReadLine().Trim();
                Console.WriteLine($"Received: {inputData}");

                // Assume data is in the format: "temperature,humidity"
                string[] parts = inputData.Split(',');
                if (parts.Length == 4 
                    && double.TryParse(parts[0], out double temperature0) 
                    && double.TryParse(parts[1], out double humidity) 
                    && double.TryParse(parts[2], out double temperature1) 
                    && double.TryParse(parts[3], out double pressure)
                    )
                {
                    // Insert data into SQL Server
                    using (SqlConnection connection = new SqlConnection(connectionString))
                    {
                        connection.Open();

                        string query = "INSERT INTO SensorData (Temperature0, Humidity, Temperature1, Pressure) " +
                            "VALUES (@Temperature0, @Humidity, @Temperature1, @Pressure)";
                        using (SqlCommand command = new SqlCommand(query, connection))
                        {
                            command.Parameters.AddWithValue("@Temperature0", temperature0);
                            command.Parameters.AddWithValue("@Humidity", humidity);
                            command.Parameters.AddWithValue("@Temperature1", temperature1);
                            command.Parameters.AddWithValue("@Pressure", pressure);
                            int rowsAffected = command.ExecuteNonQuery();

                            Console.WriteLine(rowsAffected > 0
                                ? "Data inserted into database successfully."
                                : "Failed to insert data into database.");
                        }
                    }
                }
                else
                {
                    Console.WriteLine("Invalid data format.");
                }
            }
        }
        catch (Exception ex)
        {
            Console.WriteLine($"Error: {ex.Message}");
        }
        finally
        {
            if (serialPort.IsOpen)
                serialPort.Close();
        }
    }
}
