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
        return "\u20B9" + (isNaN(num) ? "0.00" : num.toFixed(2));
    };
}

const STATUS_FLOW = { PENDING: "CONFIRMED", CONFIRMED: "SHIPPED", SHIPPED: "DELIVERED" };
let pollTimer = null;
let animationFrameId = null;
let activeOrdersData = [];

async function loadOrders() {
    const container = document.getElementById("ordersList");
    try {
        const me = await api.get("/auth/me");
        const orders = await api.get("/orders");
        
        if (!orders || orders.length === 0) {
            container.innerHTML = "<p>No orders yet.</p>";
            if (pollTimer) clearInterval(pollTimer);
            if (animationFrameId) cancelAnimationFrame(animationFrameId);
            return;
        }

        activeOrdersData = orders;
        container.innerHTML = orders.map(o => renderOrder(o, me)).join("");
        wireButtons();
        startSmoothPipelineLoop();

        const hasActiveDeliveries = orders.some(o => o.status === "PENDING" || o.status === "CONFIRMED" || o.status === "SHIPPED");
        
        if (hasActiveDeliveries && !pollTimer) {
            pollTimer = setInterval(loadOrders, 3000);
        } else if (!hasActiveDeliveries && pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    } catch (err) {
        container.innerHTML = "<p>Could not load orders: " + window.escapeHtml(err.message) + "</p>";
        if (pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }
}

function startSmoothPipelineLoop() {
    if (animationFrameId) cancelAnimationFrame(animationFrameId);

    function tick() {
        const now = Date.now();
        let needsNextFrame = false;

        activeOrdersData.forEach(order => {
            if (order.status === "CANCELLED") return;

            const fillEl = document.getElementById(`pipeline-fill-${order.id}`);
            const node2 = document.getElementById(`node-2-${order.id}`);
            const node3 = document.getElementById(`node-3-${order.id}`);
            const step2 = document.getElementById(`step-2-${order.id}`);
            const step3 = document.getElementById(`step-3-${order.id}`);

            if (!fillEl) return;

            if (order.status === "DELIVERED") {
                fillEl.style.width = "100%";
                if (step2) step2.classList.add("active");
                if (step3) step3.classList.add("active");
                if (node2) node2.innerHTML = "&#10003;";
                if (node3) node3.innerHTML = "&#10003;";
                return;
            }

            const createdTime = order.createdAt ? new Date(order.createdAt).getTime() : now;
            const elapsedSec = Math.max(0, (now - createdTime) / 1000);

            let progressRatio = 0;
            if (elapsedSec <= 10) {
                progressRatio = (elapsedSec / 10) * 0.5;
            } else if (elapsedSec < 25) {
                progressRatio = 0.5 + ((elapsedSec - 10) / 15) * 0.5;
            } else {
                progressRatio = 1.0;
            }

            const currentPercent = Math.min(100, Math.max(0, progressRatio * 100));
            fillEl.style.width = currentPercent.toFixed(2) + "%";

            if (currentPercent >= 50) {
                if (step2) step2.classList.add("active");
                if (node2) node2.innerHTML = "&#10003;";
            }
            if (currentPercent >= 99) {
                if (step3) step3.classList.add("active");
                if (node3) node3.innerHTML = "&#10003;";
            }

            if (currentPercent < 100) {
                needsNextFrame = true;
            }
        });

        if (needsNextFrame) {
            animationFrameId = requestAnimationFrame(tick);
        }
    }

    animationFrameId = requestAnimationFrame(tick);
}

function renderPipeline(order) {
    if (order.status === "CANCELLED") {
        return `
            <div style="margin: 0.75rem 0;">
                <span class="status-badge" style="background: var(--error); color: white;">CANCELLED</span>
            </div>
        `;
    }

    const isDelivered = order.status === "DELIVERED";
    const isShipped = order.status === "SHIPPED" || isDelivered;

    return `
        <div class="delivery-pipeline">
            <div class="pipeline-track">
                <div class="pipeline-track-fill" id="pipeline-fill-${order.id}" style="width: ${isDelivered ? '100%' : '0%'};"></div>
                <div class="pipeline-step active" id="step-1-${order.id}">
                    <div class="step-node">&#10003;</div>
                    <span class="step-label">Confirmed</span>
                </div>
                <div class="pipeline-step ${isShipped ? 'active' : ''}" id="step-2-${order.id}">
                    <div class="step-node" id="node-2-${order.id}">${isShipped ? '&#10003;' : '2'}</div>
                    <span class="step-label">Shipped</span>
                </div>
                <div class="pipeline-step ${isDelivered ? 'active' : ''}" id="step-3-${order.id}">
                    <div class="step-node" id="node-3-${order.id}">${isDelivered ? '&#10003;' : '3'}</div>
                    <span class="step-label">Delivered</span>
                </div>
            </div>
        </div>
    `;
}

function renderOrder(o, me) {
    const items = (o.items || []).map(i =>
        `<li>${window.escapeHtml(i.productName)} &times; ${i.quantity} (${window.formatMoney(i.unitPrice)} each)</li>`
    ).join("");
    
    const nextStatus = STATUS_FLOW[o.status];
    const canAdvance = (me.role === "SELLER" || me.role === "ADMIN") && nextStatus;
    const isOwner = me.role === "BUYER" && o.buyerId === me.id;
    const isAdmin = me.role === "ADMIN";
    const canCancel = (isOwner || isAdmin) && (o.status === "PENDING" || o.status === "CONFIRMED");

    return `
        <div class="order-card" id="order-${o.id}">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem;">
                <strong>Order #${o.id}</strong>
                <span class="status-badge">${window.escapeHtml(o.status)}</span>
            </div>

            ${renderPipeline(o)}

            <div style="margin: 0.4rem 0;"><strong>Total:</strong> ${window.formatMoney(o.totalAmount)}</div>
            ${o.shippingAddress ? `<div style="font-size: 0.85rem; color: var(--muted); margin-bottom: 0.4rem;"><strong>Ship to:</strong> ${window.escapeHtml(o.shippingAddress)}</div>` : ""}
            <ul style="margin: 0.5rem 0; padding-left: 1.2rem; font-size: 0.9rem;">${items}</ul>
            
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