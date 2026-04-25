<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="../common/head-bootstrap.jspf" %>
  <title>Admin — Users</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="../common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <%@ include file="admin-nav.jspf" %>

  <h1 class="h2 fw-bold mb-1">Users</h1>
  <p class="text-muted mb-4">Read-only list. Passwords are never shown.</p>

  <div class="card app-card border-0 shadow-sm">
    <div class="table-responsive">
      <table class="table table-hover align-middle small mb-0">
        <thead class="table-light">
        <tr>
          <th>Email</th>
          <th>Role</th>
          <th>Active</th>
          <th>Created</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="u" items="${users}">
          <tr>
            <td class="font-monospace"><c:out value="${u.email}"/></td>
            <td><span class="badge text-bg-secondary text-uppercase"><c:out value="${u.role}"/></span></td>
            <td>
              <c:choose>
                <c:when test="${u.active}"><span class="badge text-bg-success">Yes</span></c:when>
                <c:otherwise><span class="badge text-bg-warning text-dark">No</span></c:otherwise>
              </c:choose>
            </td>
            <td class="text-muted"><c:out value="${u.createdAt}"/></td>
          </tr>
        </c:forEach>
        <c:if test="${empty users}">
          <tr><td colspan="4" class="text-center text-muted py-4">No users.</td></tr>
        </c:if>
        </tbody>
      </table>
    </div>
  </div>
  <a class="btn btn-outline-secondary rounded-pill mt-3" href="${pageContext.request.contextPath}/admin/home">← Admin home</a>
</main>
<%@ include file="../common/footer.jspf" %>
</body>
</html>
