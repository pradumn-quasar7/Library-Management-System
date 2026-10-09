package com.library.controller;

import com.library.model.User;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/books/delete")
public class BookDeleteServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        String idParam = request.getParameter("id");
        String status = request.getParameter("status"); // INACTIVE or ACTIVE

        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Long id = Long.parseLong(idParam.trim());
                String newStatus = "INACTIVE".equalsIgnoreCase(status) ? "INACTIVE" : "ACTIVE";
                bookService.setBookStatus(id, newStatus, user != null ? user.getId() : null);
                setFlashSuccess(request, "Book status updated to " + newStatus + ".");
            } catch (Exception e) {
                setFlashError(request, "Failed to update book status: " + e.getMessage());
            }
        }
        redirect(request, response, "/librarian/books");
    }
}
