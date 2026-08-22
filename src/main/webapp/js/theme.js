document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("themeToggle");
    if (!toggle) return;

    updateToggleIcon();

    toggle.addEventListener("click", () => {
        const isDark = document.documentElement.getAttribute("data-theme") === "dark";
        if (isDark) {
            document.documentElement.removeAttribute("data-theme");
            localStorage.setItem("krishvamart-theme", "light");
        } else {
            document.documentElement.setAttribute("data-theme", "dark");
            localStorage.setItem("krishvamart-theme", "dark");
        }
        updateToggleIcon();
    });

    function updateToggleIcon() {
        const isDark = document.documentElement.getAttribute("data-theme") === "dark";
        toggle.innerHTML = isDark ? "&#9789;" : "&#9788;";
    }
});
