<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <%@ include file="common/head-bootstrap.jspf" %>
  <title>New booking</title>
</head>
<body class="d-flex flex-column min-vh-100">
<%@ include file="common/header.jspf" %>
<main class="container flex-grow-main py-4 py-lg-5">
  <nav aria-label="breadcrumb" class="breadcrumb-modern mb-3">
    <ol class="breadcrumb mb-0">
      <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/parking-areas" class="text-decoration-none">Areas</a></li>
      <li class="breadcrumb-item active" aria-current="page">New booking</li>
    </ol>
  </nav>

  <div class="mb-4">
    <h1 class="h2 fw-bold mb-1">New booking</h1>
    <p class="text-muted mb-0">Area: <strong><c:out value="${area.name}"/></strong></p>
  </div>

  <div class="booking-steps row g-2 mb-4 text-center">
    <div class="col-4">
      <div class="booking-step-card active" id="stepCard1" data-step="1">
        <span class="step-num">1</span>Slot
      </div>
    </div>
    <div class="col-4">
      <div class="booking-step-card" id="stepCard2" data-step="2">
        <span class="step-num">2</span>Time
      </div>
    </div>
    <div class="col-4">
      <div class="booking-step-card" id="stepCard3" data-step="3">
        <span class="step-num">3</span>Confirm
      </div>
    </div>
  </div>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show border-0 rounded-3 shadow-sm" role="alert">
      <i class="bi bi-exclamation-triangle-fill me-2"></i>
      <c:out value="${errorMessage}"/>
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  </c:if>

  <form method="post" action="${pageContext.request.contextPath}/bookings" id="bookingForm" class="needs-validation" novalidate>
    <sec:csrfInput/>
    <input type="hidden" name="parkingAreaId" value="${area.id}"/>
    <input type="hidden" name="parkingSlotId" id="parkingSlotId" value="" required/>

    <div class="row g-4">
      <div class="col-lg-7">
        <div class="card border-0 app-card shadow-sm">
          <div class="card-body p-4">
            <h2 class="h6 text-uppercase text-muted fw-bold mb-1"><i class="bi bi-grid-3x3-gap me-1"></i>Select a slot</h2>
            <p class="small text-muted mb-4">Slots refresh live when others book. Unavailable slots are faded.</p>
            <div id="slotGrid" class="row g-3">
              <c:forEach var="s" items="${slots}">
                <div class="col-md-4 col-sm-6">
                  <c:set var="avail" value="${s.status.name() eq 'AVAILABLE'}"/>
                  <div class="card h-100 slot-card ${avail ? 'slot-selectable' : 'slot-card--disabled opacity-50'}"
                       data-slot-id="${s.id}">
                    <div class="card-body">
                      <div class="d-flex justify-content-between align-items-start mb-2">
                        <strong class="h6 mb-0 font-monospace"><c:out value="${s.code}"/></strong>
                        <span class="badge rounded-pill badge-slot-${fn:toLowerCase(s.status.name())}">
                          <c:out value="${s.status}"/>
                        </span>
                      </div>
                      <p class="small text-muted mb-0"><i class="bi bi-layers me-1"></i>Floor: <c:out value="${s.floor}"/></p>
                    </div>
                  </div>
                </div>
              </c:forEach>
            </div>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="card border-0 app-card shadow-sm">
          <div class="card-body p-4">
            <h2 class="h6 text-uppercase text-muted fw-bold mb-3"><i class="bi bi-clock me-1"></i>Time &amp; payment</h2>
            <div class="mb-3">
              <label class="form-label fw-semibold" for="startAt">Start</label>
              <input class="form-control form-control-modern" type="datetime-local" name="startAt" id="startAt" required value="${defaultStart}" step="60"/>
              <div class="invalid-feedback">Choose a valid start time.</div>
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold" for="endAt">End</label>
              <input class="form-control form-control-modern" type="datetime-local" name="endAt" id="endAt" required value="${defaultEnd}" step="60"/>
              <div class="invalid-feedback">End must be after start.</div>
            </div>
            <div class="mb-3">
              <label class="form-label fw-semibold">Payment</label>
              <div class="border rounded-3 p-3 bg-light bg-opacity-50">
                <div class="form-check mb-2">
                  <input class="form-check-input" type="radio" name="paymentMethod" id="payCash" value="CASH" checked/>
                  <label class="form-check-label" for="payCash"><i class="bi bi-cash-stack me-1 text-success"></i>Cash (confirm now)</label>
                </div>
                <div class="form-check mb-0">
                  <input class="form-check-input" type="radio" name="paymentMethod" id="payCard" value="STRIPE" ${stripeEnabled ? '' : 'disabled'}/>
                  <label class="form-check-label" for="payCard">
                    <i class="bi bi-credit-card me-1 text-primary"></i>Card (Stripe Checkout)
                    <c:if test="${!stripeEnabled}"><span class="badge bg-secondary ms-1">not configured</span></c:if>
                  </label>
                </div>
              </div>
            </div>
            <div class="mb-4">
              <label class="form-label fw-semibold" for="notes">Notes <span class="text-muted fw-normal">(optional)</span></label>
              <textarea class="form-control form-control-modern" name="notes" id="notes" rows="3" maxlength="500" placeholder="Vehicle, access notes…"></textarea>
            </div>
            <button type="button" class="btn btn-primary w-100 py-2" id="openConfirmBtn">
              <i class="bi bi-arrow-right-circle me-1"></i>Review &amp; continue
            </button>
          </div>
        </div>
      </div>
    </div>
  </form>

  <div class="modal fade" id="confirmModal" tabindex="-1" aria-labelledby="confirmModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header border-0 pb-0">
          <h2 class="modal-title fs-5 fw-bold" id="confirmModalLabel"><i class="bi bi-check2-circle text-primary me-2"></i>Confirm booking</h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <ul class="list-unstyled small mb-0">
            <li class="mb-2"><strong class="text-muted">Slot</strong><br/><span id="summarySlot">-</span></li>
            <li class="mb-2"><strong class="text-muted">Start</strong><br/><span id="summaryStart">-</span></li>
            <li class="mb-2"><strong class="text-muted">End</strong><br/><span id="summaryEnd">-</span></li>
            <li class="mb-0"><strong class="text-muted">Payment</strong><br/><span id="summaryPay">-</span></li>
          </ul>
        </div>
        <div class="modal-footer border-0 pt-0">
          <button type="button" class="btn btn-outline-secondary rounded-pill" data-bs-dismiss="modal">Back</button>
          <button type="button" class="btn btn-primary rounded-pill px-4" id="confirmSubmitBtn">
            <span class="btn-submit-label">Submit booking</span>
            <span class="spinner-border spinner-border-sm d-none btn-submit-spin ms-1" role="status" aria-hidden="true"></span>
          </button>
        </div>
      </div>
    </div>
  </div>

  <div id="loadingOverlay" class="app-loading-overlay position-fixed top-0 start-0 w-100 h-100 d-none align-items-center justify-content-center">
    <div class="text-center text-white">
      <div class="spinner-border" role="status"><span class="visually-hidden">Loading…</span></div>
      <p class="small mt-3 mb-0 fw-semibold">Processing…</p>
    </div>
  </div>

  <p class="mt-4 mb-0">
    <a href="${pageContext.request.contextPath}/parking-areas" class="text-decoration-none text-muted">
      <i class="bi bi-arrow-left me-1"></i>Back to areas
    </a>
  </p>
