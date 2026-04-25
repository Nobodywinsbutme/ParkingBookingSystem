<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>Smart Parking</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="flex-grow-main">
  <section class="hero-section">
    <div class="container hero-content">
      <div class="row align-items-center">
        <div class="col-lg-7">
          <h1 class="hero-title text-white">Smart parking, booked in seconds</h1>
          <p class="hero-lead">See available spots in real time, reserve instantly, and pay securely—all in one place.</p>
          <a class="btn btn-light btn-hero-cta text-primary" href="${pageContext.request.contextPath}/parking-areas">
            <span class="hero-cta-icon" aria-hidden="true">🚗</span>
            Browse parking areas
          </a>
        </div>
        <div class="col-lg-5 d-none d-lg-block text-center">
          <div class="rounded-4 bg-white bg-opacity-10 p-4 border border-white border-opacity-25">
            <i class="bi bi-car-front-fill display-1 text-white opacity-75"></i>
            <p class="small text-white-50 mb-0 mt-2">Real-time updates · Secure payments · Fast booking</p>
          </div>
        </div>
      </div>
    </div>
  </section>

  <div class="features-strip">
    <div class="container">
      <div class="row g-4">
        <div class="col-md-4">
          <div class="card app-card h-100 border-0">
            <div class="card-body">
              <div class="app-card-icon">
                <i class="bi bi-broadcast"></i>
              </div>
              <h2 class="app-card-title">Live availability</h2>
              <p class="text-muted small mb-0">See available spots instantly—no need to refresh.</p>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card app-card h-100 border-0">
            <div class="card-body">
              <div class="app-card-icon">
                <i class="bi bi-credit-card-2-front"></i>
              </div>
              <h2 class="app-card-title">Secure payments</h2>
              <p class="text-muted small mb-0">Pay quickly and safely with your card.</p>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="card app-card h-100 border-0">
            <div class="card-body">
              <div class="app-card-icon">
                <i class="bi bi-shield-check"></i>
              </div>
              <h2 class="app-card-title">Reliable system</h2>
              <p class="text-muted small mb-0">Fast, stable, and easy to use.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</main>
<%@ include file="common/footer.jspf" %>
</body>
</html>
