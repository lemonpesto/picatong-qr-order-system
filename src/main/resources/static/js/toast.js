// toast.js
(function () {
  const toasts = document.querySelectorAll(".toast");

  toasts.forEach(function (toast) {
    toast.classList.add("show");

    setTimeout(function () {
      toast.classList.remove("show");
    }, 2000);
  });
})();