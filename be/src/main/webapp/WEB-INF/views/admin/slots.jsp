<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="../common/head-bootstrap.jspf" %>
  <title>Admin — Parking slots</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="../common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <%@ include file="admin-nav.jspf" %>

  <div class="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-3">
    <div>
      <h1 class="h2 fw-bold mb-1">Parking slots</h1>
      <p class="text-muted mb-0">Mã ô, bãi, trạng thái (AVAILABLE / MAINTENANCE khi tạo mới).</p>
    </div>
    <a class="btn btn-primary rounded-pill" href="${pageContext.request.contextPath}/admin/slots/create">
      <i class="bi bi-plus-lg me-1"></i>Create
    </a>
  </div>

  <c:if test="${not empty msgSuccess}"><div class="alert alert-success border-0 rounded-3 shadow-sm">${msgSuccess}</div></c:if>
  <c:if test="${not empty msgError}"><div class="alert alert-danger border-0 rounded-3 shadow-sm"><c:out value="${msgError}"/></div></c:if>

  <div class="card app-card border-0 shadow-sm">
    <div class="table-responsive">
      <table class="table table-hover align-middle small mb-0">
        <thead class="table-light">
        <tr>
          <th>Area</th>
          <th>Code</th>
          <th>Floor</th>
          <th>Status</th>
          <th>Active</th>
          <th class="text-end" style="width:10rem">Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="s" items="${slots}">
          <c:set var="aname" value="${areaNameById[s.parkingAreaId]}"/>
          <tr>
            <td><c:out value="${aname}"/></td>
            <td class="fw-medium"><c:out value="${s.code}"/></td>
            <td><c:out value="${s.floor}"/></td>
            <td><span class="badge text-bg-info text-uppercase"><c:out value="${s.status}"/></span></td>
            <td>
              <c:choose>
                <c:when test="${s.active}"><span class="badge text-bg-success">Yes</span></c:when>
                <c:otherwise><span class="badge text-bg-secondary">No</span></c:otherwise>
              </c:choose>
            </td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/admin/slots/${s.id}/edit">Edit</a>
              <c:if test="${s.active}">
                <form class="d-inline" action="${pageContext.request.contextPath}/admin/slots/${s.id}/delete" method="post" onsubmit="return confirm('Deactivate this slot?');">
                  <sec:csrfInput/>
                  <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                </form>
              </c:if>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty slots}">
          <tr><td colspan="6" class="text-center text-muted py-4">No slots yet.</td></tr>
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
