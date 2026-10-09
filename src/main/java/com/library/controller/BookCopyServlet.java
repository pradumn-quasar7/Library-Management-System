package com.library.controller;

import com.library.model.CopyStatus;
import com.library.model.User;
import com.library.service.BookService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/librarian/copies")
public class BookCopyServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User user = getCurrentUser(request);
        String action = request.getParameter("action");
        String bookIdParam = request.getParameter("bookId");

        try {
            if ("add".equalsIgnoreCase(action)) {
                Long bookId = Long.parseLong(bookIdParam);
                String accessionNumber = request.getParameter("accessionNumber");
                String conditionNotes = request.getParameter("conditionNotes");
                bookService.addCopy(bookId, accessionNumber, conditionNotes, user != null ? user.getId() : null);
                setFlashSuccess(request, "New copy added successfully.");
                redirect(request, response, "/librarian/books/edit?id=" + bookId);
                return;
            } else if ("updateStatus".equalsIgnoreCase(action)) {
                Long copyId = Long.parseLong(request.getParameter("copyId"));
                String statusStr = request.getParameter("status");
                String conditionNotes = request.getParameter("conditionNotes");
                CopyStatus status = CopyStatus.fromString(statusStr);
                if (status != null) {
                    bookService.updateCopyStatus(copyId, status, conditionNotes, user != null ? user.getId() : null);
                    setFlashSuccess(request, "Copy status updated to " + status + ".");
                }
                if (bookIdParam != null && !bookIdParam.isEmpty()) {
                    redirect(request, response, "/librarian/books/edit?id=" + bookIdParam);
                    return;
                }
            }
        } catch (Exception e) {
            setFlashError(request, "Error managing copy: " + e.getMessage());
            if (bookIdParam != null && !bookIdParam.isEmpty()) {
                redirect(request, response, "/librarian/books/edit?id=" + bookIdParam);
                return;
            }
        }

        redirect(request, response, "/librarian/books");
    }
}
