document.addEventListener("DOMContentLoaded", () => {
    // 1) 기존 id 우선, 없으면 BEM 클래스 fallback
    const navBars =
        document.getElementById("nav-bars") ||
        document.querySelector(".admin-topbar__toggle");

    const sidebar =
        document.getElementById("sidebar") ||
        document.querySelector(".admin-sidebar");

    // 어떤 페이지에는 sidebar가 없을 수도 있으니 방어
    if (!navBars || !sidebar) return;

    const OPEN_CLASS = "open";

    // 2) 토글 버튼 클릭
    navBars.addEventListener("click", (e) => {
        e.stopPropagation();
        sidebar.classList.toggle(OPEN_CLASS);
    });

    // 3) 바깥 클릭 시 닫기
    document.addEventListener("click", (e) => {
        if (!sidebar.classList.contains(OPEN_CLASS)) return;

        const clickedInsideSidebar = sidebar.contains(e.target);
        const clickedToggle = navBars.contains(e.target);

        if (!clickedInsideSidebar && !clickedToggle) {
            sidebar.classList.remove(OPEN_CLASS);
        }
    });

    // 4) 사이드바 내부 클릭은 전파 막기 (바깥 클릭 닫힘 방지)
    sidebar.addEventListener("click", (e) => {
        e.stopPropagation();
    });

    // 5) ESC로 닫기(선택: UX 개선, 문제 없으면 유지)
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            sidebar.classList.remove(OPEN_CLASS);
        }
    });
});
