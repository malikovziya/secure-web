import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/chat", "/dashboard.jsp", "/profile", "/uploads"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Initialization code if required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            request.setAttribute("errorMessage", "Unauthorized! Please log in first.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        } else {
            chain.doFilter(request, response); // Proceed if authenticated
        }
    }

    @Override
    public void destroy() {
        // Cleanup code if required
    }
}
