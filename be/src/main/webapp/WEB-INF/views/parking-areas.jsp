<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Parking areas</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <div class="d-flex flex-wrap align-items-end justify-content-between gap-3 mb-4">
    <div>
      <h1 class="h2 fw-bold mb-1">Parking areas</h1>
      <p class="text-muted mb-0">Choose a location and reserve a slot for your window.</p>
    </div>
  </div>
  <c:if test="${empty areas}">
    <div class="alert alert-secondary border-0 rounded-3 shadow-sm">No areas available.</div>
  </c:if>
  <div class="row g-4">
    <c:forEach var="a" items="${areas}">
      <div class="col-md-6 col-lg-4">
        <div class="card app-card h-100 border-0">
          <div class="card-body d-flex flex-column">
            <div class="d-flex align-items-start gap-3 mb-3">
              <div class="app-card-icon mb-0 flex-shrink-0">
                <i class="bi bi-building"></i>
              </div>
              <div>
                <h2 class="app-card-title mb-1"><c:out value="${a.name}"/></h2>
                <p class="text-muted small mb-0">
                  <i class="bi bi-geo-alt-fill text-primary me-1"></i>
                  <c:out value="${a.addressLine1}"/><br/>
                  <span class="ms-3 ps-1"><c:out value="${a.city}"/></span>
                </p>
              </div>
            </div>
            <div class="mt-auto pt-2">
              <sec:authorize access="isAuthenticated()">
                <a class="btn btn-primary w-100" href="${pageContext.request.contextPath}/bookings/new?areaId=${a.id}">
                  <i class="bi bi-calendar-plus me-1"></i>Book a slot
                </a>
              </sec:authorize>
              <sec:authorize access="!isAuthenticated()">
                <a class="btn btn-outline-primary w-100" href="${pageContext.request.contextPath}/login">
                  <i class="bi bi-box-arrow-in-right me-1"></i>Sign in to book
                </a>
              </sec:authorize>
            </div>
          </div>
        </div>
      </div>
    </c:forEach>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
</body>
</html>
