<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="isEdit" value="${not empty book.id}"/>
<c:set var="pageTitle" value="${isEdit ? 'Edit Book — ' : 'Add New Book — '}.concat('Athena Library')" scope="request"/>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<div class="page-header">
    <div>
        <h1>${isEdit ? 'Edit Book Details' : 'Add New Book to Catalog'}</h1>
        <p class="subtitle">${isEdit ? 'Update metadata and manage physical book copies' : 'Register a new book title in the library system'}</p>
    </div>
    <div class="page-header-actions">
        <a href="${pageContext.request.contextPath}/librarian/books" class="btn btn-secondary">← Back to Catalog</a>
    </div>
</div>

<div class="dashboard-grid">
    <!-- Book Metadata Form -->
    <div class="card">
        <div class="card-header">
            <div class="card-title">Book Metadata</div>
        </div>

        <form action="${pageContext.request.contextPath}/librarian/books/${isEdit ? 'edit' : 'create'}" method="POST">
            <c:if test="${isEdit}">
                <input type="hidden" name="id" value="${book.id}"/>
            </c:if>

            <div class="form-group">
                <label class="form-label" for="title">Book Title *</label>
                <input type="text" id="title" name="title" class="form-control" value="${book.title}" required placeholder="e.g. Effective Java">
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="isbn">ISBN (10 or 13 digits) *</label>
                    <input type="text" id="isbn" name="isbn" class="form-control" value="${book.isbn}" required placeholder="978-0134685991">
                </div>
                <div class="form-group">
                    <label class="form-label" for="genreId">Genre *</label>
                    <select id="genreId" name="genreId" class="form-select" required>
                        <option value="">Select Genre</option>
                        <c:forEach var="g" items="${genres}">
                            <option value="${g.id}" ${book.genreId eq g.id ? 'selected' : ''}>${g.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label class="form-label" for="authors">Authors (comma separated) *</label>
                <input type="text" id="authors" name="authors" class="form-control" value="${authorsInput}" required placeholder="Joshua Bloch, Neal Gafter">
                <div class="form-hint">Separate multiple authors with commas. They will be automatically indexed.</div>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="form-label" for="publisher">Publisher</label>
                    <input type="text" id="publisher" name="publisher" class="form-control" value="${book.publisher}" placeholder="Addison-Wesley">
                </div>
                <div class="form-group">
                    <label class="form-label" for="publicationYear">Publication Year</label>
                    <input type="number" id="publicationYear" name="publicationYear" class="form-control" value="${book.publicationYear}" placeholder="2022">
                </div>
            </div>

            <c:if test="${!isEdit}">
                <div class="form-group">
                    <label class="form-label" for="copiesCount">Initial Physical Copies *</label>
                    <input type="number" id="copiesCount" name="copiesCount" class="form-control" value="${copiesCount != null ? copiesCount : 2}" min="1" max="100" required>
                    <div class="form-hint">Physical inventory copies will be automatically created with unique accession barcodes.</div>
                </div>
            </c:if>

            <c:if test="${isEdit}">
                <div class="form-group">
                    <label class="form-label" for="status">Catalog Status</label>
                    <select id="status" name="status" class="form-select">
                        <option value="ACTIVE" ${book.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                        <option value="INACTIVE" ${book.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                    </select>
                </div>
            </c:if>

            <div class="form-group">
                <label class="form-label" for="description">Description / Abstract</label>
                <textarea id="description" name="description" class="form-control" placeholder="Detailed book synopsis...">${book.description}</textarea>
            </div>

            <button type="submit" class="btn btn-primary" style="width: 100%;">${isEdit ? 'Save Changes' : 'Create Book'}</button>
        </form>
    </div>

    <!-- Physical Copies Management (on Edit) -->
    <div>
        <c:if test="${isEdit}">
            <div class="card" style="margin-bottom: 1.5rem;">
                <div class="card-header">
                    <div class="card-title">Add Physical Copy</div>
                </div>
                <form action="${pageContext.request.contextPath}/librarian/copies" method="POST">
                    <input type="hidden" name="action" value="add"/>
                    <input type="hidden" name="bookId" value="${book.id}"/>

                    <div class="form-group">
                        <label class="form-label">Accession / Barcode</label>
                        <input type="text" name="accessionNumber" class="form-control" placeholder="Optional (auto-generated if empty)">
                    </div>

                    <div class="form-group">
                        <label class="form-label">Condition Notes</label>
                        <input type="text" name="conditionNotes" class="form-control" value="Good condition">
                    </div>

                    <button type="submit" class="btn btn-secondary btn-sm" style="width: 100%;">➕ Add Copy</button>
                </form>
            </div>

            <div class="card">
                <div class="card-header">
                    <div class="card-title">Inventory Copies (${copies.size()})</div>
                </div>
                <div class="table-responsive">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>Accession</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="c" items="${copies}">
                                <tr>
                                    <td>
                                        <strong>${c.accessionNumber}</strong><br/>
                                        <small style="color: var(--text-muted);">${c.conditionNotes}</small>
                                    </td>
                                    <td>
                                        <span class="badge ${c.status eq 'AVAILABLE' ? 'badge-success' : (c.status eq 'BORROWED' ? 'badge-info' : 'badge-warning')}">
                                            ${c.status}
                                        </span>
                                    </td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/librarian/copies" method="POST" style="display:flex; gap:4px;">
                                            <input type="hidden" name="action" value="updateStatus"/>
                                            <input type="hidden" name="copyId" value="${c.id}"/>
                                            <input type="hidden" name="bookId" value="${book.id}"/>
                                            <select name="status" class="form-select" style="padding: 2px 4px; font-size: 0.75rem;">
                                                <option value="AVAILABLE" ${c.status eq 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                                                <option value="MAINTENANCE" ${c.status eq 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                                <option value="LOST" ${c.status eq 'LOST' ? 'selected' : ''}>LOST</option>
                                                <option value="DAMAGED" ${c.status eq 'DAMAGED' ? 'selected' : ''}>DAMAGED</option>
                                            </select>
                                            <button type="submit" class="btn btn-secondary btn-sm" style="padding: 2px 6px;">Set</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </c:if>
    </div>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
