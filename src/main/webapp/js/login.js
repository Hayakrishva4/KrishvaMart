document.getElementById("loginForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById("loginError");
    errorEl.textContent = "";
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    try {
        await api.post("/auth/login", { email, password });
        window.location.href = "index.jsp";
    } catch (err) {
        errorEl.textContent = err.message;
    }
});
