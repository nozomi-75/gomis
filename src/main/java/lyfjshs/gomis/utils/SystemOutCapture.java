package lyfjshs.gomis.utils;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to capture System.out.print statements and also log them to file.
 * This duplicates console output to the log file without interfering with development.
 */
public class SystemOutCapture {
    
    private static final Logger logger = LogManager.getLogger("Console");
    private static final PrintStream originalOut = System.out;
    private static final PrintStream originalErr = System.err;
    private static boolean isCapturing = false;
    
    /**
     * Redirects System.out and System.err to Log4j2 using LoggingOutputStream.
     */
    public static void redirectSystemStreamsToLog4j() {
        if (isCapturing) return;
        System.setOut(new PrintStream(new LoggingOutputStream(logger, Level.INFO), true));
        System.setErr(new PrintStream(new LoggingOutputStream(logger, Level.ERROR), true));
        isCapturing = true;
        logger.info("System.out and System.err are now redirected to Log4j2");
    }
    
    /**
     * Restores the original System.out and System.err streams.
     */
    public static void restoreSystemOut() {
        if (!isCapturing) {
            return; // Not capturing
        }
        
        System.setOut(originalOut);
        System.setErr(originalErr);
        isCapturing = false;
        logger.info("System output capture stopped");
    }
    
    /**
     * Convenience method to log a message to the SystemOut logger.
     * This is useful for capturing messages that would normally go to System.out.
     */
    public static void log(String message) {
        logger.info("[LOG] " + message);
    }
    
    /**
     * Convenience method to log an error message to the SystemOut logger.
     * This is useful for capturing messages that would normally go to System.err.
     */
    public static void logError(String message) {
        logger.error("[LOG] " + message);
    }
    
    /**
     * Captures and logs an exception with its full stack trace to the system log.
     * This is useful for capturing exceptions that would normally be printed to System.err.
     * 
     * @param exception The exception to log
     * @param context Optional context message to include with the exception
     */
    public static void logException(Throwable exception, String context) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        if (context != null && !context.trim().isEmpty()) {
            pw.println("Exception in context: " + context);
        }
        
        exception.printStackTrace(pw);
        pw.close();
        
        logger.error("[EXCEPTION] " + sw.toString());
    }
    
    /**
     * Captures and logs an exception with its full stack trace to the system log.
     * 
     * @param exception The exception to log
     */
    public static void logException(Throwable exception) {
        logException(exception, null);
    }
} 