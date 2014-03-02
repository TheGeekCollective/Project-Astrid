package info.closetgeek.Astrid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;


public class AstridServerDaemon {

    private static Boolean debugMode = true;
    private static Boolean debugToScreen = true;
    
    private static LoggerClass logger;

    private static SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private static SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
    private static SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
    private static SimpleDateFormat logFilenameFormat = new SimpleDateFormat("yyyyMMdd");
    private static SimpleDateFormat epochFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
    
    public static void insertLogMessage(String _tmpString) {
        try {
            if (debugToScreen) {
                System.out.println("Logger:" + epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString);
            }
            BufferedWriter messageWriter = new BufferedWriter(new FileWriter("logs/" + logFilenameFormat.format(System.currentTimeMillis()).toString() + "-logger.log", true));
            messageWriter.write(epochFormat.format(System.currentTimeMillis()).toString() + "|" + _tmpString + "\n");
            messageWriter.close();

        } catch (IOException e) {
            System.out.println("Error Writing Log Message...");
        }
    } // End of insertLogMessage Method

    public static void setDebugMode(boolean _mode) {
        debugMode = _mode;
    }


    
    public static void main(String[] args) throws ClassNotFoundException, URISyntaxException, InterruptedException {
        logger = new LoggerClass("Olivia");
    }
    
}
