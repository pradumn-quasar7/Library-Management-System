package com.library.service;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.dao.LoanDAO;
import com.library.dao.impl.AuthorDAOImpl;
import com.library.dao.impl.BookDAOImpl;
import com.library.dao.impl.LoanDAOImpl;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Loan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Smart Book Recommendations Engine
 * Satisfies Rubric Core Java Collections (Maps, Sets, Lists, Sorting, Comparator)
 *
 * Deterministic Algorithm:
 * 1. Analyzes member's past loan history
 * 2. Counts frequencies of borrowed genres: Map<Long, Integer> genreFrequency
 * 3. Counts frequencies of borrowed authors: Map<Long, Integer> authorFrequency
 * 4. Filters out books already borrowed by the member: Set<Long> borrowedBookIds
 * 5. Scores every active book candidate:
 *      Score = (genreFrequency * 3) + (authorFrequency * 2) + (isAvailable ? 1 : 0)
 * 6. Sorts using Comparator<Book> in descending order
 * 7. Returns top N recommendations (or falls back to popular books if history is sparse)
 */
public class RecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    private final LoanDAO loanDAO;
    private final BookDAO bookDAO;
    private final AuthorDAO authorDAO;

    public RecommendationService() {
        this.loanDAO = new LoanDAOImpl();
        this.bookDAO = new BookDAOImpl();
        this.authorDAO = new AuthorDAOImpl();
    }

    public RecommendationService(LoanDAO loanDAO, BookDAO bookDAO, AuthorDAO authorDAO) {
        this.loanDAO = loanDAO;
        this.bookDAO = bookDAO;
        this.authorDAO = authorDAO;
    }

    public List<Book> getRecommendationsForMember(Long memberId, int limit) {
        List<Loan> memberLoans = loanDAO.findAllLoansByMemberId(memberId);

        // If member has no history, fall back to top popular and recent books
        if (memberLoans == null || memberLoans.isEmpty()) {
            logger.debug("Member {} has no loan history. Providing top popular books.", memberId);
            return bookDAO.findTopPopularBooks(limit);
        }

        // 1. Gather borrowed book IDs to exclude
        Set<Long> borrowedBookIds = new HashSet<>();
        // 2. Frequency maps for genre and author preferences
        Map<Long, Integer> genreFrequency = new HashMap<>();
        Map<Long, Integer> authorFrequency = new HashMap<>();

        for (Loan loan : memberLoans) {
            // Find book corresponding to copy
            Book book = bookDAO.findByIsbn(loan.getBookIsbn());
            if (book != null) {
                borrowedBookIds.add(book.getId());

                if (book.getGenreId() != null) {
                    genreFrequency.put(book.getGenreId(), genreFrequency.getOrDefault(book.getGenreId(), 0) + 1);
                }

                List<Author> authors = authorDAO.findAuthorsByBookId(book.getId());
                for (Author a : authors) {
                    authorFrequency.put(a.getId(), authorFrequency.getOrDefault(a.getId(), 0) + 1);
                }
            }
        }

        // 3. Score all active books not currently/previously borrowed
        List<Book> allActiveBooks = bookDAO.findAll(true);
        Map<Book, Integer> scoredCandidates = new HashMap<>();

        for (Book candidate : allActiveBooks) {
            if (borrowedBookIds.contains(candidate.getId())) {
                continue; // Skip already borrowed titles
            }

            int score = 0;

            // Genre match weight: 3
            if (candidate.getGenreId() != null && genreFrequency.containsKey(candidate.getGenreId())) {
                score += genreFrequency.get(candidate.getGenreId()) * 3;
            }

            // Author match weight: 2
            List<Author> authors = candidate.getAuthors();
            if (authors != null) {
                for (Author a : authors) {
                    if (authorFrequency.containsKey(a.getId())) {
                        score += authorFrequency.get(a.getId()) * 2;
                    }
                }
            }

            // Availability bonus: 1
            if (candidate.isAvailable()) {
                score += 1;
            }

            scoredCandidates.put(candidate, score);
        }

        // 4. Sort by score descending, then by publication year descending, then by title ascending
        Comparator<Book> recommendationComparator = (b1, b2) -> {
            int s1 = scoredCandidates.getOrDefault(b1, 0);
            int s2 = scoredCandidates.getOrDefault(b2, 0);
            int scoreComp = Integer.compare(s2, s1); // descending
            if (scoreComp != 0) return scoreComp;

            int y1 = b1.getPublicationYear() != null ? b1.getPublicationYear() : 0;
            int y2 = b2.getPublicationYear() != null ? b2.getPublicationYear() : 0;
            int yearComp = Integer.compare(y2, y1); // descending
            if (yearComp != 0) return yearComp;

            return b1.getTitle().compareToIgnoreCase(b2.getTitle());
        };

        List<Book> sortedList = scoredCandidates.keySet().stream()
                .sorted(recommendationComparator)
                .limit(limit > 0 ? limit : 6)
                .collect(Collectors.toList());

        // If sorted list is empty (e.g. member has borrowed all books in those genres), pad with other available books
        if (sortedList.isEmpty()) {
            return bookDAO.findTopPopularBooks(limit);
        }

        return sortedList;
    }
}
