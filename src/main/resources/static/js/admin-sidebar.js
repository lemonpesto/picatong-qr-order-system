document.addEventListener("DOMContentLoaded", () => {
    const navBars = document.getElementById("nav-bars");
    const sidebar = document.getElementById("sidebar");

    // 어떤 페이지(혹은 로그인 페이지)에는 sidebar가 없을 수도 있으니 방어
    if (!navBars || !sidebar) return;

    navBars.addEventListener("click", (e) => {
        e.stopPropagation();
        sidebar.classList.toggle("open");
    });

    document.addEventListener("click", (e) => {
        if (sidebar.classList.contains("open") && !sidebar.contains(e.target) && e.target !== navBars) {
            sidebar.classList.remove("open");
        }
    });

    sidebar.addEventListener("click", (e) => {
        e.stopPropagation();
    });
});