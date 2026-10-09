package com.library.controller;

import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Book;
import com.library.model.User;
import com.library.service.BookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebServlet("/librarian/books/edit")
public class BookEditServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            redirect(request, response, "/librarian/books");
            return;
        }

        try {
            Long id = Long.parseLong(idParam.trim());
            Book book = bookService.getBookById(id);
            request.setAttribute("book", book);
            request.setAttribute("authorsInput", book.getAuthorsFormatted());
            request.setAttribute("genres", bookService.getAllGenres());
            request.setAttribute("copies", bookService.getBookCopies(id));
            forward(request, response, "/WEB-INF/views/librarian/book-form.jsp");
        } catch (ResourceNotFoundException | NumberFormatException e) {
            setFlashError(request, "Book not found.");
            redirect(request, response, "/librarian/books");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            redirect(request, response, "/librarian/books");
            return;
        }

        Long id = Long.parseLong(idParam.trim());
        String title = request.getParameter("title");
        String isbn = request.getParameter("isbn");
        String publisher = request.getParameter("publisher");
        String yearParam = request.getParameter("publicationYear");
        String description = request.getParameter("description");
        String genreParam = request.getParameter("genreId");
        String status = request.getParameter("status");
        String authorsInput = request.getParameter("authors");

        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setDescription(description);
        book.setStatus(status != null ? status : "ACTIVE");

        if (yearParam != null && !yearParam.trim().isEmpty()) {
            try {
                book.setPublicationYear(Integer.parseInt(yearParam.trim()));
            } catch (NumberFormatException ignored) {}
        }

        if (genreParam != null && !genreParam.trim().isEmpty()) {
            try {
                book.setGenreId(Long.parseLong(genreParam.trim()));
            } catch (NumberFormatException ignored) {}
        }

        List<String> authors = null;
        if (authorsInput != null && !authorsInput.trim().isEmpty()) {
            authors = Arrays.stream(authorsInput.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }

        try {
            bookService.updateBook(book, authors, user != null ? user.getId() : null);
            setFlashSuccess(request, "Book '" + book.getTitle() + "' updated successfully.");
            redirect(request, response, "/librarian/books");
        } catch (ValidationException | ConflictException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("book", book);
            request.setAttribute("authorsInput", authorsInput);
            request.setAttribute("genres", bookService.getAllGenres());
            request.setAttribute("copies", bookService.getBookCopies(id));
            forward(request, response, "/WEB-INF/views/librarian/book-form.jsp");
        }
    }
}
