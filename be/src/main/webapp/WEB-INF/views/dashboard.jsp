<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Dashboard</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <div class="mb-4">
    <h1 class="h2 fw-bold mb-1">Dashboard</h1>
    <p class="text-muted mb-0">Overview of your account and system capacity.</p>
  </div>
  <div class="row g-4">
    <div class="col-md-6">
      <div class="stat-card">
        <div class="stat-card-icon stat-card-icon--primary">
          <i class="bi bi-calendar3"></i>
        </div>
        <p class="text-uppercase text-muted small fw-semibold mb-1 letter-spacing-wide">Your bookings</p>
        <p class="stat-value mb-3"><c:out value="${totalBookings}"/></p>
        <a class="btn btn-outline-primary btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/bookings">
          <i class="bi bi-arrow-right-circle me-1"></i>View all
        </a>
      </div>
    </div>
    <div class="col-md-6">
      <div class="stat-card">
        <div class="stat-card-icon stat-card-icon--success">
          <i class="bi bi-grid-3x3-gap"></i>
        </div>
        <p class="text-uppercase text-muted small fw-semibold mb-1 letter-spacing-wide">Available slots (system)</p>
        <p class="stat-value mb-3"><c:out value="${availableSlots}"/></p>
        <a class="btn btn-outline-success btn-sm rounded-pill px-3" href="${pageContext.request.contextPath}/parking-areas">
          <i class="bi bi-plus-lg me-1"></i>Book a slot
        </a>
      </div>
    </div>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
</body>
</html>
