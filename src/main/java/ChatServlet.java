import model.ChatMessage;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

@WebServlet("/chat")
public class ChatServlet extends HttpServlet {
    // retrieve db credentials from environment variables
    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");
    private static final Logger logger = Logger.getLogger(ChatServlet.class.getName());

    // Create a reusable policy for sanitizing input
    private static final PolicyFactory POLICY = new HtmlPolicyBuilder()
            .allowElements("b", "i", "u", "strong", "em")
            .toFactory();

    @Override
    public void init() throws ServletException {
        try {
            // Register MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ServletException("Database connection error.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.html");
            return;
        }

        String message = request.getParameter("message");
        if (message != null && !message.trim().isEmpty()) {
            String username = (String) session.getAttribute("user");
            if (!isValidInput(message)) {
                // If input is invalid (XSS attempt), set an error message and redirect
                session.setAttribute("error", "Invalid input detected. Please try again.");
                logger.warning("Malicious input detected!");
                SecureLogger.log("severe", "MALICIOUS INPUT", username, "Malicious input detected: " + message);

//              Send Email when user tries to send malicious message
                String subject_line = "Suspicious Activity Detected";
                String message_body = "User tried to send malicious message to chat.\nUsername: " + username + "\nInput: " + message;
                EmailAlert.sendEmail(subject_line, message_body);

                response.sendRedirect("chat");
                return;
            }
            saveMessage(request, response, username, message);
        } else {
            session.setAttribute("error", "Message cannot be empty.");
            logger.warning("Empty message detected!");
        }

        // Redirect to avoid form resubmission
        response.sendRedirect("chat");
    }

    // Validation method to check if input is safe
    private boolean isValidInput(String message) {
        // Sanitize the message using the OWASP sanitizer policy
        String sanitizedMessage = POLICY.sanitize(message);
        // If sanitized message is different, it indicates an XSS attempt
        return sanitizedMessage.equals(message);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.html?error=unauthorized"); // Redirect to login page if not logged in
            return;
        }

        String keyword = request.getParameter("keyword");

        // Check if the keyword is valid
        if (keyword != null && !keyword.trim().isEmpty()) {
            // Sanitize the keyword to avoid XSS or SQL injection
            PolicyFactory policy = Sanitizers.FORMATTING;
            String sanitizedKeyword = policy.sanitize(keyword);

            // If the sanitized keyword is different from the original, it's invalid input
            if (!sanitizedKeyword.equals(keyword)) {
                session.setAttribute("error", "Invalid input detected. Please try again.");
                response.sendRedirect("chat"); // Redirect back with error
                return; // Do not filter messages
            }

            List<ChatMessage> messages = searchMessagesByKeyword(sanitizedKeyword);
            request.setAttribute("messages", messages);
        } else {
            // If no keyword is provided, show all messages
            List<ChatMessage> messages = getChatMessages();
            request.setAttribute("messages", messages);
        }

        request.getRequestDispatcher("dashboard.jsp").forward(request, response);
    }

    private void saveMessage(HttpServletRequest request, HttpServletResponse response, String username, String message)
            throws IOException {
        // Create a policy factory that allows only safe HTML
        PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS);

        // Sanitize the input message to remove any dangerous content
        String sanitizedMessage = policy.sanitize(message);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO chat_messages (username, message) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, sanitizedMessage); // Save sanitized input
            stmt.executeUpdate();
        } catch (Exception e) {
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

    private List<ChatMessage> searchMessagesByKeyword(String keyword) {
        List<ChatMessage> messages = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM chat_messages WHERE message LIKE ? ORDER BY timestamp DESC")) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("username");
                    String message = rs.getString("message");
                    Timestamp timestamp = rs.getTimestamp("timestamp");
                    messages.add(new ChatMessage(username, message, timestamp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messages;
    }
}
