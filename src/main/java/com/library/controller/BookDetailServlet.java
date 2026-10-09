package com.library.controller;

import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.service.BookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/books/view")
public class BookDetailServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            redirect(request, response, "/books/search");
            return;
        }

        try {
            Long id = Long.parseLong(idParam.trim());
            Book book = bookService.getBookById(id);
            request.setAttribute("book", book);
            request.setAttribute("copies", bookService.getBookCopies(id));
            forward(request, response, "/WEB-INF/views/books/detail.jsp");
        } catch (ResourceNotFoundException | NumberFormatException e) {
            setFlashError(request, "Book not found.");
            redirect(request, response, "/books/search");
        }
    }
}
