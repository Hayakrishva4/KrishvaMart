window.escapeHtml ??= (str) => {
    if (str == null) return "";
    return String(str).replace(/[&<>"']/g, m => ({ '&': "&amp;", '<': "&lt;", '>': "&gt;", '"': "&quot;", "'": "&#39;" }[m]));
};
window.formatMoney ??= (value) => "\u20B9" + (isNaN(Number(value)) ? "0.00" : Number(value).toFixed(2));

const STATUS_FLOW = { PENDING: "CONFIRMED", CONFIRMED: "SHIPPED", SHIPPED: "DELIVERED" };
let pollTimer = null, animationFrameId = null, activeOrdersData = [];

function calculateProgress(order) {
    if (!order.status) return 0;
    const st = String(order.status).toUpperCase();
    if (st === "DELIVERED") return 100;
    if (st === "CANCELLED") return 0;
    
    const elapsedSec = Math.max(0, (Date.now() - (order.createdAt ? new Date(order.createdAt).getTime() : Date.now())) / 1000);
    const p = elapsedSec <= 35 ? (elapsedSec / 35) * 0.5 : 0.5 + ((elapsedSec - 35) / 55) * 0.5;
    return Math.min(100, Math.max(0, p * 100));
}

async function loadOrders() {
    const container = document.getElementById("ordersList");
    if (!container) return;
    try {
        const [me, res] = await Promise.all([api.get("/auth/me").catch(() => null), api.get("/orders")]);
        const orders = res?.data || (Array.isArray(res) ? res : []);
        
        if (!orders.length) {
            container.innerHTML = "<p style='color:var(--muted);padding:1.5rem 0;'>No orders placed yet.</p>";
            if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
            if (animationFrameId) cancelAnimationFrame(animationFrameId);
            return;
        }

        activeOrdersData = orders;
        container.innerHTML = orders.map(o => renderOrder(o, me)).join("");
        wireButtons();
        startSmoothPipelineLoop();

        const active = orders.some(o => ["PENDING", "CONFIRMED", "SHIPPED"].includes(String(o.status || "").toUpperCase()));
        if (active && !pollTimer) pollTimer = setInterval(loadOrders, 4000);
        else if (!active && pollTimer) { clearInterval(pollTimer); pollTimer = null; }
    } catch (err) {
        container.innerHTML = `<p style='color:var(--error);'>Could not load orders: ${window.escapeHtml(err.message)}</p>`;
        if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
    }
}

function startSmoothPipelineLoop() {
    if (animationFrameId) cancelAnimationFrame(animationFrameId);
    (function tick() {
        let next = false;
        activeOrdersData.forEach(o => {
            const st = String(o.status || "").toUpperCase();
            if (st === "CANCELLED") return;
            const fill = document.getElementById(`pipeline-fill-${o.id}`);
            const notice = document.getElementById(`delivery-notice-${o.id}`);
            const btn = document.querySelector(`#order-${o.id} .cancelBtn`);
            if (!fill) return;

            const p = st === "DELIVERED" ? 100 : calculateProgress(o);
            fill.style.width = p.toFixed(2) + "%";

            if (p >= 50) document.getElementById(`step-2-${o.id}`)?.classList.add("active");
            if (p >= 50) { let n = document.getElementById(`node-2-${o.id}`); if (n) n.innerHTML = "&#10003;"; }
            if (p >= 99.5) document.getElementById(`step-3-${o.id}`)?.classList.add("active");
            if (p >= 99.5) { let n = document.getElementById(`node-3-${o.id}`); if (n) n.innerHTML = "&#10003;"; }
            if (notice) notice.textContent = p >= 99.5 ? "Package safely delivered to your destination." : (p >= 50 ? "Reached nearby delivery center. Out for doorstep delivery." : "Order packed and dispatched from regional sorting facility.");
            if (btn && p >= 99.5) btn.style.display = "none";
            if (p < 100 && st !== "DELIVERED") next = true;
        });
        if (next) animationFrameId = requestAnimationFrame(tick);
    })();
}

function renderPipeline(o) {
    const st = String(o.status || "").toUpperCase();
    if (st === "CANCELLED") return `<div style="margin:.75rem 0"><span style="background:var(--error);color:#fff;padding:.25rem .6rem;border-radius:4px;font-size:.8rem">CANCELLED</span></div>`;
    const p = calculateProgress(o), s2 = (st === "SHIPPED" || st === "DELIVERED" || p >= 50), s3 = (st === "DELIVERED" || p >= 99);
    return `
        <div class="delivery-pipeline" style="margin:1.25rem 0 1rem"><div class="pipeline-track" style="position:relative;display:flex;justify-content:space-between;align-items:center">
            <div class="pipeline-track-fill" id="pipeline-fill-${o.id}" style="width:${p}%"></div>
            <div class="pipeline-step active" id="step-1-${o.id}"><div class="step-node" style="display:flex;align-items:center;justify-content:center;font-weight:bold">&#10003;</div><span class="step-label">Confirmed</span></div>
            <div class="pipeline-step ${s2 ? 'active' : ''}" id="step-2-${o.id}"><div class="step-node" id="node-2-${o.id}" style="display:flex;align-items:center;justify-content:center;font-weight:bold">${s2 ? '&#10003;' : '2'}</div><span class="step-label">Shipped</span></div>
            <div class="pipeline-step ${s3 ? 'active' : ''}" id="step-3-${o.id}"><div class="step-node" id="node-3-${o.id}" style="display:flex;align-items:center;justify-content:center;font-weight:bold">${s3 ? '&#10003;' : '3'}</div><span class="step-label">Delivered</span></div>
        </div></div>`;
}

function renderOrder(o, me) {
    const items = (o.items || []).map(i => `<li style="margin-bottom:.25rem">${window.escapeHtml(i.productName)} &times; ${i.quantity} <span style="color:var(--muted)">(${window.formatMoney(i.unitPrice)} each)</span></li>`).join("");
    const st = String(o.status || "").toUpperCase(), nx = STATUS_FLOW[st], role = String(me?.role).toUpperCase();
    const canAdv = me && (role === "SELLER" || role === "ADMIN") && nx;
    const canCancel = me && (String(o.buyerId) === String(me.id) || role === "ADMIN") && ["PENDING", "CONFIRMED", "SHIPPED"].includes(st);

    return `
        <div class="order-card" id="order-${o.id}" style="background:var(--card-bg);border:1px solid var(--border);border-radius:12px;padding:1.5rem;margin-bottom:1.5rem;box-shadow:0 2px 8px rgba(0,0,0,.03)">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:.75rem">
                <strong style="font-size:1.15rem;color:var(--text)">Order #${o.id}</strong>
                <span class="status-badge" style="background:var(--primary);color:#fff;padding:.25rem .75rem;border-radius:20px;font-size:.8rem;font-weight:600">${window.escapeHtml(o.status)}</span>
            </div>
            ${renderPipeline(o)}
            <div style="background:rgba(0,0,0,.03);border-left:3px solid var(--primary);padding:.6rem .85rem;border-radius:4px;margin:.75rem 0;font-size:.88rem;color:var(--text)">
                <strong>Status Update: </strong><span id="delivery-notice-${o.id}">${st === "CANCELLED" ? "Order cancelled." : (st === "DELIVERED" ? "Package delivered." : "Preparing order.")}</span>
            </div>
            <div style="margin:.5rem 0;font-size:1.05rem;font-weight:700;color:var(--text)">Total: <span style="color:var(--primary)">${window.formatMoney(o.totalAmount)}</span></div>
            ${o.shippingAddress ? `<div style="font-size:.88rem;color:var(--muted);margin-bottom:.5rem"><strong>Destination:</strong> ${window.escapeHtml(o.shippingAddress)}</div>` : ""}
            <div style="font-size:.9rem;font-weight:600;margin-top:.75rem;color:var(--text)">Items:</div>
            <ul style="margin:.4rem 0 1rem;padding-left:1.25rem;font-size:.9rem;color:var(--text)">${items}</ul>
            <div class="order-actions" style="display:flex;gap:10px;margin-top:1rem">
                ${canAdv ? `<button class="advanceBtn btn btn-primary" data-order-id="${o.id}" data-next="${nx}" style="padding:.5rem 1rem;border-radius:6px;border:none;background:var(--primary);color:#fff;font-weight:500;cursor:pointer">Mark as ${nx}</button>` : ""}
                ${canCancel ? `<button class="cancelBtn btn btn-secondary" data-order-id="${o.id}" style="padding:.5rem 1.2rem;border-radius:6px;border:1px solid #dc3545;background:transparent;color:#dc3545;font-weight:600;cursor:pointer;transition:.2s">Cancel Order</button>` : ""}
            </div>
        </div>`;
}

function wireButtons() {
    document.querySelectorAll(".advanceBtn").forEach(b => b.addEventListener("click", async e => {
        try { await api.patch("/orders/" + e.target.dataset.orderId + "/status", { status: e.target.dataset.next }); loadOrders(); } catch (err) { alert(err.message); }
    }));
    document.querySelectorAll(".cancelBtn").forEach(b => {
        b.addEventListener("mouseenter", e => { e.target.style.background = "#dc3545"; e.target.style.color = "#fff"; });
        b.addEventListener("mouseleave", e => { e.target.style.background = "transparent"; e.target.style.color = "#dc3545"; });
        b.addEventListener("click", async e => {
            if (confirm("Cancel this order? Stock will be restored.")) {
                try { await api.del("/orders/" + e.target.dataset.orderId); loadOrders(); } catch (err) { alert(err.message); }
            }
        });
    });
}
loadOrders();