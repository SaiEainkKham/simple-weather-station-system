// Include the DHT11 library for interfacing with the sensor.
#include <DHT11.h>

#include <HP20x_dev.h>
#include "Arduino.h"
#include "Wire.h"
#include <KalmanFilter.h>
unsigned char ret = 0;

// Create an instance of the DHT11 class.
// - For Arduino: Connect the sensor to Digital I/O Pin 2.
// - For ESP32: Connect the sensor to pin GPIO2 or P2.
// - For ESP8266: Connect the sensor to GPIO2 or D4.
DHT11 dht11(2);


void setup() {
    Serial.begin(9600);        // start serial for output
    delay(150);
    /* Reset HP20x_dev */
    HP20x.begin();
    delay(100);
}


void loop() {
    int temperature = 0;
    int humidity = 0;

    // Attempt to read the temperature and humidity values from the DHT11 sensor.
    int result = dht11.readTemperatureHumidity(temperature, humidity);

    // Check the results of the readings.
    // If the reading is successful, print the temperature and humidity values.
    // If there are errors, print the appropriate error messages.
    if (result == 0) {
      // Print temperature (.C) and humidity (%) from 'Grove - Temperature&Humidity Sensor (DHT11)'
      // https://wiki.seeedstudio.com/Grove-TemperatureAndHumidity_Sensor/
        Serial.print(temperature);
        Serial.print(",");
        Serial.print(humidity);
        Serial.print(",");
    } else {
        // Print error message based on the error code.
        Serial.println(DHT11::getErrorString(result));
    }


    // Print another temperature and atmospheric pressure from 'Grove - Barometer (High-Accuracy)'
    // https://wiki.seeedstudio.com/Grove-Barometer-High-Accuracy/
    long Temper = HP20x.ReadTemperature();
    long Pressure = HP20x.ReadPressure();

    // Print temperature (.C)
    float t = Temper / 100.0;
    Serial.print(t);
    Serial.print(",");

    // Print pressure (hPa)
    t = Pressure / 100.0;
    Serial.println(t);

    delay(1000);
}
