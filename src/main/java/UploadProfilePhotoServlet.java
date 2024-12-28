import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

@WebServlet("/uploadProfilePhoto")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,  // 2MB threshold for memory storage
        maxFileSize = 1024 * 1024 * 50,        // 5MB maximum file size
        maxRequestSize = 1024 * 1024 * 100     // 50MB maximum request size
)
public class UploadProfilePhotoServlet extends HttpServlet {

    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");
    String directoryPath = System.getenv("CASE_STUDY_IMAGES_DIR");

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "GET method is not allowed");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart = request.getPart("profilePhoto");  // Retrieve the file part from the request
        HttpSession session = request.getSession(false);

        if (session != null && filePart != null) {
            String username = (String) session.getAttribute("user");

            // Check if the uploaded file is an image and its size
            String contentType = filePart.getContentType();
            String statusMessage;
            String messageType;

            // Get the value of the checkbox to enable or disable malware scan
            boolean enableMalwareScan = request.getParameter("enableMalwareScan") != null;

            if (isImage(contentType) && filePart.getSize() <= 5 * 1024 * 1024) {  // Max 5MB
                // Save the file to the server
                String fileName = saveFileToServer(filePart);
                // Perform malware scan if enabled
                if (enableMalwareScan && scanForMalware(fileName)) {
                    updateProfilePhoto(username, fileName);
                    statusMessage = "Successfully uploaded the profile photo.";
                    messageType = "success";
                } else if (!enableMalwareScan) {
                    // No scan performed, just upload the photo
                    updateProfilePhoto(username, fileName);
                    statusMessage = "Profile photo uploaded without malware scan.";
                    messageType = "success";
                } else {
                    // File is infected, delete it
                    File file = new File(directoryPath, fileName.substring(fileName.lastIndexOf("/") + 1));
                    file.delete();
                    statusMessage = "Malware detected in the uploaded file. Upload failed.";
                    messageType = "error";
                }
            } else {
                // Handle file size or type error
                if (filePart.getSize() > 5 * 1024 * 1024) {
                    statusMessage = "File is too large. Maximum allowed size is 5MB.";
                } else {
                    statusMessage = "Invalid file type. Only images are allowed.";
                }
                messageType = "error";
            }

            request.setAttribute("statusMessage", statusMessage);
            request.setAttribute("messageType", messageType);
            request.getRequestDispatcher("uploadProfilePhoto.jsp").forward(request, response);  // Redirect to the status page
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
        }
    }

    // Check if the file content type is one of the allowed image types
    private boolean isImage(String contentType) {
        return ALLOWED_FILE_TYPES.contains(contentType);
    }

    // Save the uploaded file to the server
    private String saveFileToServer(Part filePart) throws IOException {
        String fileName = filePart.getSubmittedFileName();
        if (directoryPath == null) {
            throw new IllegalStateException("Environment variable IMAGE_DIRECTORY_PATH is not set");
        }
        File uploads = new File(directoryPath);
        if (!uploads.exists()) {
            uploads.mkdirs();  // Create the directory if it doesn't exist
        }

        File file = new File(uploads, fileName);
        filePart.write(file.getAbsolutePath());  // Save the file

        return "images/" + fileName;  // Return the relative path
    }

    // Scan the uploaded file for malware
    private boolean scanForMalware(String fileName) {
        File scanFile = new File(directoryPath, fileName.substring(fileName.lastIndexOf("/") + 1));
        System.out.println("Scanning file -> " + scanFile.getAbsolutePath());
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "C:\\Program Files (x86)\\ClamWin\\bin\\clamscan.exe", // Correct path
                    "--no-summary",
                    scanFile.getAbsolutePath()
            );
            System.out.println("ProcessBuilder initialized.");

            Process process = builder.start();
            System.out.println("ClamAV process started.");

            // Consume output streams to prevent blocking
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("ClamAV Output: " + line);
                }
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("ClamAV Error: " + line);
                }
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("ClamAV scan completed with exit code -> " + exitCode);

            return exitCode == 0;  // Exit code 0 indicates no malware
        } catch (IOException e) {
            System.err.println("IOException occurred during malware scan: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            System.err.println("Process was interrupted: " + e.getMessage());
            e.printStackTrace();
            Thread.currentThread().interrupt();  // Reset interrupt flag
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected exception occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Update the profile photo in the database
    private void updateProfilePhoto(String username, String filePath) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "UPDATE users SET profile_photo = ? WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, filePath);
            stmt.setString(2, username);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();  // Log the error
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
