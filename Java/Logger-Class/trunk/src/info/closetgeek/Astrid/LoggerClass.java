package info.closetgeek.Astrid;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.UTF8Buffer;
import org.fusesource.mqtt.client.Callback;
import org.fusesource.mqtt.client.CallbackConnection;
import org.fusesource.mqtt.client.Listener;
import org.fusesource.mqtt.client.MQTT;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;

public class LoggerClass {

    private static final boolean debugToScreen = true;
    private static boolean debugMode = true;
    
    private static String arduinoLabel;
    
        // MQTT Server Details
    private static MQTT connectMQTT; 
    private static final String mqttServer = "10.0.0.200";
    private static String outputMessage;
    private static String[] topicParts;
    
    // MySQL ---------
    private static Connection connect = null;
    private static Statement statement = null;
    private static PreparedStatement preparedStatement = null;
    private static ResultSet resultSet = null;
    
    private static String databaseHost = "10.0.0.200";
    private static String databaseUsername = "marvin";
    private static String databasePassword = "shutdown";

    // End of MySQL --
    
    // Node Configuration Details - Populated from central database
    private static String nodeName;
    private static String nodeHost;
    private static int numberOfDigitalPins, numberOfAnaloguePins, nodeID;
    
    private static String[] sensorElements;

    private static SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private static SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
    private static SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
    private static SimpleDateFormat logFilenameFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat epochFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");

