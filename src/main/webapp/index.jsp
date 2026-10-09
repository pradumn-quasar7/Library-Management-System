<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Athena Library — Online Library Management System" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<style>
.hero-section {
    background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4338ca 100%);
    border-radius: 20px;
    padding: 3.5rem 2.5rem;
    color: #ffffff;
    margin-bottom: 2.5rem;
    box-shadow: 0 20px 25px -5px rgba(49, 46, 129, 0.3);
    position: relative;
    overflow: hidden;
}
.hero-section::after {
    content: '📖';
    position: absolute;
    right: -20px;
    bottom: -30px;
    font-size: 14rem;
    opacity: 0.08;
    pointer-events: none;
}
.hero-badge {
    display: inline-block;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.25);
    padding: 0.35rem 0.85rem;
    border-radius: 9999px;
    font-size: 0.82rem;
    font-weight: 700;
    margin-bottom: 1.25rem;
    letter-spacing: 0.05em;
    text-transform: uppercase;
}
.hero-title {
    font-size: 2.75rem;
    font-weight: 800;
    line-height: 1.15;
    margin-bottom: 1rem;
    color: #ffffff;
}
.hero-desc {
    font-size: 1.1rem;
    color: #c7d2fe;
    max-width: 620px;
    margin-bottom: 2rem;
    line-height: 1.6;
}
.hero-search-box {
    background: #ffffff;
    border-radius: 14px;
    padding: 0.5rem;
    display: flex;
    max-width: 600px;
    box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.2);
}
.hero-search-input {
    flex: 1;
    border: none;
    padding: 0.75rem 1.25rem;
    font-size: 1rem;
    outline: none;
    font-family: inherit;
    color: #0f172a;
}
.demo-accounts-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.25rem;
    margin-bottom: 2.5rem;
}
.demo-card {
    background: #ffffff;
    border: 1px solid var(--border-color);
    border-radius: var(--radius-lg);
    padding: 1.5rem;
    box-shadow: var(--shadow-sm);
}
.demo-card code {
    background: #f1f5f9;
    padding: 0.2rem 0.4rem;
    border-radius: 4px;
    font-family: 'JetBrains Mono', monospace;
    font-size: 0.85rem;
    color: var(--primary);
}
</style>

<section class="hero-section">
    <div class="hero-badge">Enterprise Academic Demonstration</div>
    <h1 class="hero-title">Discover, Borrow &amp; Excel with Athena Library</h1>
    <p class="hero-desc">
        A complete Java 21, Jakarta Servlets, JSP &amp; JDBC powered platform with transactional borrowing,
        reservation queues, background notifications, and explainable recommendations.
    </p>

    <form action="${pageContext.request.contextPath}/books/search" method="GET" class="hero-search-box">
        <input type="text" name="q" class="hero-search-input" placeholder="Search by title, author, or ISBN...">
        <button type="submit" class="btn btn-primary" style="border-radius: 10px; padding: 0.75rem 1.5rem;">Search Catalog</button>
    </form>
</section>

<!-- Quick Credentials for Academic Evaluator / Viva -->
<div class="card-header" style="margin-bottom: 1rem;">
    <h2>Demo Login Credentials (Click to copy or use)</h2>
    <span class="badge badge-info">Evaluation Ready</span>
</div>

<div class="demo-accounts-grid">
    <div class="demo-card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
            <h3 style="font-size: 1.1rem;">Librarian Account</h3>
            <span class="badge badge-danger">Librarian Role</span>
        </div>
        <p style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.75rem;">
            Full administrative control: Catalog CRUD, copy management, member approval, return processing, reports &amp; CSV export.
        </p>
        <div style="font-size: 0.85rem; margin-bottom: 1rem;">
            <div>Email: <code>admin@library.local</code></div>
            <div style="margin-top: 0.25rem;">Password: <code>Admin@123</code></div>
        </div>
        <a href="${pageContext.request.contextPath}/login?email=admin@library.local" class="btn btn-secondary btn-sm" style="width: 100%;">Login as Librarian</a>
    </div>

    <div class="demo-card">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
            <h3 style="font-size: 1.1rem;">Member Account</h3>
            <span class="badge badge-info">Member Role</span>
        </div>
        <p style="font-size: 0.88rem; color: var(--text-secondary); margin-bottom: 0.75rem;">
            Member experience: Search books, transactional borrow &amp; return, reservations, reading room booking &amp; recommendations.
        </p>
        <div style="font-size: 0.85rem; margin-bottom: 1rem;">
            <div>Email: <code>john.smith@library.local</code></div>
            <div style="margin-top: 0.25rem;">Password: <code>Member@123</code></div>
        </div>
        <a href="${pageContext.request.contextPath}/login?email=john.smith@library.local" class="btn btn-secondary btn-sm" style="width: 100%;">Login as Member</a>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
