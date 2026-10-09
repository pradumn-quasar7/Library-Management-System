package com.library.controller;

import com.library.exception.ConflictException;
import com.library.exception.ValidationException;
import com.library.model.Member;
import com.library.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response, "/WEB-INF/views/auth/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");

        try {
            Member member = authService.registerMember(email, password, fullName, phone, address);
            setFlashSuccess(request, "Registration successful! Your Membership ID is " + member.getMembershipId() + ". Please sign in.");
            redirect(request, response, "/login");
        } catch (ValidationException | ConflictException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("fullName", fullName);
            request.setAttribute("phone", phone);
            request.setAttribute("address", address);
            forward(request, response, "/WEB-INF/views/auth/register.jsp");
        }
    }
}
