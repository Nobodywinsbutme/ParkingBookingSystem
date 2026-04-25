/**
 * Vanilla JS: client-side hints before submit (server still validates).
 */
function wireFormValidation(form, rules) {
  if (!form || !rules) return;

  form.addEventListener("submit", function (event) {
    var messages = [];
    Object.keys(rules).forEach(function (field) {
      var el = form.elements[field];
      if (!el) return;
      var r = rules[field];
      var v = (el.value || "").trim();
      if (r.required && !v) {
        messages.push(field + " is required.");
      }
      if (r.email && v && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) {
        messages.push("Enter a valid email.");
      }
      if (r.minLength && v.length > 0 && v.length < r.minLength) {
        messages.push(field + " must be at least " + r.minLength + " characters.");
      }
    });
    if (messages.length) {
      event.preventDefault();
      var text = messages.join(" ");
      if (window.AppUi) {
        AppUi.showToast("warning", text);
      } else {
        alert(text);
      }
    }
  });
}
