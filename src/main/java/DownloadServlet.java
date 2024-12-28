import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@WebServlet("/uploads/download")
public class DownloadServlet extends HttpServlet {

    private static final String UPLOAD_DIRECTORY = "/static/uploads";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get the filename from the request parameters
        String fileName = request.getParameter("file");

        if (fileName == null || fileName.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name is missing.");
            return;
        }

        // Determine the file's path
        File file = new File(getServletContext().getRealPath(UPLOAD_DIRECTORY), fileName);
        // Check if the user is authorized to download the file (assuming the user uploaded the file)
        String username = (String) request.getSession().getAttribute("user");

        // Check if the file exists
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            SecureLogger.log("warning", "FAILED FILE DOWNLOAD", username, "File Download Failed: " + fileName);
            return;
        }

        // You can modify this check to match the uploader from the database, if needed
        if (username == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You must be logged in to download files.");
            return;
        }

        // Set response headers to indicate a file download
        response.setContentType("application/octet-stream");
        response.setContentLength((int) file.length());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // Copy the file to the response output stream
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            SecureLogger.log("info", "FILE DOWNLOADED", username, "Successful File Download: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error while downloading the file.");
            SecureLogger.log("warning", "FAILED FILE DOWNLOAD", username, "File Download Failed: " + fileName);
        }
    }
}
