<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="../common/head-bootstrap.jspf" %>
  <title>Admin</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="../common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <h1 class="h2 fw-bold mb-1">Admin</h1>
  <p class="text-muted mb-4">Restricted area (role <code>ADMIN</code>).</p>

  <%@ include file="admin-nav.jspf" %>

  <div class="row g-3">
    <div class="col-md-4">
      <a class="text-decoration-none" href="${pageContext.request.contextPath}/admin/areas">
        <div class="card app-card h-100 border-0 shadow-sm p-4">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-card-icon stat-card-icon--primary mb-0"><i class="bi bi-building"></i></div>
            <div>
              <h2 class="h6 fw-bold mb-0">Parking areas</h2>
              <p class="text-muted small mb-0">CRUD + deactivate</p>
            </div>
          </div>
        </div>
      </a>
    </div>
    <div class="col-md-4">
      <a class="text-decoration-none" href="${pageContext.request.contextPath}/admin/slots">
        <div class="card app-card h-100 border-0 shadow-sm p-4">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-card-icon stat-card-icon--primary mb-0"><i class="bi bi-grid-3x2-gap"></i></div>
            <div>
              <h2 class="h6 fw-bold mb-0">Parking slots</h2>
              <p class="text-muted small mb-0">CRUD, status, area</p>
            </div>
          </div>
        </div>
      </a>
    </div>
    <div class="col-md-4">
      <a class="text-decoration-none" href="${pageContext.request.contextPath}/admin/users">
        <div class="card app-card h-100 border-0 shadow-sm p-4">
          <div class="d-flex align-items-center gap-3">
            <div class="stat-card-icon stat-card-icon--primary mb-0"><i class="bi bi-people"></i></div>
            <div>
              <h2 class="h6 fw-bold mb-0">Users</h2>
              <p class="text-muted small mb-0">View only</p>
            </div>
          </div>
        </div>
      </a>
    </div>
  </div>

  <a class="btn btn-outline-primary rounded-pill mt-4" href="${pageContext.request.contextPath}/">
    <i class="bi bi-arrow-left me-1"></i>Public site
  </a>
</main>
<%@ include file="../common/footer.jspf" %>
</body>
</html>
