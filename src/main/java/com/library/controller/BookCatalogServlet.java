package com.library.controller;

import com.library.model.Book;
import com.library.model.Genre;
import com.library.service.BookService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/books/search", "/member/catalog"})
public class BookCatalogServlet extends BaseServlet {
    private final BookService bookService = new BookService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = request.getParameter("q");
        String genreParam = request.getParameter("genre");
        String availableParam = request.getParameter("available");
        String sort = request.getParameter("sort");

        Long genreId = null;
        if (genreParam != null && !genreParam.trim().isEmpty()) {
            try {
                genreId = Long.parseLong(genreParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        Boolean availableOnly = "true".equalsIgnoreCase(availableParam);

        List<Book> books = bookService.searchBooks(query, genreId, availableOnly ? true : null, sort);
        List<Genre> genres = bookService.getAllGenres();

        request.setAttribute("books", books);
        request.setAttribute("genres", genres);
        request.setAttribute("query", query);
        request.setAttribute("selectedGenreId", genreId);
        request.setAttribute("availableOnly", availableOnly);
        request.setAttribute("sortBy", sort);

        forward(request, response, "/WEB-INF/views/books/catalog.jsp");
    }
}
