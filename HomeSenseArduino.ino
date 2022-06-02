#include <ESP8266Webhook.h>
#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <FirebaseArduino.h>
#include <ArduinoJson.h>

char ssid[] = "";          //The network SSID
char password[] = "";     //The network key

#define FIREBASE_HOST "esp8266-c9c43-default-rtdb.firebaseio.com"
#define FIREBASE_AUTH "oL5jOQXjjmxQORo2zRUGCKZ88jx5XinsVfKmqL6X"
#define api_key "kWuFYf711aUP5WNe6bn5STW-gnUf1aSFpFEJ6so0uYJ"      //The Webhook key
#define ifttt_event "ESP8266" //The IFTTT event name
static Webhook webhook(api_key, ifttt_event);

void setup() {
  Serial.begin(9600);
  pinMode(D1, INPUT_PULLUP); //The first magnet sensor
  pinMode(D2, INPUT_PULLUP); //The second magnet sensor
  pinMode(D3, INPUT_PULLUP); //The motion sensor
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(1000);

//Attempt to connect to Wifi network:
  Serial.println("");
  Serial.print("Connecting Wifi: ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print("-");
    delay(500);
  }
  
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  IPAddress ip = WiFi.localIP();
  Serial.println(ip);
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  

}

void addToLog (String name){

StaticJsonBuffer<300> JSONbuffer;   //Declaring static JSON buffer
JsonObject& JSONencoder = JSONbuffer.createObject();
JSONencoder["sensor"] = name;
JSONencoder["type"] = "Alarm";


char JSONmessageBuffer[300];
JSONencoder.prettyPrintTo(JSONmessageBuffer, sizeof(JSONmessageBuffer));
Serial.println(JSONmessageBuffer);

HTTPClient http;  //Declare object of class HTTPClient

http.begin("http://vmedu247.mtacloud.co.il/addLogs");      //Specify request destination
http.addHeader("Content-Type", "application/json");  //Specify content-type header

int httpCode = http.POST(JSONmessageBuffer);   //Send the request
String payload = http.getString();  //Get the response payload

Serial.println(httpCode);   //Print HTTP return code
Serial.println(payload);    //Print request response payload

http.end();  //Close connection
}



void message(String name){
  String phone = Firebase.getString("phone");
  webhook.trigger(phone,name);
}


void loop() {
  bool activate = Firebase.getBool("activate");   //Fetch the system status
  bool sms1 = Firebase.getBool("Sensors/s1/sms"); //Fetch the sms status (for each sensor)
  bool sms2 = Firebase.getBool("Sensors/s2/sms");
  bool sms3 = Firebase.getBool("Sensors/s3/sms");
    if(digitalRead(D1)==HIGH){                    //The first condition to check if the door is open
      Serial.println("Door 1 open");
      Firebase.setBool("Sensors/s1/open",true);   //Write to database that the door is open
      if(activate){                               //Condition to check if its necessary to inform the user
      if(!sms1){                                  //Condition to check if one message already sent
        Firebase.setBool("Sensors/s1/sms",true);
        message(Firebase.getString("Sensors/s1/name"));
        addToLog(Firebase.getString("Sensors/s1/name"));
      }
     }
    }
    else{
      Firebase.setBool("Sensors/s1/open",false);
      Serial.println("Door 1 close");
    }
    
    if(digitalRead(D2)==HIGH){
      Serial.println("Door 2 open");
      Firebase.setBool("Sensors/s2/open",true);
      if(activate){
      if(!sms2){
        Firebase.setBool("Sensors/s2/sms",true);
        message(Firebase.getString("Sensors/s2/name"));
        addToLog(Firebase.getString("Sensors/s2/name"));
      }
     }
    }
    else{
      Firebase.setBool("Sensors/s2/open",false);
      Serial.println("Door 2 close");
    }

    if(digitalRead(D3)==HIGH){
      Serial.println("Someone walking");
      Firebase.setBool("Sensors/s3/open",true);
      if(activate){
      if(!sms3){
        Firebase.setBool("Sensors/s3/sms",true);
        message(Firebase.getString("Sensors/s3/name"));
        addToLog(Firebase.getString("Sensors/s3/name"));
      }
     }
    }
    else{
      Firebase.setBool("Sensors/s3/open",false);
      Serial.println("Clear");
    }
}
