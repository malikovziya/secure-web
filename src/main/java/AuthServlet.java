import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.*;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    // Database connection details
    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");
    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());

    // Temporary store for OTPs (in-memory)
    private static final Map<String, String> otpStore = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String logout = request.getParameter("logout");

        if (logout != null && logout.equals("true")) {
            handleLogout(request, response);
            return;
        }

        // Check for encrypted and signed cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String rememberedUsername = null;
            String rememberedRole = null;
            String rememberedSignature = null;

            for (Cookie cookie : cookies) {
                if ("rememberMe".equals(cookie.getName())) {
                    rememberedUsername = cookie.getValue();
                }
                if ("role".equals(cookie.getName())) {
                    rememberedRole = cookie.getValue();
                }
                if ("rememberMeSignature".equals(cookie.getName())) {
                    rememberedSignature = cookie.getValue();
                }
            }

            if (rememberedUsername != null && rememberedRole != null && rememberedSignature != null) {
                // Verify the signature of the cookie data
                String cookieData = rememberedUsername + rememberedRole;
                try {
                    if (SecurityUtil.verifySignature(cookieData, rememberedSignature)) {
                        // Decrypt the cookie data
                        rememberedUsername = SecurityUtil.decrypt(rememberedUsername);
                        rememberedRole = SecurityUtil.decrypt(rememberedRole);

                        // Create a session for the remembered user
                        HttpSession session = request.getSession(true); // Ensure new session is created
                        session.setAttribute("user", rememberedUsername);
                        session.setAttribute("role", rememberedRole);
                        session.setMaxInactiveInterval(300); // 5 minutes

                        // Redirect to chat page after successful login
                        response.sendRedirect("chat");
                        SecureLogger.log("info", "Login Success", rememberedUsername, "User logged in using cookies.");
                        return;
                    }
                } catch (Exception e) {
                    logger.warning("Error verifying cookie signature: " + e.getMessage());
                }
            }
        }

        // If no valid cookie found, show login page
        response.sendRedirect("index.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            if ("login".equals(action)) {
                handleLogin(request, response);
            } else if ("verifyOtp".equals(action)) {
                handleOtpVerification(request, response);
            } else if ("signup".equals(action)) {
                handleSignup(request, response);
            } else if ("logout".equals(action)) {
                handleLogout(request, response);
            } else {
                response.sendRedirect("index.html");
            }
        } catch (Exception e) {
            logger.severe("Error during request processing: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password").trim();
        boolean rememberMe = "on".equals(request.getParameter("rememberMe"));

        // First validate credentials and get email
        String email = validateUserCredentials(username, password);

        if (email != null) {
            // Valid credentials, proceed with OTP
            try {
                String otp = generateOTP();
                otpStore.put(username, otp);
                sendEmail(email, "Your OTP", "Your OTP for login is: " + otp);

                HttpSession session = request.getSession(true);
                session.setAttribute("tempUser", username);
                session.setMaxInactiveInterval(300); // 5 minutes

                // Also store remember me preference in session for later use
                if (rememberMe) {
                    session.setAttribute("rememberMe", true);
                }

                response.sendRedirect("otpVerification.html");
                return; // Important: return here to prevent further execution
            } catch (RuntimeException e) {
                logger.severe("Failed to send OTP email: " + e.getMessage());
                response.sendRedirect("index.html?error=otpfailed");
                return;
            }
        } else {
            // Invalid credentials
            response.sendRedirect("index.html?error=true");
            return;
        }
    }

    private void handleSignup(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username").trim();
        String email = request.getParameter("email").trim();
        String password = request.getParameter("password").trim();
        String confirmPassword = request.getParameter("confirmPassword").trim();
        String role = request.getParameter("role").trim(); // Retrieve role from form

        // Validate role
        if (!role.equals("Admin") && !role.equals("Moderator") && !role.equals("User")) {
            response.sendRedirect("signup.html?error=Invalid role.");
            return;
        }

        // Validate email format
        if (!isValidEmail(email)) {
            response.sendRedirect("signup.html?error=Invalid email format.");
            return;
        }

        // Check if username already exists
        if (isUsernameTaken(username) || isEmailTaken(email)) {
            response.sendRedirect("signup.html?error=Username or Email already exists. Please choose a different one.");
            return;
        }

        // Check if password and confirm password match
        if (!password.equals(confirmPassword)) {
            response.sendRedirect("signup.html?error=Passwords do not match.");
            return;
        }

        // Hash the password using bcrypt
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Store username and hashed password in the database
        if (saveUserToDatabase(username, hashedPassword, role, email)) {
            response.sendRedirect("index.html?signup=true"); // Redirect to login page after successful signup
        } else {
            response.sendRedirect("signup.html?error=Signup failed. Please try again.");
        }
    }

    private void handleOtpVerification(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("tempUser") == null) {
            response.sendRedirect("index.html?error=sessionExpired");
            return;
        }

        String username = (String) session.getAttribute("tempUser");
        String inputOtp = request.getParameter("otp").trim();

        // Validate OTP
        if (otpStore.containsKey(username) && otpStore.get(username).equals(inputOtp)) {
            // OTP is valid, complete login
            otpStore.remove(username);

            // Get user role
            String role = getUserRole(username);
            if (role == null) {
                response.sendRedirect("index.html?error=invalidRole");
                return;
            }

            // Set up the session
            session.setAttribute("user", username);
            session.setAttribute("role", role);
            session.setMaxInactiveInterval(300); // 5 minutes
            session.setAttribute("sessionStartTime", System.currentTimeMillis());

            // Handle "Remember Me" if it was selected
            Boolean rememberMe = (Boolean) session.getAttribute("rememberMe");
            if (rememberMe != null && rememberMe) {
                try {
                    setupRememberMeCookies(username, role, response);
                } catch (Exception e) {
                    logger.warning("Failed to set remember-me cookies: " + e.getMessage());
                }
            }

            // Clear temporary attributes
            session.removeAttribute("tempUser");
            session.removeAttribute("rememberMe");

            response.sendRedirect("chat");
        } else {
            response.sendRedirect("otpVerification.html?error=invalidOtp");
        }
    }

    // Add this helper method for setting up remember-me cookies
    private void setupRememberMeCookies(String username, String role, HttpServletResponse response) throws Exception {
        String encryptedUsername = SecurityUtil.encrypt(username);
        String encryptedRole = SecurityUtil.encrypt(role);
        String signature = SecurityUtil.sign(encryptedUsername + encryptedRole);

        Cookie rememberCookie = new Cookie("rememberMe", encryptedUsername);
        rememberCookie.setMaxAge(60 * 60 * 24 * 30); // 30 days
        rememberCookie.setHttpOnly(true);
        rememberCookie.setSecure(true);
        response.addCookie(rememberCookie);

        Cookie roleCookie = new Cookie("role", encryptedRole);
        roleCookie.setMaxAge(60 * 60 * 24 * 30);
        roleCookie.setHttpOnly(true);
        roleCookie.setSecure(true);
        response.addCookie(roleCookie);

        Cookie signatureCookie = new Cookie("rememberMeSignature", signature);
        signatureCookie.setMaxAge(60 * 60 * 24 * 30);
        signatureCookie.setHttpOnly(true);
        signatureCookie.setSecure(true);
        response.addCookie(signatureCookie);
    }

    private String generateOTP() {
        return String.valueOf(100000 + new Random().nextInt(900000)); // 6-digit OTP
    }

    private String validateUserCredentials(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String sql = "SELECT password, email FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    return rs.getString("email");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeResources(conn, stmt, rs);
        }

        return null;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    private boolean isEmailTaken(String email) {
        // Implement logic to check email existence in the database
        return false;
    }

    // Check if the username already exists in the database
    private boolean isUsernameTaken(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Prepare SQL query to check username existence
            String sql = "SELECT * FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            // Execute the query
            rs = stmt.executeQuery();

            return rs.next(); // Return true if the username already exists

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private boolean saveUserToDatabase(String username, String hashedPassword, String role, String email) {
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Prepare SQL query to insert the new user
            String sql = "INSERT INTO users (username, password, role, email) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.setString(4, email);

            // Execute the query to insert the user
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the user was inserted successfully

        } catch (Exception e) {
            e.printStackTrace();  // Log any exceptions
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();  // Log any exceptions while closing resources
            }
        }

        return false;
    }

    // Add this helper method to get user role
    private String getUserRole(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            String sql = "SELECT role FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (Exception e) {
            logger.severe("Error getting user role: " + e.getMessage());
        } finally {
            closeResources(conn, stmt, rs);
        }
        return null;
    }

    private String getUserRoleIfValid(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Connect to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // Query to fetch user details
            String sql = "SELECT password, role FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            // Execute the query
            rs = stmt.executeQuery();

            // If the username exists, verify the password
            if (rs.next()) {
                String storedHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    SecureLogger.log("info", "Login Success", username, "User logged in successfully.");
                    return rs.getString("role"); // Return role if password matches
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log any exceptions
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace(); // Log any exceptions while closing resources
            }
        }

        SecureLogger.log("warning", "Login Failure", username, "Login attempt failed.");
        return null; // Return null if credentials are invalid
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            String username = (String) session.getAttribute("user");
            String ipAddress = getClientIP(request);

            // Retrieve session start time from the session (ensure it exists)
            Long sessionStartTime = (Long) session.getAttribute("sessionStartTime");
            if (sessionStartTime != null) {
                long currentTime = System.currentTimeMillis();
                long sessionDuration = currentTime - sessionStartTime; // Duration in milliseconds

                // Log the session duration
                logger.info("User '" + username + "' logged out from IP: '" + ipAddress + "'. Session lasted for " + sessionDuration + "ms.");
            } else {
                logger.info("User '" + username + "' logged out from IP: '" + ipAddress + "'");
            }
            SecureLogger.log("info", "Logout Success", username, "User logged out successfully.");
            session.invalidate();
        }

        // Delete cookies
        deleteCookie(response, "rememberMe");
        deleteCookie(response, "role");
        deleteCookie(response, "rememberMeSignature");

        // Redirect to the login page
        response.sendRedirect("index.html");
    }

    private void sendEmail(String toEmail, String subject, String body) {
        final String fromEmail = "ziyamelikov04@gmail.com"; // Replace with your email
        final String password = "fuhz xbyk jxzq dslf"; // Replace with your email password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        // Add these properties to fix SSL/TLS issues
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        // Create a session with authentication
        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    private void deleteCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);  // Expire the cookie
        cookie.setPath("/");  // Ensure the cookie is deleted from all paths
        response.addCookie(cookie);
    }

    public String getClientIP(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");

        // Check if IP is empty, null, or contains localhost/loopback addresses
        if (clientIP == null || clientIP.isEmpty() ||
                clientIP.contains("127.0.0.1") ||
                clientIP.contains("0:0:0:0:0:0:0:1") ||
                clientIP.contains("::1")) {

            clientIP = request.getRemoteAddr();
        }

        // Remove any potential localhost variations
        if (clientIP.equals("0:0:0:0:0:0:0:1") ||
                clientIP.equals("127.0.0.1") ||
                clientIP.equals("::1")) {
            return "localhost";
        }

        // Additional cleanup for multiple IPs in X-Forwarded-For header
        if (clientIP != null && clientIP.contains(",")) {
            clientIP = clientIP.split(",")[0].trim();
        }

        return clientIP;
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
