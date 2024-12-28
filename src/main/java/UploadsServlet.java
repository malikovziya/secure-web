import model.FileRecord;
import java.io.*;
import java.nio.file.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.util.*;
import java.sql.*;

@MultipartConfig(
        fileSizeThreshold = 0
)
@WebServlet("/uploads")
public class UploadsServlet extends HttpServlet {
    private String UPLOAD_DIRECTORY;
    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");

    @Override
    public void init() throws ServletException {
        // Set the upload directory to the path in the 'static/uploads' folder
        UPLOAD_DIRECTORY = "D:\\Ziya\\3rd course\\Web Programming and Security\\CaseStudy\\src\\main\\webapp\\static\\uploads";
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.html?error=unauthorized"); // Redirect to login page if not logged in
            return;
        }

        // Session validation
        String username = (String) request.getSession().getAttribute("user");
        if (username == null) {
            request.getSession().setAttribute("errorMessage", "You must be logged in to upload files.");
            response.sendRedirect(request.getContextPath() + "/login");  // Redirect to login if session is invalid
            return;
        }

        // Get uploaded files from the database
        List<FileRecord> files = getUploadedFiles();
        request.setAttribute("files", files);
        // Forward to the uploads.jsp page
        request.getRequestDispatcher("uploads.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // Session validation
        String username = (String) request.getSession().getAttribute("user");
        if (username == null) {
            request.getSession().setAttribute("errorMessage", "You must be logged in to upload files.");
            response.sendRedirect(request.getContextPath() + "/login");  // Redirect to login if session is invalid
            return;
        }

        if ("upload".equals(action)) {
            handleFileUpload(request, response);
        } else if ("remove".equals(action)) {
            handleFileRemove(request, response);
        }
    }

    private void handleFileRemove(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getParameter("fileName");

        if (fileName == null || fileName.isEmpty()) {
            request.getSession().setAttribute("errorMessage", "Invalid file name.");
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        // Get user role from session
        String username = (String) request.getSession().getAttribute("user");
        String userRole = (String) request.getSession().getAttribute("role");

        // Check if the user has the right role (moderator or admin)
        if (userRole == null || (!userRole.equalsIgnoreCase("Moderator") && !userRole.equalsIgnoreCase("Admin"))) {
            request.getSession().setAttribute("errorMessage", "You do not have permission to remove files.");
            SecureLogger.log("warning", "UNAUTHORIZED FILE REMOVAL", username, "File Removal Failed: " + fileName);
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM file_uploads WHERE file_name = ?")) {
            stmt.setString(1, fileName);

            // Remove from database
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Remove from file system
                File file = new File(UPLOAD_DIRECTORY, fileName);
                if (file.exists() && file.delete()) {
                    request.getSession().setAttribute("successMessage", "File removed successfully.");
                    SecureLogger.log("info", "FILE REMOVED", username, "File removed successfully: " + fileName);
                } else {
                    request.getSession().setAttribute("errorMessage", "Failed to delete file from server.");
                    SecureLogger.log("warning", "FILE REMOVAL FAILED", username, "Failed to delete file from server: " + fileName);
                }
            } else {
                request.getSession().setAttribute("errorMessage", "File not found in database.");
                SecureLogger.log("warning", "FILE REMOVAL FAILED", username, "File not found in database: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "An error occurred while removing the file.");
            SecureLogger.log("warning", "FILE REMOVAL FAILED", username, "An error occurred while removing the file: " + fileName);
        }

        response.sendRedirect(request.getContextPath() + "/uploads");
    }

    private void handleFileUpload(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Part filePart = request.getPart("file");

        // Check if a file is uploaded
        if (filePart == null || filePart.getSize() == 0) {
            request.getSession().setAttribute("errorMessage", "No file selected or file is empty.");
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        String username = (String) request.getSession().getAttribute("user");

        // Manually handle file size validation
        long maxFileSize = 10 * 1024 * 1024; // 10 MB in bytes
        if (filePart.getSize() > maxFileSize) {
            request.getSession().setAttribute("errorMessage", "Maximum file size: 10 MB.");
            SecureLogger.log("warning", "FAILED FILE UPLOAD", username, "Maximum allowed file size: 10 MB: " + fileName);
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        File uploadDir = new File(UPLOAD_DIRECTORY);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File file = new File(uploadDir, fileName);

        // Check if the file already exists
        if (file.exists()) {
            request.getSession().setAttribute("errorMessage", "A file with the same name already exists. Please choose a different name.");
            SecureLogger.log("warning", "FAILED FILE UPLOAD", username, "File Upload Failed (duplicate name): " + fileName);
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        // Save the file
        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            request.getSession().setAttribute("errorMessage", "Failed to upload the file. Please try again.");
            SecureLogger.log("warning", "FAILED FILE UPLOAD", username, "Failed to upload the file: " + fileName);
            response.sendRedirect(request.getContextPath() + "/uploads");
            return;
        }

        saveFileRecord(fileName, username, file.getPath());

        // Redirect to the GET endpoint with a success message
        request.getSession().setAttribute("successMessage", "File uploaded successfully.");
        SecureLogger.log("info", "FILE UPLOADED", username, "File uploaded successfully: " + fileName);
        response.sendRedirect(request.getContextPath() + "/uploads");
    }

    private List<FileRecord> getUploadedFiles() {
        List<FileRecord> files = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM file_uploads ORDER BY timestamp DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String fileName = rs.getString("file_name");
                String uploader = rs.getString("uploader");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                String filePath = rs.getString("file_path"); // Ensure filePath is retrieved
                files.add(new FileRecord(fileName, uploader, timestamp, filePath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return files;
    }

    private void saveFileRecord(String fileName, String uploader, String filePath) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO file_uploads (file_name, uploader, timestamp, file_path) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, fileName);
            stmt.setString(2, uploader);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Add timestamp
            stmt.setString(4, filePath); // Store file path
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
