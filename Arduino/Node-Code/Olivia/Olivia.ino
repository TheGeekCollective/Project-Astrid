
String deviceLabel = "Olivia";
String deviceLocation = "Geek-Lair";
String deviceType = "Arduino-Nano";

// -----------------------------------------------------

int counter = 0;

int numberOfAnaloguePins = 6;

int lightSensorPin = 1;
int tempSensorPin = 0;

int analogueSensorRawData[6];

int analogueInputs[] = {0,1,2,3,4,5};
int analogueData[] = {0,0,0,0,0,0};
// This array denotes the Sensor Type - i.e L = Light, T = Temp
char analogueSensorTypeData[] = {'T','T','T','T','T','L'};

int led = 13;

char serialReadString[50];//stores the recieved characters
int stringPosition=-1;//stores the current position of my serialReadString

int inByte = 0;         // incoming serial byte

void setup()
{
  pinMode(led, OUTPUT);
  
  // start serial port at 9600 bps:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }

 establishContact();  // send a byte to establish contact until receiver responds 
}

void loop()
{
    while (Serial.available() > 0){ // As long as there's more to read, keep reading
      int inByte = Serial.read();// Read next byte
      stringPosition++;//increase the position in the string
      if(inByte=='\n'){//if it's my terminating character
         serialReadString[stringPosition] = 0;//set current position to Null to terminate the String
      }else{//if it's not a terminating character
          serialReadString[stringPosition] = inByte; // Save the character in a character array
      }
  }
     if (stringPosition>0) {
       stringPosition=-1;//set the string position to -1
      if (serialReadString[0] == '.') {
        for (counter = 0; counter<numberOfAnaloguePins; counter = counter + 1) {
          Serial.print("|A");
          Serial.print(analogueInputs[counter], DEC);
          Serial.print(":");
          Serial.print(analogueData[counter], DEC);
          Serial.print(":");
          Serial.print(analogueSensorTypeData[counter]);
        }
        Serial.println("|P");     
      }
      
      if (serialReadString[0] == 'x') {
        Serial.println("x|"+deviceLabel+"|"+deviceLocation+"|"+deviceType+"|");    
      }
      
      if (serialReadString[0] == 'l' && serialReadString[1] == 'e' && serialReadString[2] == 'd') {
        // Test/Debug Command - "led<on/off>" - "led1", "led0"
        
        if (serialReadString[3] == '1') {
          digitalWrite(led, HIGH);
        }
        
        if (serialReadString[3] == '0') {
          digitalWrite(led, LOW);
        }
      }   
    }
    
    // Read Analogue Sensors - Using pre-defined arrays
    for (counter = 0; counter<numberOfAnaloguePins; counter = counter + 1) {
      analogueData[counter] = analogRead(analogueInputs[counter]);
    }
    
    for (counter = 0; counter<numberOfAnaloguePins; counter = counter + 1) {
      Serial.print("|A");
      Serial.print(analogueInputs[counter], DEC);
      Serial.print(":");
      Serial.print(analogueData[counter], DEC);
      Serial.print(":");
      Serial.print(analogueSensorTypeData[counter]);
    }
    
    Serial.println("|A");  
        
        
    delay(500);    
    
}

void establishContact() {
  digitalWrite(led, HIGH);
  while (Serial.available() <= 0) {
    Serial.println('.');   // send a capital A
    delay(1000);
  }
  digitalWrite(led, LOW);
}

