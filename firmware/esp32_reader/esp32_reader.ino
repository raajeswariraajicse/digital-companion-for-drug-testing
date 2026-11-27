// TraceRx pocket reader: reads color from a TCS34725 sensor and sends "R,G,B" over Bluetooth LE.
// Libraries: Adafruit TCS34725 (Arduino Library Manager). Board: ESP32 Dev Module.
// Wiring: TCS34725 VIN->3V3, GND->GND, SDA->GPIO21, SCL->GPIO22. Sensor LED stays on for constant light.
#include <Wire.h>
#include <Adafruit_TCS34725.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

#define SERVICE_UUID "6e400001-b5a3-f393-e0a9-e50e24dcca9e"
#define CHAR_UUID    "6e400003-b5a3-f393-e0a9-e50e24dcca9e"

Adafruit_TCS34725 tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_154MS, TCS34725_GAIN_4X);
BLECharacteristic *colorChar;
bool deviceConnected = false;

class ServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer *s) { deviceConnected = true; }
  void onDisconnect(BLEServer *s) { deviceConnected = false; s->getAdvertising()->start(); }
};

void setup() {
  Serial.begin(115200);
  if (!tcs.begin()) {
    Serial.println("TCS34725 not found, check wiring");
    while (true) delay(1000);
  }
  BLEDevice::init("TraceRx-Reader");
  BLEServer *server = BLEDevice::createServer();
  server->setCallbacks(new ServerCallbacks());
  BLEService *service = server->createService(SERVICE_UUID);
  colorChar = service->createCharacteristic(CHAR_UUID, BLECharacteristic::PROPERTY_NOTIFY);
  colorChar->addDescriptor(new BLE2902());
  service->start();
  server->getAdvertising()->addServiceUUID(SERVICE_UUID);
  server->getAdvertising()->start();
}

void loop() {
  uint16_t r, g, b, c;
  tcs.getRawData(&r, &g, &b, &c);
  if (c == 0) { delay(200); return; }
  // Normalize by the clear channel. Calibrate against a white card for better accuracy.
  int R = constrain((int)((float)r / c * 255.0), 0, 255);
  int G = constrain((int)((float)g / c * 255.0), 0, 255);
  int B = constrain((int)((float)b / c * 255.0), 0, 255);
  char buf[24];
  snprintf(buf, sizeof(buf), "%d,%d,%d", R, G, B);
  Serial.println(buf);
  if (deviceConnected) {
    colorChar->setValue((uint8_t *)buf, strlen(buf));
    colorChar->notify();
  }
  delay(500);
}
