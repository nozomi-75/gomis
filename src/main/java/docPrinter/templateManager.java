package docPrinter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lyfjshs.gomis.Database.entity.GuidanceCounselor;
import lyfjshs.gomis.components.FormManager.FormManager;
import lyfjshs.gomis.components.settings.SettingsManager;

public class templateManager {
    private static final Logger logger = LogManager.getLogger(templateManager.class);
    
    // Try to detect installation directory, fallback to current directory for development
    private static final String BASE_DEFAULT = getInstallationTemplatePath();
    public static final File USER_TEMPLATE_DIR = new File(System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "GOMIS" + File.separator + "templates");
    private static final File CONFIG_FILE = new File(System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming" + File.separator + "GOMIS" + File.separator + "config.properties");

    /**
     * Get the template path for installed application or development
     */
    private static String getInstallationTemplatePath() {
        // Try 64-bit and 32-bit installation paths first, then development path
        String[] possiblePaths = {
            "C:\\Program Files\\GOMIS\\templates\\",
            "C:\\Program Files (x86)\\GOMIS\\templates\\",
            System.getProperty("user.dir") + File.separator + "templates" + File.separator
        };
        
        for (String path : possiblePaths) {
            File templateDir = new File(path);
            if (templateDir.exists() && templateDir.isDirectory()) {
                logger.info("Found template directory: " + path);
                return path;
            }
        }
        
        // Fallback to current directory for development
        String fallbackPath = System.getProperty("user.dir") + File.separator + "templates" + File.separator;
        logger.warn("No installation template directory found, using fallback: " + fallbackPath);
        return fallbackPath;
    }

    public enum TemplateType {
        GOOD_MORAL("good_moral_template.docx"),
        INCIDENT_REPORT("incident_report_template.docx"),
        DROPPING_FORM("dropping_form_template.docx");

        private final String fileName;
        TemplateType(String fileName) { this.fileName = fileName; }
        public String getFileName() { return fileName; }
    }

    // Export default template to user-specified location
    public static boolean exportDefaultTemplate(TemplateType type, File exportTo) {
        try {
            File defaultTemplate = new File(BASE_DEFAULT + type.getFileName());
            if (!defaultTemplate.exists()) {
                logger.error("Default template not found: " + defaultTemplate.getAbsolutePath());
                return false;
            }
            Files.createDirectories(exportTo.getParentFile().toPath());
            Files.copy(defaultTemplate.toPath(), exportTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Template exported successfully: " + exportTo.getAbsolutePath());
            // Save metadata
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SettingsManager.saveTemplateMetadata(type.name(), exportTo.getAbsolutePath(), now, getCurrentUserName());
            logTemplateAuditAction(type, "EXPORT", exportTo.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.error("Error exporting template", e);
            return false;
        }
    }

    // Import user template and set as active
    public static boolean importCustomTemplate(TemplateType type, File userSelectedDocx) {
        try {
            if (!userSelectedDocx.exists()) {
                logger.error("Selected template file does not exist: " + userSelectedDocx.getAbsolutePath());
                return false;
            }
            
            if (!userSelectedDocx.getName().toLowerCase().endsWith(".docx")) {
                logger.error("Selected file is not a DOCX file: " + userSelectedDocx.getName());
                return false;
            }
            
            USER_TEMPLATE_DIR.mkdirs();
            File userTemplate = new File(USER_TEMPLATE_DIR, type.getFileName());
            Files.copy(userSelectedDocx.toPath(), userTemplate.toPath(), StandardCopyOption.REPLACE_EXISTING);
            setActiveTemplate(type, userTemplate.getAbsolutePath());
            logger.info("Custom template imported successfully: " + userTemplate.getAbsolutePath());
            // Save metadata
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SettingsManager.saveTemplateMetadata(type.name(), userTemplate.getAbsolutePath(), now, getCurrentUserName());
            logTemplateAuditAction(type, "IMPORT", userTemplate.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.error("Error importing custom template", e);
            return false;
        }
    }

    // Set active template in config
    public static void setActiveTemplate(TemplateType type, String templatePath) {
        Properties props = new Properties();
        try {
            if (CONFIG_FILE.exists()) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                    props.load(in);
                }
            }
            props.setProperty("active_template_" + type.name().toLowerCase(), templatePath);
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
                props.store(out, "GOMIS Config");
            }
            logger.info("Active template set for " + type.name() + ": " + templatePath);
            // Save metadata
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SettingsManager.saveTemplateMetadata(type.name(), templatePath, now, getCurrentUserName());
            logTemplateAuditAction(type, "SET_ACTIVE", templatePath);
        } catch (Exception e) {
            logger.error("Error setting active template", e);
        }
    }

    // Get active template (user or fallback to default)
    public static File getActiveTemplate(TemplateType type) {
        Properties props = new Properties();
        try {
            if (CONFIG_FILE.exists()) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                    props.load(in);
                }
                String path = props.getProperty("active_template_" + type.name().toLowerCase());
                if (path != null && new File(path).exists()) {
                    logger.debug("Using user template for " + type.name() + ": " + path);
                    return new File(path);
                }
            }
        } catch (Exception e) {
            logger.error("Error reading template config", e);
        }
        // Fallback to default
        File defaultTemplate = new File(BASE_DEFAULT + type.getFileName());
        logger.debug("Using default template for " + type.name() + ": " + defaultTemplate.getAbsolutePath());
        return defaultTemplate;
    }

    // Delete user template
    public static boolean deleteUserTemplate(TemplateType type) {
        File userTemplate = new File(USER_TEMPLATE_DIR, type.getFileName());
        boolean deleted = userTemplate.exists() && userTemplate.delete();
        if (deleted) {
            logger.info("User template deleted: " + userTemplate.getAbsolutePath());
        }
        return deleted;
    }

    /**
     * Returns the default output folder, which is the user's Desktop directory.
     * This is cross-platform and works on Windows, macOS, and Linux.
     */
    public static File getDefaultOutputFolder() {
        return new File(System.getProperty("user.home") + File.separator + "Desktop");
    }
    
    /**
     * Validate if a template file exists and is accessible
     */
    public static boolean validateTemplate(TemplateType type) {
        File template = getActiveTemplate(type);
        boolean valid = template.exists() && template.canRead();
        logger.debug("Template validation for " + type.name() + ": " + (valid ? "VALID" : "INVALID"));
        return valid;
    }
    
    /**
     * Get template status information
     */
    public static String getTemplateStatus(TemplateType type) {
        File template = getActiveTemplate(type);
        if (!template.exists()) {
            return "Template not found";
        }
        if (!template.canRead()) {
            return "Template not readable";
        }
        return "Template available (" + template.getName() + ")";
    }
    
    /**
     * Reset template to default
     */
    public static boolean resetToDefaultTemplate(TemplateType type) {
        try {
            // Remove user template if it exists
            deleteUserTemplate(type);
            
            // Remove from config
            Properties props = new Properties();
            if (CONFIG_FILE.exists()) {
                try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                    props.load(in);
                }
            }
            props.remove("active_template_" + type.name().toLowerCase());
            try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
                props.store(out, "GOMIS Config");
            }
            
            logger.info("Template reset to default for " + type.name());
            // Save metadata (point to default)
            File defaultTemplate = new File(BASE_DEFAULT + type.getFileName());
            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            SettingsManager.saveTemplateMetadata(type.name(), defaultTemplate.getAbsolutePath(), now, getCurrentUserName());
            logTemplateAuditAction(type, "RESET", defaultTemplate.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.error("Error resetting template to default", e);
            return false;
        }
    }
    
    /**
     * Get all available template types
     */
    public static TemplateType[] getAllTemplateTypes() {
        return TemplateType.values();
    }
    
    /**
     * Check if default templates directory exists and has templates
     */
    public static boolean isDefaultTemplatesAvailable() {
        File defaultDir = new File(BASE_DEFAULT);
        if (!defaultDir.exists() || !defaultDir.isDirectory()) {
            logger.warn("Default templates directory not found: " + defaultDir.getAbsolutePath());
            return false;
        }
        
        // Check if at least one template exists
        for (TemplateType type : TemplateType.values()) {
            File template = new File(defaultDir, type.getFileName());
            if (template.exists()) {
                logger.info("Found default template: " + template.getName());
                return true;
            }
        }
        
        logger.warn("No default templates found in directory: " + defaultDir.getAbsolutePath());
        return false;
    }
    
    /**
     * Initialize template system - called during application startup
     */
    public static void initializeTemplateSystem() {
        logger.info("Initializing template system...");
        logger.info("Base template directory: " + BASE_DEFAULT);
        logger.info("User template directory: " + USER_TEMPLATE_DIR.getAbsolutePath());
        
        // Ensure user template directory exists
        if (!USER_TEMPLATE_DIR.exists()) {
            USER_TEMPLATE_DIR.mkdirs();
            logger.info("Created user template directory");
        }
        
        // Check default templates availability
        if (isDefaultTemplatesAvailable()) {
            logger.info("Default templates are available");
        } else {
            logger.warn("Default templates are not available");
        }
        
        // Validate each template type
        for (TemplateType type : TemplateType.values()) {
            boolean valid = validateTemplate(type);
            logger.info("Template " + type.name() + ": " + (valid ? "VALID" : "INVALID"));
        }
    }

    private static void logTemplateAuditAction(TemplateType type, String action, String filePath) {
        try {
            GuidanceCounselor user = FormManager.staticCounselorObject;
            String userName = (user != null) ? (user.getFirstName() + " " + user.getLastName()) : "Unknown";
            Connection conn = lyfjshs.gomis.Main.settings != null ? lyfjshs.gomis.Main.settings.getClass().getDeclaredField("connection").get(lyfjshs.gomis.Main.settings) instanceof Connection ? (Connection) lyfjshs.gomis.Main.settings.getClass().getDeclaredField("connection").get(lyfjshs.gomis.Main.settings) : null : null;
            if (conn == null) return;
            String sql = "INSERT INTO TEMPLATE_AUDIT_HISTORY (TEMPLATE_TYPE, ACTION, USER, FILE_PATH) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, type.name());
                stmt.setString(2, action);
                stmt.setString(3, userName);
                stmt.setString(4, filePath);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            logger.error("Error logging template audit action", e);
        }
    }

    private static String getCurrentUserName() {
        GuidanceCounselor user = FormManager.staticCounselorObject;
        return (user != null) ? (user.getFirstName() + " " + user.getLastName()) : "Unknown";
    }
}