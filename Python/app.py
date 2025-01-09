from flask import Flask, render_template
import pyodbc

app = Flask(__name__)

# Connection string for SensorDataDB (raw data)
raw_conn_str = (
    "DRIVER={ODBC Driver 18 for SQL Server};"
    "SERVER=localhost\\SQLEXPRESS;"
    "DATABASE=SensorDataDB;"
    "Trusted_Connection=yes;"
    "TrustServerCertificate=yes;"
)

# Connection string for SensorDataCalculated (aggregated data)
agg_conn_str = (
    "DRIVER={ODBC Driver 18 for SQL Server};"
    "SERVER=localhost\\SQLEXPRESS;"
    "DATABASE=SensorDataCalculated;"
    "Trusted_Connection=yes;"
    "TrustServerCertificate=yes;"
)

@app.route('/')
def home():
    return (
        "<h1>Welcome to the Weather Station</h1>"
        "<p>Available Routes:</p>"
        "<ul>"
        "<li><a href='/raw'>View Raw Data</a></li>"
        "<li><a href='/stats'>View Aggregated Data</a></li>"
        "</ul>"
    )

@app.route('/raw')
def show_raw_data():
    # Connect to SensorDataDB
    conn = pyodbc.connect(raw_conn_str)
    cursor = conn.cursor()
    
    # Retrieve the 10 most recent rows from SensorData
    cursor.execute("""
        SELECT TOP 10 
            Temperature0, 
            Humidity, 
            Timestamp
        FROM SensorData
        ORDER BY Timestamp DESC
    """)
    rows = cursor.fetchall()

    cursor.close()
    conn.close()

    # Render a template and pass it the data rows
    return render_template("raw_data.html", rows=rows)

@app.route('/stats')
def show_aggregated_data():
    # Connect to SensorDataCalculated
    conn = pyodbc.connect(agg_conn_str)
    cursor = conn.cursor()

    # Example query: get the 10 most recent daily summaries
    cursor.execute("""
        SELECT TOP 10 
            [Date], 
            MinTemp0, 
            MaxTemp0, 
            AvgTemp0, 
            MinHumidity,
            MaxHumidity,
            AvgHumidity,
            MinTemp1,
            MaxTemp1,
            AvgTemp1,
            MinPressure,
            MaxPressure,
            AvgPressure
        FROM DailyStats
        ORDER BY [Date] DESC
    """)
    stats_rows = cursor.fetchall()

    cursor.close()
    conn.close()

    # Render a template for aggregated data
    return render_template("stats_data.html", stats=stats_rows)

if __name__ == '__main__':
    app.run(debug=True)
