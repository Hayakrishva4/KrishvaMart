async function loadAdminData() {
    try {
        const users = await api.get("/admin/users");
        document.getElementById("adminUsers").innerHTML = users.map(u => `
            <div class="user-row">
                ${escapeHtml(u.name)} &lt;${escapeHtml(u.email)}&gt; &mdash; <strong>${escapeHtml(u.role)}</strong>
            </div>
        `).join("") || "<p>No users.</p>";

        const orders = await api.get("/admin/orders");
        document.getElementById("adminOrders").innerHTML = orders.map(o => `
            <div class="order-card">
                Order #${o.id} &mdash; <span class="status-badge">${escapeHtml(o.status)}</span>
                &mdash; ${formatMoney(o.totalAmount)}
            </div>
        `).join("") || "<p>No orders.</p>";
    } catch (err) {
        document.getElementById("adminUsers").innerHTML = "<p>" + escapeHtml(err.message) + "</p>";
    }
}

document.getElementById("moderateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const msg = document.getElementById("moderateMessage");
    const productId = document.getElementById("moderateProductId").value;
    try {
        await api.del("/admin/products/" + productId);
        msg.textContent = "Listing deactivated.";
    } catch (err) {
        msg.textContent = err.message;
    }
});

loadAdminData();
