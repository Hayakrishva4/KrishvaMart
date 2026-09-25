document.addEventListener("DOMContentLoaded", async () => {
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", async () => {
            try {
                await api.post("/auth/logout");
            } finally {
                window.location.href = window.location.pathname.includes("/jsp/")
                    ? "index.jsp"
                    : "jsp/index.jsp";
            }
        });
    }
    try {
        const res = await api.get("/auth/me");
        const user = (res && res.data) ? res.data : res;
        const role = user && user.role ? String(user.role).toUpperCase() : "GUEST";
        const navLinks = document.querySelectorAll("a");   
        navLinks.forEach(link => {
            const text = link.textContent.trim().toLowerCase();
            const href = link.getAttribute("href") ? link.getAttribute("href").toLowerCase() : "";
            if ((text === "admin" || href.includes("admin")) && role !== "ADMIN") {
                link.style.display = "none";
            }
            if ((text === "sell" || href.includes("seller")) && role !== "SELLER" && role !== "ADMIN") {
                link.style.display = "none";
            }
        });
    } catch (err) {
        document.querySelectorAll("a").forEach(link => {
            const text = link.textContent.trim().toLowerCase();
            const href = link.getAttribute("href") ? link.getAttribute("href").toLowerCase() : "";
            if (text === "admin" || href.includes("admin") || text === "sell" || href.includes("seller")) {
                link.style.display = "none";
            }
        });
    }
});