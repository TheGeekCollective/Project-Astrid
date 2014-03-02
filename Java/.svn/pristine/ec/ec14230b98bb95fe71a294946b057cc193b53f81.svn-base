package info.closetgeek.Astrid;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;


public class ArduinoClass implements SerialPortEventListener {
    
    private static final boolean debugToScreen = true;
    private static boolean debugMode = true;
    
    private static boolean validConfig = false;
    
    private static String hostname;
    
    // MQTT Server Details
    private static MQTT commandMQTT; 
    private static final String mqttServer = "10.0.0.200";
    
    // Sensor Data Topic Schema - /Home/<Location>/<Label>/Sensor/<SensorPin>/<Value
    private static String dataTopic = "/Home/";
    private static String commandTopic = "/Home/";


    // Arduino Hardware Details
    private static String portName;
    
    private static final int delayCycle = 5000;

    private static SerialPort serialPort;

    private static BufferedReader input;
    private static OutputStream output;
    
    private static String[] messageParts, dataParts;
    private static int pinNumder;
    
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;

    private static String outputMessage;

    private static String boardLabel, boardLocation, boardModel;
    
    // Data Storage Arrays - Stores Digital state as Boolean and Analogue as int
    private static Boolean[] digitalStatusArray;
    private static int[] analogueDataArray;
    
    private static int numDigitalPins, numAnaloguePins;

    private static String[] analogueDataTypeArray;
    
    private static SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private static SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
    private static SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
    private static SimpleDateFormat logFilenameFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat epochFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    
    public static void insertLogMessage(String _tmpString) {
        try {
            BufferedWriter messageWriter = new BufferedWriter(new FileWriter("logs/" + logFilenameFormat.format(System.currentTimeMillis()).toString() + "-" + boardLabel + "-arduino.log", true));
            messageWriter.write(epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString + "\n");
            messageWriter.close();
            if (debugToScreen) {
                System.out.println("Arduino|" + boardLabel + "|" + epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString);
            }
        } catch (IOException e) {
            System.out.println("Error Writing Log Message...");
        }
    } // End of insertLogMessage Method
    
    private static void mqttSensorData(String _Data) throws IOException {
        // Sensor Data Topic Schema - /Home/<Location>/<Label>/Sensor/<SensorPin>/<Value
        dataTopic = "/Home/"+boardLocation+"/"+hostname+"/"+boardLabel+"/Sensor/";
        Runtime.getRuntime().exec("/usr/bin/mosquitto_pub -h " + mqttServer + " -t " + dataTopic + " -m " + _Data + " --quiet");
        insertLogMessage("--MQTT Pub ->"+"/usr/bin/mosquitto_pub -h " + mqttServer + " -t " + dataTopic + " -m " + _Data + " --quiet");
    }
    
