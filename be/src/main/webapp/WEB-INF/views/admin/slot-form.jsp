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
  <p class="text-muted small mb-4">Code must be unique within the same parking area.</p>

  <c:if test="${not empty msgError}"><div class="alert alert-danger border-0 rounded-3"><c:out value="${msgError}"/></div></c:if>

  <c:choose>
    <c:when test="${empty slot}">
      <form class="card app-card border-0 shadow-sm p-4" action="${pageContext.request.contextPath}/admin/slots" method="post">
        <sec:csrfInput/>
        <div class="mb-3">
          <label class="form-label" for="parkingAreaId">Parking area *</label>
          <select class="form-select" id="parkingAreaId" name="parkingAreaId" required>
            <option value="" selected disabled>— Chọn bãi —</option>
            <c:forEach var="a" items="${areas}">
              <option value="${a.id}"><c:out value="${a.name}"/></option>
            </c:forEach>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label" for="code">Slot code *</label>
          <input class="form-control" id="code" name="code" required maxlength="64" placeholder="e.g. A-101"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="floor">Floor</label>
          <input class="form-control" id="floor" name="floor" maxlength="32"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="status">Status *</label>
          <select class="form-select" id="status" name="status">
            <option value="AVAILABLE" selected>AVAILABLE</option>
            <option value="MAINTENANCE">MAINTENANCE</option>
          </select>
        </div>
        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-primary rounded-pill">Save</button>
          <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/slots">Cancel</a>
        </div>
      </form>
    </c:when>
    <c:otherwise>
      <form class="card app-card border-0 shadow-sm p-4" action="${pageContext.request.contextPath}/admin/slots/${slot.id}" method="post">
        <sec:csrfInput/>
        <div class="mb-3">
          <label class="form-label" for="parkingAreaId">Parking area *</label>
          <select class="form-select" id="parkingAreaId" name="parkingAreaId" required>
            <c:forEach var="a" items="${areas}">
              <option value="${a.id}" <c:if test="${a.id eq slot.parkingAreaId}">selected="selected"</c:if>><c:out value="${a.name}"/></option>
            </c:forEach>
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label" for="code">Slot code *</label>
          <input class="form-control" id="code" name="code" required maxlength="64" value="${slot.code}"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="floor">Floor</label>
          <input class="form-control" id="floor" name="floor" maxlength="32" value="${slot.floor}"/>
        </div>
        <div class="mb-3">
          <label class="form-label" for="status">Status *</label>
          <c:choose>
            <c:when test="${slot.status.name() eq 'BOOKED'}">
              <p class="small text-muted mb-2">This slot is <strong>BOOKED</strong> (occupied). You can keep it or set to <strong>MAINTENANCE</strong> to take offline.</p>
              <select class="form-select" id="status" name="status">
                <option value="BOOKED" selected>BOOKED</option>
                <option value="MAINTENANCE">MAINTENANCE</option>
              </select>
            </c:when>
            <c:otherwise>
              <select class="form-select" id="status" name="status">
                <option value="AVAILABLE" ${slot.status.name() eq 'AVAILABLE' ? 'selected' : ''}>AVAILABLE</option>
                <option value="MAINTENANCE" ${slot.status.name() eq 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
              </select>
            </c:otherwise>
          </c:choose>
        </div>
        <div class="d-flex gap-2">
          <button type="submit" class="btn btn-primary rounded-pill">Update</button>
          <a class="btn btn-outline-secondary rounded-pill" href="${pageContext.request.contextPath}/admin/slots">Cancel</a>
        </div>
      </form>
    </c:otherwise>
  </c:choose>
</main>
<%@ include file="../common/footer.jspf" %>
</body>
</html>
