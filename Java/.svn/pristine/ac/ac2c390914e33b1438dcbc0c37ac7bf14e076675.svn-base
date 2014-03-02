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
import static java.lang.Boolean.TRUE;
import java.text.SimpleDateFormat;
import java.util.Enumeration;


public class astridNodeDaemon {
    
    private static final boolean debugToScreen = true;
    private static boolean debugMode = true;
    
    private static ArduinoClass myArduino;
    
    private static String daemonVersion = "0.1a";
    private static String hostname;

    private static boolean runningFlag = TRUE;
    private static int delayCycle = 5000;

    private BufferedReader input;
    private OutputStream output;
    private static final int TIME_OUT = 2000;
    private static final int DATA_RATE = 9600;
    
    private static String outputMessage;
    
    private static SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private static SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
    private static SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
    private static SimpleDateFormat logFilenameFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat epochFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    
    
    public static String getHostname() throws IOException {
        String line;
        Process p = Runtime.getRuntime().exec("hostname");
        BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        System.out.println("OUTPUT");
        line = input.readLine();
        System.out.println(line);
        input.close();

        return line;
    }
    
    public static void insertLogMessage(String _tmpString) {
        try {
            BufferedWriter messageWriter = new BufferedWriter(new FileWriter("logs/" + logFilenameFormat.format(System.currentTimeMillis()).toString() + "-daemon.log", true));
            messageWriter.write(epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString + "\n");
            messageWriter.close();
            if (debugToScreen) {
                System.out.println("Daemon:"+epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString);
            }
        } catch (IOException e) {
            System.out.println("Error Writing Log Message...");
        }
    } // End of insertLogMessage Method
    
    public static void setDebugMode(boolean _mode) {
        debugMode = _mode;
    }

    public static void main(String[] args) throws Exception {
        
        insertLogMessage("Astrid Node Daemon Started - v"+daemonVersion);
        
        hostname = getHostname();
        
        myArduino = new ArduinoClass(hostname,"/dev/ttyUSB0",6,20);
        
        while (runningFlag) {
            System.out.println("LED ON!");
            outputMessage = "led1";
            myArduino.serialOutput(outputMessage);
            Thread.sleep(delayCycle);
            outputMessage = "..";
            System.out.println("Polled..|"+outputMessage+"|");
            myArduino.serialOutput(outputMessage);
            Thread.sleep(delayCycle);
            System.out.println("LED OFF!");
            outputMessage = "led0";
            myArduino.serialOutput(outputMessage);
            Thread.sleep(delayCycle);
            
        }
        
    }

}
