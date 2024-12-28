import model.ChatMessage;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/downloadMessages")
public class DownloadMessagesServlet extends HttpServlet {

    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Check if user is logged in and has Admin role
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.html?error=unauthorized"); // Redirect to login if not logged in
            return;
        }

        String userRole = (String) session.getAttribute("role"); // Assuming role is stored in session
        String username = (String) session.getAttribute("user");
        if (!"Admin".equalsIgnoreCase(userRole)) {
            session.setAttribute("error", "You do not have permission to download messages.");
            SecureLogger.log("warning", "UNAUTHORIZED MESSAGE DOWNLOAD ATTEMPT", username, "Non-admin user tried to download messages.");
            response.sendRedirect("chat");
            return; // Stop further processing if not an Admin
        }

        List<ChatMessage> messages = getChatMessages(); // Get all chat messages from the DB

        // Set the response content type to text file
        response.setContentType("text/plain");
        // Set the content disposition header to trigger a file download
        response.setHeader("Content-Disposition", "attachment; filename=chat-messages.txt");

        try (PrintWriter out = response.getWriter()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // Define timestamp format

            // Format and write each message to the file
            for (ChatMessage msg : messages) {
                String formattedMessage = String.format("[%s] %s: %s",
                        sdf.format(msg.getTimestamp()), // Format timestamp
                        msg.getUsername(),             // Username
                        msg.getMessage());             // Message content
                out.println(formattedMessage); // Write formatted message
            }
            SecureLogger.log("info", "MESSAGE HISTORY DOWNLOADED", username, "User downloaded message history!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ChatMessage> getChatMessages() {
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM chat_messages ORDER BY timestamp DESC");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String message = rs.getString("message");
                Timestamp timestamp = rs.getTimestamp("timestamp");
                messages.add(new ChatMessage(username, message, timestamp));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }
}
