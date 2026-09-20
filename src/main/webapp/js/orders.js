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

const TIME_TO_SHIPPED_SEC = 35;
const TIME_TO_DELIVERED_SEC = 90;

function calculateProgress(order) {
    if (order.status === "DELIVERED") return 100;
    if (order.status === "CANCELLED") return 0;

    const now = Date.now();
    const createdTime = order.createdAt ? new Date(order.createdAt).getTime() : now;
    const elapsedSec = Math.max(0, (now - createdTime) / 1000);

    let progressRatio = 0;
    if (elapsedSec <= TIME_TO_SHIPPED_SEC) {
        progressRatio = (elapsedSec / TIME_TO_SHIPPED_SEC) * 0.5;
    } else if (elapsedSec < TIME_TO_DELIVERED_SEC) {
        const remaining = elapsedSec - TIME_TO_SHIPPED_SEC;
        const phase2Span = TIME_TO_DELIVERED_SEC - TIME_TO_SHIPPED_SEC;
        progressRatio = 0.5 + (remaining / phase2Span) * 0.5;
    } else {
        progressRatio = 1.0;
    }
    return Math.min(100, Math.max(0, progressRatio * 100));
}

async function loadOrders() {
    const container = document.getElementById("ordersList");
    if (!container) return;

    try {
        const me = await api.get("/auth/me");
        const res = await api.get("/orders");
        const orders = (res && res.data) ? res.data : (Array.isArray(res) ? res : []);
        
        if (!orders || orders.length === 0) {
            container.innerHTML = "<p style='color: var(--muted); padding: 1.5rem 0;'>No orders placed yet.</p>";
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
            pollTimer = setInterval(loadOrders, 4000);
        } else if (!hasActiveDeliveries && pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    } catch (err) {
        container.innerHTML = "<p class='form-error' style='color: var(--error);'>Could not load orders: " + window.escapeHtml(err.message) + "</p>";
        if (pollTimer) {
            clearInterval(pollTimer);
            pollTimer = null;
        }
    }
}

function startSmoothPipelineLoop() {
    if (animationFrameId) cancelAnimationFrame(animationFrameId);

    function tick() {
        let needsNextFrame = false;

        activeOrdersData.forEach(order => {
            if (order.status === "CANCELLED") return;

            const fillEl = document.getElementById(`pipeline-fill-${order.id}`);
            const node2 = document.getElementById(`node-2-${order.id}`);
            const node3 = document.getElementById(`node-3-${order.id}`);
            const step2 = document.getElementById(`step-2-${order.id}`);
            const step3 = document.getElementById(`step-3-${order.id}`);
            const statusNotice = document.getElementById(`delivery-notice-${order.id}`);

            if (!fillEl) return;

            if (order.status === "DELIVERED") {
                fillEl.style.width = "100%";
                if (step2) step2.classList.add("active");
                if (step3) step3.classList.add("active");
                if (node2) node2.innerHTML = "&#10003;";
                if (node3) node3.innerHTML = "&#10003;";
                if (statusNotice) {
                    statusNotice.textContent = "Package safely delivered to your destination.";
                }
                return;
            }

            const currentPercent = calculateProgress(order);
            fillEl.style.width = currentPercent.toFixed(2) + "%";

            if (currentPercent >= 50) {
                if (step2) step2.classList.add("active");
                if (node2) node2.innerHTML = "&#10003;";
                if (statusNotice) {
                    statusNotice.textContent = "Reached nearby delivery center. Out for doorstep delivery.";
                }
            } else {
                if (statusNotice) {
                    statusNotice.textContent = "Order packed and dispatched from regional sorting facility.";
                }
            }

            if (currentPercent >= 99) {
                if (step3) step3.classList.add("active");
                if (node3) node3.innerHTML = "&#10003;";
            }

            if (currentPercent < 100 && order.status !== "DELIVERED") {
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
                <span class="status-badge" style="background: var(--error); color: white; padding: 0.25rem 0.6rem; border-radius: 4px; font-size: 0.8rem;">CANCELLED</span>
            </div>
        `;
    }

    const isDelivered = order.status === "DELIVERED";
    const isShipped = order.status === "SHIPPED" || isDelivered;
    const currentPercent = calculateProgress(order);

    return `
        <div class="delivery-pipeline" style="margin: 1.25rem 0 1rem 0;">
            <div class="pipeline-track" style="position: relative; display: flex; justify-content: space-between; align-items: center;">
                <div class="pipeline-track-fill" id="pipeline-fill-${order.id}" style="width: ${currentPercent}%;"></div>
                
                <div class="pipeline-step active" id="step-1-${order.id}">
                    <div class="step-node" style="display: flex; align-items: center; justify-content: center; font-weight: bold;">&#10003;</div>
                    <span class="step-label">Confirmed</span>
                </div>
                
                <div class="pipeline-step ${isShipped || currentPercent >= 50 ? 'active' : ''}" id="step-2-${order.id}">
                    <div class="step-node" id="node-2-${order.id}" style="display: flex; align-items: center; justify-content: center; font-weight: bold;">
                        ${isShipped || currentPercent >= 50 ? '&#10003;' : '2'}
                    </div>
                    <span class="step-label">Shipped</span>
                </div>
                
                <div class="pipeline-step ${isDelivered || currentPercent >= 99 ? 'active' : ''}" id="step-3-${order.id}">
                    <div class="step-node" id="node-3-${order.id}" style="display: flex; align-items: center; justify-content: center; font-weight: bold;">
                        ${isDelivered || currentPercent >= 99 ? '&#10003;' : '3'}
                    </div>
                    <span class="step-label">Delivered</span>
                </div>
            </div>
        </div>
    `;
}

function getTrackingStatusText(order) {
    if (order.status === "CANCELLED") {
        return "Order has been cancelled.";
    }
    if (order.status === "DELIVERED") {
        return "Package delivered successfully.";
    }
    if (order.status === "SHIPPED") {
        return "Reached nearby delivery center. Out for delivery.";
    }
    return "Order confirmed and being prepared at fulfillment center.";
}

function renderOrder(o, me) {
    const items = (o.items || []).map(i =>
        `<li style="margin-bottom: 0.25rem;">${window.escapeHtml(i.productName)} &times; ${i.quantity} <span style="color: var(--muted);">(${window.formatMoney(i.unitPrice)} each)</span></li>`
    ).join("");
    
    const nextStatus = STATUS_FLOW[o.status];
    const canAdvance = (me && (me.role === "SELLER" || me.role === "ADMIN")) && nextStatus;
    const isOwner = me && me.role === "BUYER" && o.buyerId === me.id;
    const isAdmin = me && me.role === "ADMIN";
    const canCancel = (isOwner || isAdmin) && (o.status === "PENDING" || o.status === "CONFIRMED");

    return `
        <div class="order-card" id="order-${o.id}" style="background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; margin-bottom: 1.5rem; box-shadow: 0 2px 8px rgba(0,0,0,0.03);">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                <strong style="font-size: 1.15rem; color: var(--text);">Order #${o.id}</strong>
                <span class="status-badge" style="background: var(--primary); color: #fff; padding: 0.25rem 0.75rem; border-radius: 20px; font-size: 0.8rem; font-weight: 600;">
                    ${window.escapeHtml(o.status)}
                </span>
            </div>

            ${renderPipeline(o)}

            <!-- Professional Live Tracking Notice -->
            <div style="background: rgba(0, 0, 0, 0.03); border-left: 3px solid var(--primary); padding: 0.6rem 0.85rem; border-radius: 4px; margin: 0.75rem 0; font-size: 0.88rem; color: var(--text);">
                <strong>Status Update: </strong>
                <span id="delivery-notice-${o.id}">${getTrackingStatusText(o)}</span>
            </div>

            <div style="margin: 0.5rem 0; font-size: 1.05rem; font-weight: 700; color: var(--text);">
                Total: <span style="color: var(--primary);">${window.formatMoney(o.totalAmount)}</span>
            </div>

            ${o.shippingAddress ? `
                <div style="font-size: 0.88rem; color: var(--muted); margin-bottom: 0.5rem;">
                    <strong>Destination:</strong> ${window.escapeHtml(o.shippingAddress)}
                </div>
            ` : ""}

            <div style="font-size: 0.9rem; font-weight: 600; margin-top: 0.75rem; color: var(--text);">Items:</div>
            <ul style="margin: 0.4rem 0 1rem 0; padding-left: 1.25rem; font-size: 0.9rem; color: var(--text);">${items}</ul>
            
            <div class="order-actions" style="display: flex; gap: 10px; margin-top: 0.5rem;">
                ${canAdvance
                ? `<button class="advanceBtn btn btn-primary" data-order-id="${o.id}" data-next="${nextStatus}" style="padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; border: none; background: var(--primary); color: #fff; font-weight: 500;">Mark as ${nextStatus}</button>`
                : ""}
                ${canCancel
                ? `<button class="cancelBtn btn btn-secondary" data-order-id="${o.id}" style="padding: 0.5rem 1rem; border-radius: 6px; cursor: pointer; border: 1px solid var(--border); background: transparent; color: var(--error); font-weight: 500;">Cancel Order</button>`
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