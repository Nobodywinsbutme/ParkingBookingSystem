(function () {
  var ctx = window.__BOOKING_CTX__ || "";
  var areaId = window.__BOOKING_AREA_ID__ || "";

  function badgeClass(status) {
    if (status === "AVAILABLE") return "badge rounded-pill badge-slot-available";
    if (status === "BOOKED") return "badge rounded-pill badge-slot-booked";
    if (status === "MAINTENANCE") return "badge rounded-pill badge-slot-maintenance";
    return "badge rounded-pill bg-light text-dark";
  }

  function renderSlots(slots, selectedId) {
    var grid = document.getElementById("slotGrid");
    if (!grid || !slots) return;
    grid.innerHTML = "";
    slots.forEach(function (s) {
      var col = document.createElement("div");
      col.className = "col-md-4 col-sm-6";
      var selectable = s.selectableForRange === true;
      var card = document.createElement("div");
      card.className =
        "card h-100 slot-card " +
        (selectable ? "slot-selectable" : "slot-card--disabled opacity-50") +
        (selectedId === s.id ? " slot-card--selected" : "");
      card.setAttribute("data-slot-id", s.id);
      card.innerHTML =
        '<div class="card-body">' +
        '<div class="d-flex justify-content-between align-items-start mb-2">' +
        '<strong class="h6 mb-0 font-monospace">' +
        escapeHtml(s.code) +
        "</strong>" +
        '<span class="' +
        badgeClass(s.status) +
        '">' +
        escapeHtml(s.status) +
        "</span>" +
        "</div>" +
        '<p class="small text-muted mb-0"><i class="bi bi-layers me-1"></i>Floor: ' +
        escapeHtml(s.floor || "—") +
        "</p>" +
        (!selectable
          ? '<p class="small text-danger mb-0 mt-2"><i class="bi bi-slash-circle me-1"></i>Not available for this range</p>'
          : '<p class="small text-success mb-0 mt-2"><i class="bi bi-cursor me-1"></i>Tap to select</p>') +
        "</div>";
      col.appendChild(card);
      grid.appendChild(col);
    });
  }

  function escapeHtml(str) {
    if (!str) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function qs(sel) {
    return document.querySelector(sel);
  }

  function wireSlotGridClicks() {
    var grid = document.getElementById("slotGrid");
    if (!grid || grid.dataset.delegationWired === "1") return;
    grid.dataset.delegationWired = "1";
    grid.addEventListener("click", function (ev) {
      var card = ev.target.closest(".slot-selectable[data-slot-id]");
      if (!card) return;
      var id = card.getAttribute("data-slot-id");
      var hidden = document.getElementById("parkingSlotId");
      if (hidden) hidden.value = id;
      grid.querySelectorAll(".slot-card").forEach(function (el) {
        el.classList.remove("slot-card--selected");
      });
      card.classList.add("slot-card--selected");
      if (typeof window.__bookingStepRefresh === "function") window.__bookingStepRefresh();
    });
  }

  function loadSlots() {
    var startAt = qs("#startAt");
    var endAt = qs("#endAt");
    if (!startAt || !endAt || !startAt.value || !endAt.value) return;
    var url =
      ctx +
      "/api/parking-areas/" +
      encodeURIComponent(areaId) +
      "/slots?startAt=" +
      encodeURIComponent(startAt.value) +
      "&endAt=" +
      encodeURIComponent(endAt.value);
    var sel = qs("#parkingSlotId") ? qs("#parkingSlotId").value : "";
    fetch(url, { credentials: "same-origin" })
      .then(function (r) {
        if (!r.ok) throw new Error("Could not load slots");
        return r.json();
      })
      .then(function (data) {
        renderSlots(data, sel);
      })
      .catch(function () {
        /* keep existing markup */
      });
  }

  function wireStomp() {
    if (typeof SockJS === "undefined" || typeof Stomp === "undefined") return;
    var socket = new SockJS(ctx + "/ws");
    var client = Stomp.over(socket);
    client.debug = function () {};
    client.connect({}, function () {
      client.subscribe("/topic/slots", function (message) {
        try {
          var body = JSON.parse(message.body);
          if (body.areaId === areaId) {
            loadSlots();
          }
        } catch (e) {
          /* ignore */
        }
      });
    });
  }

  document.addEventListener("DOMContentLoaded", function () {
    wireSlotGridClicks();
    var startAt = qs("#startAt");
    var endAt = qs("#endAt");
    if (startAt)
      startAt.addEventListener("change", function () {
        loadSlots();
      });
    if (endAt)
      endAt.addEventListener("change", function () {
        loadSlots();
      });
    loadSlots();
    wireStomp();
  });
})();
