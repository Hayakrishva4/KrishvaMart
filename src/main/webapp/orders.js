const STATUS_FLOW = { PENDING: "CONFIRMED", CONFIRMED: "SHIPPED", SHIPPED: "DELIVERED" };

async function loadOrders() {
    const container = document.getElementById("ordersList");
    try {
        const me = await api.get("/auth/me");
        const orders = await api.get("/orders");
        if (orders.length === 0) {
            container.innerHTML = "<p>No orders yet.</p>";
            return;
        }
        container.innerHTML = orders.map(o => renderOrder(o, me.role)).join("");
        wireStatusButtons();
    } catch (err) {
        container.innerHTML = "<p>Could not load orders: " + escapeHtml(err.message) + "</p>";
    }
}

function renderOrder(o, role) {
    const items = o.items.map(i =>
        `<li>${escapeHtml(i.productName)} &times; ${i.quantity} (${formatMoney(i.unitPrice)} each)</li>`
    ).join("");
    const nextStatus = STATUS_FLOW[o.status];
    const canAdvance = (role === "SELLER" || role === "ADMIN") && nextStatus;
    return `
        <div class="order-card">
            <strong>Order #${o.id}</strong>
            <span class="status-badge">${escapeHtml(o.status)}</span>
            <div>Total: ${formatMoney(o.totalAmount)}</div>
            ${o.shippingAddress ? `<div>Ship to: ${escapeHtml(o.shippingAddress)}</div>` : ""}
            <ul>${items}</ul>
            ${canAdvance
                ? `<button class="advanceBtn" data-order-id="${o.id}" data-next="${nextStatus}">Mark as ${nextStatus}</button>`
                : ""}
        </div>
    `;
}

function wireStatusButtons() {
    document.querySelectorAll(".advanceBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const orderId = e.target.dataset.orderId;
            const next = e.target.dataset.next;
            try {
                await api.patch("/orders/" + orderId + "/status", { status: next });
                loadOrders();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}

loadOrders();
