package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.RecommendationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/member/recommendations")
public class MemberRecommendationsServlet extends BaseServlet {
    private final RecommendationService recommendationService = new RecommendationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Member member = getCurrentMember(request);
        if (member == null) {
            redirect(request, response, "/login");
            return;
        }

        List<Book> recommendations = recommendationService.getRecommendationsForMember(member.getId(), 12);
        request.setAttribute("recommendations", recommendations);
        forward(request, response, "/WEB-INF/views/member/recommendations.jsp");
    }
}
