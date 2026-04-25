<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Sign in</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main auth-shell">
  <div class="row justify-content-center">
    <div class="col-md-5 col-lg-4">
      <div class="card auth-card border-0">
        <div class="card-body p-4 p-md-5">
          <div class="text-center mb-4">
            <div class="d-inline-flex align-items-center justify-content-center rounded-circle bg-primary bg-opacity-10 text-primary mb-3" style="width:56px;height:56px">
              <i class="bi bi-person-badge fs-3"></i>
            </div>
            <h1 class="h4 fw-bold mb-0">Sign in</h1>
            <p class="text-muted small mt-2 mb-0">Welcome back to Smart Parking</p>
          </div>
          <form action="${pageContext.request.contextPath}/login" method="post" novalidate id="loginForm">
            <sec:csrfInput/>
            <div class="mb-3">
              <label class="form-label fw-semibold" for="email">Email</label>
              <input class="form-control form-control-modern" type="email" name="email" id="email" required maxlength="255" autocomplete="username"/>
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold" for="password">Password</label>
              <input class="form-control form-control-modern" type="password" name="password" id="password" required minlength="8" maxlength="72" autocomplete="current-password"/>
            </div>
            <button class="btn btn-primary w-100 py-2 rounded-pill" type="submit" id="loginBtn">
              <span class="btn-submit-label">Sign in</span>
              <span class="spinner-border spinner-border-sm d-none btn-submit-spin" role="status" aria-hidden="true"></span>
            </button>
          </form>
          <p class="mt-4 mb-0 small text-center text-muted">
            <a href="${pageContext.request.contextPath}/register" class="text-decoration-none">Create an account</a>
          </p>
        </div>
      </div>
    </div>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/validation.js"></script>
<script>
  wireFormValidation(document.getElementById("loginForm"), {
    email: { required: true, email: true },
    password: { required: true, minLength: 8 }
  });
  document.getElementById("loginForm").addEventListener("submit", function (e) {
    if (e.defaultPrevented) return;
    document.querySelector("#loginBtn .btn-submit-label").classList.add("d-none");
    document.querySelector("#loginBtn .btn-submit-spin").classList.remove("d-none");
  });
  document.addEventListener("DOMContentLoaded", function () {
    <c:if test="${param.registered ne null}">
    if (window.AppUi) AppUi.showToast("success", "Registration successful. Please sign in.");
    </c:if>
    <c:if test="${param.error ne null}">
    if (window.AppUi) AppUi.showToast("danger", "Invalid email or password.");
    </c:if>
  });
</script>
</body>
</html>