    private static void initialiseCommandListener() throws URISyntaxException {
        MQTT commandMQTT = new MQTT();
        commandMQTT.setHost("10.0.0.200", 1883);
        
        final CallbackConnection commandConnection = commandMQTT.callbackConnection();
        commandConnection.listener(new Listener() {

            @Override
            public void onConnected() {
                insertLogMessage("MQTT Command Connected.");
            }

            @Override
            public void onDisconnected() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void onPublish(UTF8Buffer utfb, Buffer buffer, Runnable r) {
                byte[] myBytes = buffer.toByteArray();
                
                outputMessage = new String(myBytes);
                outputMessage = outputMessage+"\n";
                insertLogMessage("--> MQTT Command Recieved!***********************"+outputMessage);
                insertLogMessage("MQTT Command -> TX.|"+outputMessage+"|");
           //     outputMessage = buffer+"\n";
                try {
                    output.write(outputMessage.getBytes());
                } catch (IOException ex) {
                    insertLogMessage("MQTT Command TX Write Failed.");
                }
                
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        });
        
        commandConnection.connect(new Callback<Void>() {

            @Override
            public void onSuccess(Void t) {
                insertLogMessage("-- Command Callback Success..");
                // Subscribe to a topic
                Topic[] topics = {new Topic("/Home/"+boardLocation+"/"+hostname+"/"+boardLabel+"/Command/", QoS.AT_LEAST_ONCE)};
                //commandTopic = "/Home/"+boardLocation+"/"+boardLabel+"/Command/";
                commandConnection.subscribe(topics, new Callback<byte[]>() {
                    public void onSuccess(byte[] qoses) {
                        // The result of the subcribe request.
                    }

                    public void onFailure(Throwable value) {
                       // commandConnection.close(null); // subscribe failed.
                    }
                });
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
        });
    }
    
    // Public Get/Set Methods
    public void setDebugMode(boolean _mode) {
        debugMode = _mode;
    }
    
    public String getPortName() {
        return portName;
    }

    public void setDigitalStatus(int _digitalPin, Boolean _state) {
        digitalStatusArray[_digitalPin] = _state;
    }

    public Boolean getDigitalStatus(int _digitalPin) {
        return digitalStatusArray[_digitalPin];
    }

    public int getAnalogueStatus(int _analoguePin) {
        return analogueDataArray[_analoguePin];
    }

    public int getNumberOfAnaloguePins() {
        return numAnaloguePins;
    }

    public int getNumberOfDigitalPins() {
        return numDigitalPins;
    }

    private void initialize() {
        CommPortIdentifier portId = null;
        Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

        //First, Find an instance of serial port as set in PORT_NAMES.
        while (portEnum.hasMoreElements()) {
            CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
            //   for (String portName : PORT_NAMES) {
            if (currPortId.getName().equals(portName)) {
                portId = currPortId;
                System.out.println("Port - " + portName);
                break;
            }
            //    }
        }
        if (portId == null) {
            System.out.println("Could not find COM port.");
            return;
        }

        try {
            // open serial port, and use class name for the appName.
            serialPort = (SerialPort) portId.open(this.getClass().getName(),
                    TIME_OUT);

            // set port parameters
            serialPort.setSerialPortParams(DATA_RATE,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);

            // open the streams
            input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
            output = serialPort.getOutputStream();

            // add event listeners
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            
            while (!validConfig) {
                insertLogMessage("Requesting Device Config.");
                outputMessage = "xxx\n";
                output.write(outputMessage.getBytes());
                Thread.sleep(2000);
            }
            
            System.out.println("Connected...");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * This should be called when you stop using the port. This will prevent
     * port locking on platforms like Linux.
     */
    public synchronized void close() {
        if (serialPort != null) {
            serialPort.removeEventListener();
            serialPort.close();
        }
    }

    public synchronized void serialOutput(String _outputString) throws IOException {
        try {
            if (debugMode) insertLogMessage("-->Serial TX->"+_outputString+"<-");
            _outputString += '\n';  //add a newline character
            output.write(_outputString.getBytes());
            output.flush();
            //      Thread.sleep(2000);
        } catch (Exception e) {
            System.out.println("Error in serialOuput Method..");
        }
    }

    /**
     * Handle an event on the serial port. Read the data and print it.
     */
    @Override
    public synchronized void serialEvent(SerialPortEvent oEvent) {
        //       System.out.println("serialEvent Detected"+oEvent.getEventType());
        if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {
                //               System.out.println("Reading....");
                String inputLine = input.readLine();
                System.out.println("SE|" + getPortName() + "|"+boardLabel+"|>" + inputLine + "<");

                if (inputLine.startsWith(".")) {
                    insertLogMessage("Received Hello from Arduino.");
                    insertLogMessage("Requesting Device Config.");
                    outputMessage = "xxx\n";
                    output.write(outputMessage.getBytes());
                    //   System.out.println("Hello");
                }
                
                if (inputLine.startsWith("x")) {
                    insertLogMessage("Received Config from Arduino.");
                    insertLogMessage("------------------------------------------");
                    // x|<Label>|<Location>|<Model>|
                    String[] messageParts = inputLine.split("\\|");
                    boardLabel = messageParts[1];
                    insertLogMessage("---Label    : "+boardLabel);
                    boardLocation = messageParts[2];
                    insertLogMessage("---Location : "+boardLocation);
                    boardModel = messageParts[3];
                    insertLogMessage("---Model    : "+boardModel);
                    insertLogMessage("------------------------------------------");
                    validConfig=true;
                    
                }
                
                if (inputLine.startsWith("|")) {
                    
                    // |<SensorPin>:<Value>|.......|<Source>
                    String[] messageParts = inputLine.split("\\|");

                    mqttSensorData("|"+System.currentTimeMillis()+inputLine);
                    
//                    if (debugMode) {
//                        insertLogMessage("Received Data String from Arduino.");
//                        insertLogMessage("------------------------------------------");
//                        insertLogMessage("-- Raw Data Elements: " + messageParts.length);
//                    }
//                    for (int counter = 0; counter < messageParts.length; counter++) {
//                        if (debugMode) {
//                            insertLogMessage("-- " + counter + "|" + messageParts[counter] + "|");
//                        }
//                    }
//                    if (debugMode) {
//                        insertLogMessage("--");
//                        insertLogMessage("-- Processed Data Elements: " + (messageParts.length - 2));
//                    }
//
//                    for (int counter = 1; counter < (messageParts.length - 1); counter++) {
//                        if (debugMode) {
//                            insertLogMessage("-- " + counter + "|" + messageParts[counter] + "|");
//                        }
                        // Further split Data Element on ":"
                    //    String[] dataParts = messageParts[counter].split(":");
                        // First character of element 0 is A or D - Analogue or Digital
//                        if (dataParts[0].substring(0, 1).equals("A")) {
//                            analogueDataArray[Integer.parseInt(dataParts[0].substring(1))] = Integer.parseInt(dataParts[1]);
//                            mqttSensorData(dataParts[0],dataParts[1]);
//                            if (debugMode) insertLogMessage("-- Analogue Port:"+dataParts[0].substring(1)+" Data |"+dataParts[1]+"|");
//                        }
//                        
//                        if (dataParts[0].substring(0, 1).equals("D")) {
//                            digitalStatusArray[Integer.parseInt(dataParts[0].substring(1))] = Boolean.valueOf(dataParts[1]);
//                            mqttSensorData(dataParts[0],dataParts[1]);
//                            if (debugMode) insertLogMessage("-- Digital  Port:"+dataParts[0].substring(1)+" Data |"+dataParts[1]+"|");
//                        }
                        
               //    }
                    if (debugMode) insertLogMessage("------------------------------------------");
                
            }

            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        // Ignore all the other eventTypes, but you should consider the other ones.
    }

    public ArduinoClass(String _hostname, String _portName, int _numDigitalPins, int _numAnaloguePins) throws URISyntaxException, IOException {
        // Constructor Method

        portName = _portName;
        
        hostname = _hostname;

        numDigitalPins = _numDigitalPins;
        numAnaloguePins = _numAnaloguePins;

        digitalStatusArray = new Boolean[numDigitalPins];
        analogueDataArray = new int[numAnaloguePins];
        analogueDataTypeArray = new String[numAnaloguePins];
        
        insertLogMessage("Arduino Class Starting on |" + portName + "|");
        initialize();
        insertLogMessage("Arduino Class on |" + portName + "| Started...");
        insertLogMessage("Starting MQTT Command Listener.");
        initialiseCommandListener();

    }  // End of Constructor Method

} // End of Main Class
