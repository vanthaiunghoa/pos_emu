/*
 * PosEmuEngine
 * This is the engine of POS_EMU simulator
 * 
 */
package pos_emu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class PosEmuUtils {
    public enum LogType
    {
        LOG_LEVEL_NONE,
        LOG_LEVEL_ERR,
        LOG_LEVEL_WARNING,
        LOG_LEVEL_INFO,
        LOG_LEVEL_ALL
    }

    private static void DisplayLog(String messageToLog, LogType logType) {
        String logMessage = "";
        String strType;
        
        // Add date/time
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        logMessage = dtf.format(now) + " : ";
        
        // Add module name
        logMessage += "POS_EMU_GUI : ";
        
        // Add log level
        strType = "INFO";
        if (logType == LogType.LOG_LEVEL_ERR) {
            strType = "*ERR";
        } else if (logType == LogType.LOG_LEVEL_WARNING) {
            strType = "WARN";
        }
        logMessage += strType+": ";
        
        // Add the message to log
        logMessage += messageToLog;
        
        // Display the log
        System.out.println(logMessage);
    }
    
    public static void DisplayLogInfo(String messageToLog) {
        DisplayLog(messageToLog, LogType.LOG_LEVEL_INFO);
    }
    
    public static void DisplayLogError(String messageToLog) {
        DisplayLog(messageToLog, LogType.LOG_LEVEL_ERR);
    }

    public static void DisplayLogWarning(String messageToLog) {
        DisplayLog(messageToLog, LogType.LOG_LEVEL_WARNING);
    }
    
}
