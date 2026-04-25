<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="../common/head-bootstrap.jspf" %>
  <title>Admin — Parking areas</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="../common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <%@ include file="admin-nav.jspf" %>

  <div class="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-3">
    <div>
      <h1 class="h2 fw-bold mb-1">Parking areas</h1>
      <p class="text-muted mb-0">Create, edit, or deactivate bãi đỗ.</p>
    </div>
    <a class="btn btn-primary rounded-pill" href="${pageContext.request.contextPath}/admin/areas/create">
      <i class="bi bi-plus-lg me-1"></i>Create
    </a>
  </div>

  <c:if test="${not empty msgSuccess}"><div class="alert alert-success border-0 rounded-3 shadow-sm">${msgSuccess}</div></c:if>
  <c:if test="${not empty msgError}"><div class="alert alert-danger border-0 rounded-3 shadow-sm"><c:out value="${msgError}"/></div></c:if>

  <div class="card app-card border-0 shadow-sm">
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
        <tr>
          <th>Name</th>
          <th>City</th>
          <th>Address</th>
          <th>Active</th>
          <th class="text-end" style="width:10rem">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="a" items="${areas}">
          <tr>
            <td class="fw-medium"><c:out value="${a.name}"/></td>
            <td><c:out value="${a.city}"/></td>
            <td class="text-muted small"><c:out value="${a.addressLine1}"/></td>
            <td>
              <c:choose>
                <c:when test="${a.active}"><span class="badge text-bg-success">Yes</span></c:when>
                <c:otherwise><span class="badge text-bg-secondary">No</span></c:otherwise>
              </c:choose>
            </td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/areas/${a.id}/edit">Edit</a>
              <c:if test="${a.active}">
                <form class="d-inline" action="${pageContext.request.contextPath}/admin/areas/${a.id}/delete" method="post" onsubmit="return confirm('Deactivate this area? It will be hidden from the public list.');">
                  <sec:csrfInput/>
                  <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty areas}">
          <tr><td colspan="5" class="text-center text-muted py-4">No areas yet.</td></tr>
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
