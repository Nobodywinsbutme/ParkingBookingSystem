<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Error</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main py-5">
  <div class="row justify-content-center">
    <div class="col-lg-6">
      <div class="card border-0 app-card shadow-sm">
        <div class="card-body p-4 p-md-5 text-center">
          <div class="text-danger mb-3"><i class="bi bi-exclamation-octagon display-4"></i></div>
          <h1 class="h4 fw-bold">Something went wrong</h1>
          <p class="text-muted mb-4"><c:out value="${message}"/></p>
          <a class="btn btn-primary rounded-pill px-4" href="${pageContext.request.contextPath}/">
            <i class="bi bi-house-door me-1"></i>Back home
          </a>
        </div>
      </div>
    </div>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
</body>
</html>
