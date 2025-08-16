package LEGACY_test_unused;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.utils.SystemOutCapture;

/**
 * Simple test class to demonstrate the logging system.
 * This shows how console output is duplicated to the log file.
 */
public class LoggingTest {
    
    private static final Logger logger = LogManager.getLogger(LoggingTest.class);
    
    public static void main(String[] args) {
        // Start capturing console output
        SystemOutCapture.redirectSystemStreamsToLog4j();
        
        System.out.println("=== GOMIS Logging Test ===");
        System.out.println("This text will appear in both console and log file");
        
        // Test different log levels
        logger.info("This is an INFO message");
        logger.debug("This is a DEBUG message");
        logger.warn("This is a WARNING message");
        logger.error("This is an ERROR message");
        
        // Test exception logging
        try {
            throw new RuntimeException("Test exception");
        } catch (Exception e) {
            System.err.println("Caught exception: " + e.getMessage());
            SystemOutCapture.logException(e, "Test exception in LoggingTest");
        }
        
        // Test direct logging methods
        SystemOutCapture.log("This is a direct log message");
        SystemOutCapture.logError("This is a direct error message");
        
        System.out.println("=== Test completed ===");
        System.out.println("Check the log file at: " + System.getProperty("user.home") + "/GOMIS/logs/gomis.log");
        
        // Stop capturing
        SystemOutCapture.restoreSystemOut();
        System.out.println("System output capture stopped - this will only appear in console");
    }
} 