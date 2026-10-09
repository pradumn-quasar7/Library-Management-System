package com.library.filter;

import com.library.model.Role;
import com.library.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class LibrarianAuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session != null) {
            User user = (User) session.getAttribute("currentUser");
            if (user != null && Role.LIBRARIAN.equals(user.getRole())) {
                chain.doFilter(request, response);
                return;
            }
        }

        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Librarian privileges required.");
    }

    @Override
    public void destroy() {
    }
}
