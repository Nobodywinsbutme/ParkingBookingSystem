<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>My bookings</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <div class="d-flex flex-wrap align-items-center justify-content-between gap-3 mb-4">
    <div>
      <h1 class="h2 fw-bold mb-1">My bookings</h1>
      <p class="text-muted mb-0 small">Track status, pay pending Stripe checkouts, or cancel when allowed.</p>
    </div>
    <a class="btn btn-primary rounded-pill px-4" href="${pageContext.request.contextPath}/parking-areas">
      <i class="bi bi-plus-lg me-1"></i>Book another
    </a>
  </div>

  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show border-0 rounded-3 d-none" role="alert" id="flashSuccessBanner">
      <c:out value="${successMessage}"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <span id="flashSuccessText" class="d-none"><c:out value="${successMessage}"/></span>
  </c:if>
  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show border-0 rounded-3 d-none" role="alert" id="flashErrorBanner">
      <c:out value="${errorMessage}"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
    <span id="flashErrorText" class="d-none"><c:out value="${errorMessage}"/></span>
  </c:if>

  <c:if test="${empty bookings}">
    <div class="alert alert-secondary border-0 rounded-3 shadow-sm">No bookings yet.</div>
  </c:if>
  <c:if test="${not empty bookings}">
    <div class="card border-0 app-card shadow-sm overflow-hidden">
      <div class="table-responsive">
        <table class="table table-modern table-hover align-middle mb-0">
          <thead class="table-light">
          <tr>
            <th>ID</th>
            <th>Booking</th>
            <th>Payment</th>
            <th>Start</th>
            <th>End</th>
            <th>Total</th>
            <th class="text-end">Actions</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="b" items="${bookings}">
            <tr>
              <td><code class="small text-muted"><c:out value="${b.id()}"/></code></td>
              <td>
                <c:choose>
                  <c:when test="${b.status().name() eq 'CONFIRMED'}">
                    <c:set var="stClass" value="success"/>
                  </c:when>
                  <c:when test="${b.status().name() eq 'PENDING'}">
                    <c:set var="stClass" value="warning text-dark"/>
                  </c:when>
                  <c:when test="${b.status().name() eq 'CANCELLED'}">
                    <c:set var="stClass" value="secondary"/>
                  </c:when>
                  <c:when test="${b.status().name() eq 'COMPLETED'}">
                    <c:set var="stClass" value="info text-dark"/>
                  </c:when>
                  <c:otherwise>
                    <c:set var="stClass" value="primary"/>
                  </c:otherwise>
                </c:choose>
                <span class="badge bg-${stClass} rounded-pill">
                  <c:out value="${b.status()}"/>
                </span>
              </td>
              <td>
                <c:forEach var="p" items="${b.payments()}">
                  <span class="badge bg-light text-dark border me-1 rounded-pill"><c:out value="${p.method()}"/> · <c:out value="${p.status()}"/></span>
                </c:forEach>
              </td>
              <td class="small"><c:out value="${b.startAt()}"/></td>
              <td class="small"><c:out value="${b.endAt()}"/></td>
              <td class="fw-semibold"><c:out value="${b.amountTotal()}"/> <span class="text-muted small"><c:out value="${b.currency()}"/></span></td>
              <td class="text-end text-nowrap">
                <c:if test="${b.status().name() eq 'PENDING'}">
                  <c:forEach var="p" items="${b.payments()}">
                    <c:if test="${p.method().name() eq 'STRIPE' && p.status().name() eq 'PENDING'}">
                      <a class="btn btn-sm btn-primary rounded-pill mb-1" href="${pageContext.request.contextPath}/payment/checkout?bookingId=${b.id()}">
                        <i class="bi bi-credit-card me-1"></i>Pay
                      </a>
                    </c:if>
                  </c:forEach>
                </c:if>
                <c:if test="${b.status().name() eq 'PENDING' || b.status().name() eq 'CONFIRMED'}">
                  <form class="d-inline" method="post" action="${pageContext.request.contextPath}/bookings/${b.id()}/cancel" onsubmit="return confirm('Cancel this booking?');">
                    <sec:csrfInput/>
                    <button type="submit" class="btn btn-sm btn-outline-danger rounded-pill mb-1">Cancel</button>
                  </form>
                </c:if>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </c:if>
</main>
<%@ include file="common/footer.jspf" %>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    var ok = document.getElementById("flashSuccessText");
    if (ok && ok.textContent && window.AppUi) AppUi.showToast("success", ok.textContent.trim());
    var er = document.getElementById("flashErrorText");
    if (er && er.textContent && window.AppUi) AppUi.showToast("danger", er.textContent.trim());
  });
</script>
</body>
</html>
