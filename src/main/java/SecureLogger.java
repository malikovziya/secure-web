import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class SecureLogger {
    private static final Logger logger = Logger.getLogger("SecureLogger");
    private static final String LOG_FILE_PATH = System.getenv("LOG_FILE_PATH");

    static {
        try {
            // Create the log file if it doesn't exist
            File logFile = new File(LOG_FILE_PATH);
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }

            // Restrict permissions
            logFile.setReadable(false, false);
            logFile.setWritable(false, false);
            logFile.setReadable(true, true);  // Owner can read
            logFile.setWritable(true, true); // Owner can write

            // Set up the file handler for the logger
            FileHandler fileHandler = new FileHandler(LOG_FILE_PATH, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disable console output
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static void log(String log_type, String action, String username, String details) {
        String timestamp = LocalDateTime.now().toString();
        LocalDateTime dateTime = LocalDateTime.parse(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = dateTime.format(formatter);

        String log_text = String.format("[%s] User: %s | Action: %s | Details: %s", formattedDate, username, action, details);
        if (log_type.equalsIgnoreCase("warning")){
            logger.warning(log_text);
        } else if (log_type.equalsIgnoreCase("severe")){
            logger.severe(log_text);
        } else {
            logger.info(log_text);
        }
    }
}