    // Public Get/Set Methods
    public void setDebugMode(boolean _mode) {
        debugMode = _mode;
    }
    
    
    public static void insertLogMessage(String _tmpString) {
        try {
            if (debugToScreen) {
               System.out.println("Logger|" + arduinoLabel + "|" + epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString);
            }
            BufferedWriter messageWriter = new BufferedWriter(new FileWriter("logs/" + logFilenameFormat.format(System.currentTimeMillis()).toString() + "-" + arduinoLabel + "-logger.log", true));
            messageWriter.write(epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString + "\n");
            messageWriter.close();
            if (debugToScreen) {
                //       System.out.println("Arduino|" + boardLabel + "|" + epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString);
            }
        } catch (IOException e) {
            System.out.println("Error Writing Log Message...");
        }
    } // End of insertLogMessage Method

    
    public static void connectToDatabase() throws ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        try {
            insertLogMessage("Connecting to Database....");
            connect = (Connection) DriverManager.getConnection("jdbc:mysql://"+databaseHost+"/Astrid_HA?user="+databaseUsername+"&password="+databasePassword);
            insertLogMessage("Connected!!");
        } catch (SQLException ex) {
            insertLogMessage("Failed to connect to Database.");
        }
        
    }
    
    public static void writeDataElementToDB(String _topic, String _data) {
        insertLogMessage("--> Writing Data Element to Database");
        String[] sensorElements = _data.split("\\|");
        
        for (int counter = 0; counter < sensorElements.length; counter++) {
            if (debugMode) {
                insertLogMessage("-- " + counter + "|" + sensorElements[counter] + "|");
            }
        }
        
                           if (debugMode) {
                        insertLogMessage("--");
                        insertLogMessage("-- Processed Data Elements: " + (sensorElements.length - 2));
                    }

                    for (int counter = 1; counter < (sensorElements.length - 1); counter++) {
                        if (debugMode) {
                            insertLogMessage("-- " + counter + "|" + sensorElements[counter] + "|");
                        }
                        // Further split Data Element on ":"
                        String[] dataParts = sensorElements[counter].split(":");
                        // First character of element 0 is A or D - Analogue or Digital
                        if (dataParts[0].substring(0, 1).equals("A")) {
                            if (debugMode) insertLogMessage("-- Analogue Port:"+dataParts[0].substring(1)+" Data |"+dataParts[1]+"| Type |"+dataParts[2]+"|");
                            try {
                                insertLogMessage("---> Preparing SQL Insert");
                        //        preparedStatement = (PreparedStatement) connect.prepareStatement("insert into sesnorData values (default, ?, ?, ?, ?, ?, ?, ?, ?)");
                                preparedStatement = (PreparedStatement) connect.prepareStatement("insert into sensorData set nodeName=?");
                                preparedStatement.setString(1, nodeName);
                         //       preparedStatement.setString(2, dataParts[0].substring(1));
                         //       preparedStatement.setInt(3, Integer.parseInt(dataParts[1]));
                         //       preparedStatement.setInt(4, 99);
                         //       preparedStatement.setString(5, dataParts[2]);
                         //       preparedStatement.setInt(6, 22);
                         //       preparedStatement.setString(7, dataParts[2]);
                            //    preparedStatement.setDate(8, 77);
                                preparedStatement.executeUpdate();
                            } catch (SQLException ex) {
                                insertLogMessage("---> SQL Analogue Data Insert Failed!! "+ex.toString());
                            }
                        }
                        
                        if (dataParts[0].substring(0, 1).equals("D")) {
                    //        digitalStatusArray[Integer.parseInt(dataParts[0].substring(1))] = Boolean.valueOf(dataParts[1]);
                    //        mqttSensorData(dataParts[0],dataParts[1]);
                            if (debugMode) insertLogMessage("-- Digital  Port:"+dataParts[0].substring(1)+" Data |"+dataParts[1]+"| Type |"+dataParts[2]+"|");
                        }
                        
                   }
        
        
        
     //   preparedStatement = connect.preparedStatement();

    }
    
    public static void getNodeConfiguration() {
        insertLogMessage("-> Requesting Node Config from DB");
        try {
            preparedStatement = (PreparedStatement) connect.prepareStatement("select * from nodes where name = ?");
            preparedStatement.setString(1, arduinoLabel);
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            insertLogMessage("-> Received Config for Node |"+resultSet.getString("name")+"|");
            nodeName = resultSet.getString("name");
            nodeID = resultSet.getInt("id");
            nodeHost = resultSet.getString("host");
            numberOfAnaloguePins = resultSet.getInt("analogue");
            numberOfDigitalPins = resultSet.getInt("digital");
            insertLogMessage("--> Config |"+nodeID+"|"+nodeName+"|"+nodeHost+"|"+numberOfAnaloguePins+"|"+numberOfDigitalPins+"|");
        } catch (SQLException ex) {
            insertLogMessage("-> Database Config Request Failed!"+ex);
        }
    }
    
        private static void initialiseMQTTListener() throws URISyntaxException {
        MQTT connectMQTT = new MQTT();
        connectMQTT.setHost("10.0.0.200", 1883);
        
        final CallbackConnection commandConnection = connectMQTT.callbackConnection();
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
                insertLogMessage("--> MQTT Message Recieved!"+outputMessage+utfb.toString());
                
                // First lets split the source topic in to its components
                String[] topicParts = utfb.toString().split("/");
                
                if (topicParts[5].equals("Sensor")) {
                    writeDataElementToDB(utfb.toString(), outputMessage);
                }
                
                
           //     insertLogMessage("MQTT Command -> TX.|"+outputMessage+"|");
           //     outputMessage = buffer+"\n";
//                try {
//                    output.write(outputMessage.getBytes());
//                } catch (IOException ex) {
//                    insertLogMessage("MQTT Command TX Write Failed.");
//                }
                
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
                Topic[] topics = {new Topic("/Home/Geek-Lair"+"/"+nodeHost+"/"+nodeName+"/#", QoS.AT_LEAST_ONCE)};
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
    
    public LoggerClass(String _arduinoLabel) throws ClassNotFoundException, URISyntaxException, InterruptedException {
        // Main Class Constructor!
        arduinoLabel = _arduinoLabel;
        connectToDatabase();
        getNodeConfiguration();
        initialiseMQTTListener();
 //       while(true) {
            Thread.sleep(20000);
 //       }
    } // End of Main Class Constructor
}