</main>
<%@ include file="common/footer.jspf" %>
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1.6.1/dist/sockjs.min.js" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js" crossorigin="anonymous"></script>
<script>
  window.__BOOKING_CTX__ = "${pageContext.request.contextPath}";
  window.__BOOKING_AREA_ID__ = "${area.id}";
</script>
<script src="${pageContext.request.contextPath}/js/booking-live.js"></script>
<script>
  (function () {
    var form = document.getElementById("bookingForm");
    var slotInput = document.getElementById("parkingSlotId");
    var startAt = document.getElementById("startAt");
    var endAt = document.getElementById("endAt");
    var step1 = document.getElementById("stepCard1");
    var step2 = document.getElementById("stepCard2");
    var step3 = document.getElementById("stepCard3");

    function toast(msg, type) {
      if (window.AppUi) AppUi.showToast(type || "warning", msg);
      else alert(msg);
    }

    function updateSteps() {
      var sid = slotInput.value;
      var st = startAt.value;
      var en = endAt.value;
      var timeOk = st && en && st < en;
      [step1, step2, step3].forEach(function (el) {
        el.classList.remove("active", "done");
      });
      if (!sid) {
        step1.classList.add("active");
      } else if (!timeOk) {
        step1.classList.add("done");
        step2.classList.add("active");
      } else {
        step1.classList.add("done");
        step2.classList.add("done");
        step3.classList.add("active");
      }
    }

    function validateTimes() {
      if (!startAt.value || !endAt.value) {
        startAt.classList.add("is-invalid");
        endAt.classList.add("is-invalid");
        return false;
      }
      if (startAt.value >= endAt.value) {
        startAt.classList.add("is-invalid");
        endAt.classList.add("is-invalid");
        toast("End time must be after start time.", "danger");
        return false;
      }
      startAt.classList.remove("is-invalid");
      endAt.classList.remove("is-invalid");
      return true;
    }

    startAt.addEventListener("change", updateSteps);
    endAt.addEventListener("change", updateSteps);
    document.getElementById("slotGrid").addEventListener("click", function () {
      setTimeout(updateSteps, 0);
    });
    window.__bookingStepRefresh = updateSteps;
    updateSteps();

    document.getElementById("openConfirmBtn").addEventListener("click", function () {
      if (!slotInput.value) {
        toast("Please select an available slot.", "warning");
        return;
      }
      if (!validateTimes()) return;
      var card = document.querySelector('#slotGrid [data-slot-id="' + slotInput.value + '"]');
      document.getElementById("summarySlot").textContent = card ? card.querySelector("strong").textContent : slotInput.value;
      document.getElementById("summaryStart").textContent = startAt.value;
      document.getElementById("summaryEnd").textContent = endAt.value;
      document.getElementById("summaryPay").textContent = document.getElementById("payCard").checked ? "Card (Stripe)" : "Cash";
      step3.classList.remove("active");
      step3.classList.add("done");
      var modal = new bootstrap.Modal(document.getElementById("confirmModal"));
      modal.show();
    });

    document.getElementById("confirmSubmitBtn").addEventListener("click", function () {
      if (!slotInput.value) {
        toast("Please select an available slot for this time range.", "warning");
        return;
      }
      if (!validateTimes()) return;
      var overlay = document.getElementById("loadingOverlay");
      overlay.classList.remove("d-none");
      overlay.classList.add("d-flex");
      document.querySelector("#confirmSubmitBtn .btn-submit-label").classList.add("d-none");
      document.querySelector("#confirmSubmitBtn .btn-submit-spin").classList.remove("d-none");
      form.submit();
    });
  })();
</script>
</body>
</html>
