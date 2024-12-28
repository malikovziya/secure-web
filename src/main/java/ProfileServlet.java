import model.UserProfile;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    private static final String DB_URL = System.getenv("CASE_STUDY_DB_URL");
    private static final String DB_USERNAME = System.getenv("CASE_STUDY_DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("CASE_STUDY_DB_PASSWORD");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("index.html?error=unauthorized"); // Redirect to login page if not logged in
            return;
        }

        String username = (String) session.getAttribute("user");
        UserProfile userProfile = getUserProfile(username);

        if (userProfile == null) {
            // Provide fallback user profile
            userProfile = new UserProfile(username, null); // Null photo defaults to default in JSP
        }

        request.setAttribute("userProfile", userProfile);
        request.getRequestDispatcher("profile.jsp").forward(request, response);
    }

    private UserProfile getUserProfile(String username) {
        UserProfile userProfile = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            String sql = "SELECT username, profile_photo FROM users WHERE username = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            rs = stmt.executeQuery();

            if (rs.next()) {
                String profilePhoto = rs.getString("profile_photo");
                profilePhoto = (profilePhoto == null || profilePhoto.isEmpty()) ? "static/images/img.png" : ("static\\" + profilePhoto);
//                System.out.println("profile photo -> " + profilePhoto);
                userProfile = new UserProfile(username, profilePhoto);
            }
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

        return userProfile;
    }
}