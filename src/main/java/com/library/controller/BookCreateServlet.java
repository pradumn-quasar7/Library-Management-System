package com.library.controller;

import com.library.exception.ConflictException;
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

@WebServlet("/librarian/books/create")
public class BookCreateServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("genres", bookService.getAllGenres());
        forward(request, response, "/WEB-INF/views/librarian/book-form.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = getCurrentUser(request);

        String title = request.getParameter("title");
        String isbn = request.getParameter("isbn");
        String publisher = request.getParameter("publisher");
        String yearParam = request.getParameter("publicationYear");
        String description = request.getParameter("description");
        String genreParam = request.getParameter("genreId");
        String authorsInput = request.getParameter("authors");
        String copiesParam = request.getParameter("copiesCount");

        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setPublisher(publisher);
        book.setDescription(description);

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

        int copiesCount = 1;
        if (copiesParam != null && !copiesParam.trim().isEmpty()) {
            try {
                copiesCount = Integer.parseInt(copiesParam.trim());
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
            Book created = bookService.createBook(book, authors, copiesCount, user != null ? user.getId() : null);
            setFlashSuccess(request, "Book '" + created.getTitle() + "' was successfully added to the catalog with " + copiesCount + " copies.");
            redirect(request, response, "/librarian/books");
        } catch (ValidationException | ConflictException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.setAttribute("book", book);
            request.setAttribute("authorsInput", authorsInput);
            request.setAttribute("copiesCount", copiesCount);
            request.setAttribute("genres", bookService.getAllGenres());
            forward(request, response, "/WEB-INF/views/librarian/book-form.jsp");
        }
    }
}
