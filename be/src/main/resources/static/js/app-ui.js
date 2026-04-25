/**
 * Shared UI: Bootstrap toasts, helpers. Loads after bootstrap.bundle.
 */
(function (global) {
  "use strict";

  function getContainer() {
    return document.getElementById("appToastContainer");
  }

  function showToast(type, message) {
    if (!message) return;
    var c = getContainer();
    if (!c || typeof bootstrap === "undefined") {
      console.warn("[AppUi]", type, message);
      return;
    }
    var bg =
      type === "success"
        ? "text-bg-success"
        : type === "error" || type === "danger"
          ? "text-bg-danger"
          : type === "warning"
            ? "text-bg-warning"
            : "text-bg-primary";

    var closeClass =
      type === "warning" ? "btn-close" : "btn-close btn-close-white";

    var el = document.createElement("div");
    el.className = "toast align-items-center " + bg + " border-0";
    el.setAttribute("role", "alert");
    el.setAttribute("aria-live", "assertive");
    el.innerHTML =
      '<div class="d-flex">' +
      '<div class="toast-body fw-medium"></div>' +
      '<button type="button" class="' +
      closeClass +
      ' me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
      "</div>";
    el.querySelector(".toast-body").textContent = message;
    c.appendChild(el);
    var t = new bootstrap.Toast(el, { delay: type === "error" || type === "danger" ? 6000 : 4500 });
    t.show();
    el.addEventListener("hidden.bs.toast", function () {
      el.remove();
    });
  }

  function setSubmitLoading(form, loading) {
    if (!form) return;
    var btn = form.querySelector('[type="submit"]');
    if (btn) {
      btn.disabled = !!loading;
      var spin = btn.querySelector(".btn-submit-spin");
      var lbl = btn.querySelector(".btn-submit-label");
      if (spin) spin.classList.toggle("d-none", !loading);
      if (lbl) lbl.classList.toggle("d-none", !!loading);
    }
  }

  global.AppUi = {
    showToast: showToast,
    setSubmitLoading: setSubmitLoading,
  };
})(window);
