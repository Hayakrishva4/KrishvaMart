document.getElementById("registerForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById("registerError");
    errorEl.textContent = "";
    const name = document.getElementById("name").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const role = document.getElementById("role").value;
    try {
        await api.post("/auth/register", { name, email, password, role });
        window.location.href = "index.jsp";
    } catch (err) {
        errorEl.textContent = err.message;
    }
});
