if (typeof window.escapeHtml !== "function") {
    window.escapeHtml = function (str) {
        if (str === null || str === undefined) return "";
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#39;");
    };
}
if (typeof window.formatMoney !== "function") {
    window.formatMoney = function (value) {
        const num = Number(value);
        return "₹" + (isNaN(num) ? "0.00" : num.toFixed(2));
    };
}

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
        container.innerHTML = orders.map(o => renderOrder(o, me)).join("");
        wireButtons();
    } catch (err) {
        container.innerHTML = "<p>Could not load orders: " + window.escapeHtml(err.message) + "</p>";
    }
}

function renderOrder(o, me) {
    const items = o.items.map(i =>
        `<li>${window.escapeHtml(i.productName)} &times; ${i.quantity} (${window.formatMoney(i.unitPrice)} each)</li>`
    ).join("");
    const nextStatus = STATUS_FLOW[o.status];
    const canAdvance = (me.role === "SELLER" || me.role === "ADMIN") && nextStatus;

    // Determine if the current user can cancel this order
    const isOwner = me.role === "BUYER" && o.buyerId === me.id;
    const isAdmin = me.role === "ADMIN";
    const canCancel = (isOwner || isAdmin) && (o.status === "PENDING" || o.status === "CONFIRMED");

    return `
        <div class="order-card">
            <strong>Order #${o.id}</strong>
            <span class="status-badge">${window.escapeHtml(o.status)}</span>
            <div>Total: ${window.formatMoney(o.totalAmount)}</div>
            ${o.shippingAddress ? `<div>Ship to: ${window.escapeHtml(o.shippingAddress)}</div>` : ""}
            <ul>${items}</ul>
            <div class="order-actions" style="margin-top: 10px; display: flex; gap: 10px;">
                ${canAdvance
                ? `<button class="advanceBtn btn btn-primary" data-order-id="${o.id}" data-next="${nextStatus}">Mark as ${nextStatus}</button>`
                : ""}
                ${canCancel
                ? `<button class="cancelBtn btn btn-secondary" data-order-id="${o.id}">Cancel Order</button>`
                : ""}
            </div>
        </div>
    `;
}

function wireButtons() {
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

    document.querySelectorAll(".cancelBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const orderId = e.target.dataset.orderId;
            if (confirm("Are you sure you want to cancel this order? Stock will be restored automatically.")) {
                try {
                    await api.del("/orders/" + orderId);
                    loadOrders();
                } catch (err) {
                    alert(err.message);
                }
            }
        });
    });
}

loadOrders();