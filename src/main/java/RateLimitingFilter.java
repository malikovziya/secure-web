import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@WebFilter("/*")  // Apply to all incoming requests
public class RateLimitingFilter implements Filter {

    private static final long WINDOW_SIZE = TimeUnit.MINUTES.toMillis(1);  // 1-minute window
    private static final int MAX_REQUESTS = 100;  // Max requests per minute
    private final Map<String, RequestInfo> requestInfoMap = new HashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic (if any)
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String clientIP = httpRequest.getRemoteAddr();

        // Track the number of requests per IP in a time window
        RequestInfo requestInfo = requestInfoMap.getOrDefault(clientIP, new RequestInfo());
        long currentTime = System.currentTimeMillis();

        // Cleanup old request data (older than the time window)
        if (currentTime - requestInfo.timestamp > WINDOW_SIZE) {
            requestInfo.reset();
        }

        if (requestInfo.count >= MAX_REQUESTS) {
            // If request count exceeds limit, respond with 429 Too Many Requests
            httpResponse.setStatus(429); // Manually use the HTTP status code for "Too Many Requests"
            httpResponse.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        // Increment the request count and allow the request to continue
        requestInfo.count++;
        requestInfo.timestamp = currentTime;
        requestInfoMap.put(clientIP, requestInfo);

        // Proceed with the request
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup (if needed)
    }

    private static class RequestInfo {
        long timestamp;  // Last request timestamp
        int count;  // Count of requests within the window

        void reset() {
            timestamp = System.currentTimeMillis();
            count = 0;
        }
    }
}
