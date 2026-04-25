<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Register</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main auth-shell">
  <div class="row justify-content-center">
    <div class="col-md-6 col-lg-5">
      <div class="card auth-card border-0">
        <div class="card-body p-4 p-md-5">
          <div class="text-center mb-4">
            <div class="d-inline-flex align-items-center justify-content-center rounded-circle bg-success bg-opacity-10 text-success mb-3" style="width:56px;height:56px">
              <i class="bi bi-person-plus fs-3"></i>
            </div>
            <h1 class="h4 fw-bold mb-0">Create account</h1>
            <p class="text-muted small mt-2 mb-0">Start booking parking in minutes</p>
          </div>
          <c:if test="${not empty registerError}">
            <div class="alert alert-danger border-0 rounded-3 py-2 small mb-3">
              <c:out value="${registerError}"/>
            </div>
          </c:if>
          <form:form modelAttribute="registerForm" method="post" action="${pageContext.request.contextPath}/register" novalidate="true" id="regForm" cssClass="needs-validation">
            <div class="mb-3">
              <label class="form-label fw-semibold">Email</label>
              <form:input path="email" type="email" cssClass="form-control form-control-modern" maxlength="255" autocomplete="email"/>
              <form:errors path="email" cssClass="text-danger small d-block mt-1"/>
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Password <span class="text-muted fw-normal small">(min 8)</span></label>
              <form:password path="password" cssClass="form-control form-control-modern" maxlength="72" autocomplete="new-password"/>
              <form:errors path="password" cssClass="text-danger small d-block mt-1"/>
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold">Confirm password</label>
              <form:password path="confirmPassword" cssClass="form-control form-control-modern" maxlength="72" autocomplete="new-password"/>
              <form:errors path="confirmPassword" cssClass="text-danger small d-block mt-1"/>
            </div>
            <button class="btn btn-primary w-100 py-2 rounded-pill" type="submit" id="regSubmit">
              <span class="btn-submit-label">Create account</span>
              <span class="spinner-border spinner-border-sm d-none btn-submit-spin" role="status" aria-hidden="true"></span>
            </button>
          </form:form>
          <p class="mt-4 mb-0 small text-center text-muted">
            <a href="${pageContext.request.contextPath}/login" class="text-decoration-none">Already have an account?</a>
          </p>
        </div>
      </div>
    </div>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/validation.js"></script>
<script>
  wireFormValidation(document.getElementById("regForm"), {
    email: { required: true, email: true },
    password: { required: true, minLength: 8 },
    confirmPassword: { required: true, minLength: 8 }
  });
  document.getElementById("regForm").addEventListener("submit", function (e) {
    if (e.defaultPrevented) return;
    var btn = document.getElementById("regSubmit");
    btn.querySelector(".btn-submit-label").classList.add("d-none");
    btn.querySelector(".btn-submit-spin").classList.remove("d-none");
  });
</script>
</body>
</html>
