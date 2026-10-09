package com.library.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        boolean loggedIn = (session != null && session.getAttribute("currentUser") != null);

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            String uri = httpRequest.getRequestURI();
            String query = httpRequest.getQueryString();
            String redirectUrl = uri + (query != null ? "?" + query : "");

            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute("redirectAfterLogin", redirectUrl);
            newSession.setAttribute("errorMessage", "Please sign in to access this page.");

            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {
    }
}
