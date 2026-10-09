package com.library.controller;

import com.library.model.Member;
import com.library.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("currentUser");
        }
        return null;
    }

    protected Member getCurrentMember(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Member) session.getAttribute("currentMember");
        }
        return null;
    }

    protected void setFlashSuccess(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("successMessage", message);
    }

    protected void setFlashError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute("errorMessage", message);
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String jspPath)
            throws ServletException, IOException {
        request.getRequestDispatcher(jspPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }
}
