<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="../common/head-bootstrap.jspf" %>
  <title><c:out value="${formTitle}"/></title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="../common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5" style="max-width: 36rem">
  <%@ include file="admin-nav.jspf" %>

  <h1 class="h2 fw-bold mb-1"><c:out value="${formTitle}"/></h1>
  <p class="text-muted small mb-4">Fields marked * are required.</p>

  <c:if test="${not empty msgError}"><div class="alert alert-danger border-0 rounded-3"><c:out value="${msgError}"/></div></c:if>

  <c:choose>
    <c:when test="${empty area}">
      <form class="card app-card border-0 shadow-sm p-4" action="${pageContext.request.contextPath}/admin/areas" method="post">
        <sec:csrfInput/>
        <div class="mb-3">
          <label class="form-label" for="name">Name *</label>
          <input class="form-control" id="name" name="name" required maxlength="255" value=""/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="city">City</label>
          <input class="form-control" id="city" name="city" maxlength="255"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="addressLine1">Address</label>
          <input class="form-control" id="addressLine1" name="addressLine1" maxlength="512"/>
        </div>
        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-primary rounded-pill">Save</button>
          <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/areas">Cancel</a>
        </div>
      </form>
    </c:when>
    <c:otherwise>
      <form class="card app-card border-0 shadow-sm p-4" action="${pageContext.request.contextPath}/admin/areas/${area.id}" method="post">
        <sec:csrfInput/>
        <div class="mb-3">
          <label class="form-label" for="name">Name *</label>
          <input class="form-control" id="name" name="name" required maxlength="255" value="${area.name}"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="city">City</label>
          <input class="form-control" id="city" name="city" maxlength="255" value="${area.city}"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="addressLine1">Address</label>
          <input class="form-control" id="addressLine1" name="addressLine1" maxlength="512" value="${area.addressLine1}"/>
        </div>
        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-primary rounded-pill">Update</button>
          <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/areas">Cancel</a>
        </div>
      </form>
    </c:otherwise>
  </c:choose>
</main>
<%@ include file="../common/footer.jspf" %>
</body>
</html>
