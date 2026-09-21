document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("chatToggle");
    const panel = document.getElementById("chatPanel");
    const closeBtn = document.getElementById("chatClose");
    const form = document.getElementById("chatForm");
    const input = document.getElementById("chatInput");
    const messages = document.getElementById("chatMessages");

    toggle.addEventListener("click", () => panel.classList.toggle("hidden"));
    closeBtn.addEventListener("click", () => panel.classList.add("hidden"));

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const text = input.value.trim();
        if (!text) return;
        appendMessage("user", text);
        input.value = "";
        try {
            const data = await api.post("/chat", { message: text });
            appendMessage("bot", data.reply);
        } catch (err) {
            appendMessage("bot", "Sorry, I couldn't process that right now.");
        }
    });

    function appendMessage(who, text) {
        const div = document.createElement("div");
        div.className = "chat-msg " + who;
        div.textContent = text;
        messages.appendChild(div);
        messages.scrollTop = messages.scrollHeight;
    }
});
