document.addEventListener("DOMContentLoaded", () => {
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
});
