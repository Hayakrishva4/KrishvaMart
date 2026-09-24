window.escapeHtml ??= (str) => !str ? "" : String(str).replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m]));
window.formatMoney ??= (v) => "\u20B9" + (isNaN(Number(v)) ? "0.00" : Number(v).toFixed(2));

const urlParams = new URLSearchParams(window.location.search), buyNowId = urlParams.get('buyNow');
let isOrderPlaced = false;

function setupNavigationInterceptor() {
    if (!document.getElementById("directCheckoutModal")) {
        document.body.insertAdjacentHTML("beforeend", `
            <div id="directCheckoutModal" style="display:none;position:fixed;inset:0;
            background:rgba(0,0,0,.65);z-index:9999;justify-content:center;align-items:center;">
                <div style="background:var(--card-bg,#1e1e2f);border:1px solid var(--border,#333);padding:1.25rem;
                border-radius:8px;max-width:280px;width:90%;text-align:center;box-shadow:0 8px 24px rgba(0,0,0,.5);">
                    <h3 style="margin:0 0 .5rem;color:var(--text,#fff);font-size:1.05rem;">Unsaved Item</h3>
                    <p style="color:var(--muted,#aaa);font-size:.85rem;margin:0 0 1rem;">Save this item to your cart?</p>
                    <div style="display:flex;justify-content:center;gap:.5rem;">
                        <button id="modalBuyLaterBtn" style="padding:.4rem .8rem;background:var(--primary,#20c997);color:#fff;
                        border:none;border-radius:4px;font-weight:600;cursor:pointer;font-size:.85rem;">Add to Cart</button>
                        <button id="modalDiscardBtn" style="padding:.4rem .8rem;background:transparent;border:1px solid var(--error,#dc3545);
                        color:var(--error,#dc3545);border-radius:4px;font-weight:600;cursor:pointer;font-size:.85rem;">Remove</button>
                    </div>
                </div>
            </div>`);
    }
    document.addEventListener("click", e => {
        if (!buyNowId || isOrderPlaced) return;
        const href = e.target.closest("a")?.getAttribute("href");
        if (href && !href.startsWith("#") && !href.startsWith("javascript:")) {
            e.preventDefault();
            const m = document.getElementById("directCheckoutModal"),
             b = document.getElementById("modalBuyLaterBtn"), d = document.getElementById("modalDiscardBtn");
            m.style.display = "flex";
            b.onclick = async () => {
                b.disabled = true; b.textContent = "Saving...";
                if (window.directCheckoutItem) await api.post("/cart", { productId: window.directCheckoutItem.id, quantity: window.directCheckoutItem.quantity || 1 }).catch(console.error);
                isOrderPlaced = true; window.location.href = href;
            };
            d.onclick = () => { isOrderPlaced = true; window.location.href = href; };
        }
    });
    window.addEventListener("beforeunload", e => { if (buyNowId && !isOrderPlaced && window.directCheckoutItem) e.preventDefault(), e.returnValue = ""; });
}

async function loadCart() {
    const container = document.getElementById("cartItems"), totalEl = document.getElementById("cartTotal");
    if (!container) return;

    if (buyNowId) {
        setupNavigationInterceptor();
        const header = document.querySelector(".cart-page h1");
        if (header) header.textContent = "Direct Checkout";
        try {
            const prod = (await api.get("/products/" + buyNowId))?.data || await api.get("/products/" + buyNowId);
            if (totalEl) totalEl.textContent = formatMoney(prod.price);
            window.directCheckoutItem = { id: prod.id, price: prod.price, quantity: 1 };
            container.innerHTML = `
                <div class="cart-item" style="display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:1rem;border:2px solid var(--primary);border-radius:8px;margin-bottom:.85rem;background:var(--card-bg);">
                    <div style="flex:1;min-width:0;"><strong style="font-size:1rem;color:var(--text);display:block;margin-bottom:.25rem;">${escapeHtml(prod.name)}</strong><span style="color:var(--primary);font-size:.85rem;font-weight:bold;background:rgba(32,201,151,.1);padding:.2rem .5rem;border-radius:4px;">Fast Track Checkout</span></div>
                    <div style="display:flex;align-items:center;gap:.5rem;"><span style="font-size:.9rem;font-weight:600;">Qty:</span><input type="number" id="directQtyInput" min="1" max="${prod.stockQty}" value="1" class="qtyInput" style="width:55px;padding:.35rem .45rem;border:1px solid var(--border);border-radius:6px;text-align:center;background:var(--card-bg);color:var(--text);"></div>
                    <div id="directItemTotal" style="font-weight:700;font-size:1.05rem;color:var(--primary);text-align:right;min-width:80px;">${formatMoney(prod.price)}</div>
                </div>`;
            document.getElementById("directQtyInput")?.addEventListener("change", e => {
                let q = parseInt(e.target.value, 10);
                if (isNaN(q) || q < 1) q = 1;
                if (q > prod.stockQty) { alert(`Only ${prod.stockQty} items available!`); q = prod.stockQty; }
                e.target.value = window.directCheckoutItem.quantity = q;
                const tot = formatMoney(prod.price * q);
                document.getElementById("directItemTotal").textContent = tot;
                if (totalEl) totalEl.textContent = tot;
            });
        } catch (err) { container.innerHTML = `<p style='color:var(--error);'>Checkout error: ${escapeHtml(err.message)}</p>`; }
        return;
    }

    try {
        const data = (await api.get("/cart"))?.data || await api.get("/cart");
        const items = data.items || [], total = data.total ?? data.totalAmount ?? 0;
        if (totalEl) totalEl.textContent = formatMoney(total);
        
        if (!items.length) {
            container.innerHTML = `
                <style>@keyframes floatCart{0%,100%{transform:translateY(0)}50%{transform:translateY(-10px)}}</style>
                <div style="text-align:center;padding:3.5rem 1rem;display:flex;flex-direction:column;align-items:center">
                    <svg viewBox="0 0 24 24" style="width:72px;height:72px;fill:none;stroke:var(--primary,#20c997);stroke-width:1.5;stroke-linecap:round;stroke-linejoin:round;animation:floatCart 3s ease-in-out infinite;filter:drop-shadow(0 4px 6px rgba(32,201,151,0.2))">
                        <circle cx="9" cy="21" r="1"></circle><circle cx="20" cy="21" r="1"></circle><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path><path d="M15 11a3 3 0 0 1-6 0" stroke="var(--muted,#888)" style="opacity:0.4"></path>
                    </svg>
                    <p style="color:var(--muted);margin:1.5rem 0 1.25rem;font-size:1.05rem;">Your cart is currently empty.</p>
                    <a href="index.jsp" style="padding:.55rem 1.4rem;background:var(--primary);color:#fff;border-radius:6px;text-decoration:none;font-weight:600;">Browse Products</a>
                </div>`;
            return;
        }

        container.innerHTML = items.map(i => `
            <div class="cart-item" data-product-id="${i.productId}" style="display:flex;align-items:center;justify-content:space-between;gap:1rem;padding:1rem;border:1px solid var(--border);border-radius:8px;margin-bottom:.85rem;background:var(--card-bg);">
                <div style="flex:1;min-width:0;"><a href="product-detail.jsp?id=${i.productId}" style="font-weight:600;font-size:.95rem;color:var(--text);text-decoration:none;display:block;margin-bottom:.25rem;">${escapeHtml(i.productName)}</a><span style="color:var(--muted);font-size:.85rem;">${formatMoney(i.unitPrice)} each</span></div>
                <div style="display:flex;align-items:center;gap:.5rem;"><input type="number" min="1" max="99" value="${i.quantity}" class="qtyInput" style="width:55px;padding:.35rem .45rem;border:1px solid var(--border);border-radius:6px;text-align:center;background:var(--card-bg);color:var(--text);"><button class="updateQtyBtn" style="padding:.35rem .75rem;font-size:.8rem;background:transparent;border:1px solid var(--border);border-radius:6px;color:var(--text);cursor:pointer;">Update</button><button class="removeBtn secondary" style="padding:.35rem .65rem;font-size:.8rem;background:transparent;border:1px solid var(--border);border-radius:6px;color:var(--error);cursor:pointer;">&#10005;</button></div>
                <div style="font-weight:600;font-size:.95rem;color:var(--primary);min-width:80px;text-align:right;">${formatMoney(i.unitPrice * i.quantity)}</div>
            </div>`).join("");
        
        document.querySelectorAll(".updateQtyBtn").forEach(b => b.addEventListener("click", async e => {
            const r = e.target.closest(".cart-item"), pid = r.dataset.productId, q = parseInt(r.querySelector(".qtyInput").value, 10);
            if (isNaN(q) || q < 1) return alert("Quantity must be at least 1");
            try { await api.put("/cart/" + pid, { quantity: q }); loadCart(); } catch (err) { document.getElementById("checkoutMessage").textContent = err.message; }
        }));
        document.querySelectorAll(".removeBtn").forEach(b => b.addEventListener("click", async e => {
            try { await api.del("/cart/" + e.target.closest(".cart-item").dataset.productId); loadCart(); } catch (err) { document.getElementById("checkoutMessage").textContent = err.message; }
        }));
    } catch (err) { container.innerHTML = `<p style='color:var(--error);'>Could not load cart: ${escapeHtml(err.message)}</p>`; }
}

async function loadRecommendations() {
    const rc = document.getElementById("recommendations");
    if (!rc) return;
    try {
        const raw = ((await api.get("/products?page=1&pageSize=6"))?.data || (await api.get("/products?page=1&pageSize=6"))), prods = (Array.isArray(raw) ? raw : (raw.items || [])).slice(0, 3);
        if (!prods.length) { rc.innerHTML = "<p style='color:var(--muted);font-size:.85rem;'>No recommendations available.</p>"; return; }
        rc.innerHTML = `<div style="display:flex;flex-direction:column;gap:.75rem;">` + prods.map(p => `
            <div class="mini-rec-card" style="display:flex;align-items:center;gap:.85rem;padding:.65rem;border:1px solid var(--border);border-radius:8px;background:var(--card-bg);">
                <img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}" style="width:72px;height:72px;object-fit:cover;border-radius:6px;background:#eee;flex-shrink:0;">
                <div style="flex:1;min-width:0;"><a href="product-detail.jsp?id=${p.id}" style="display:block;font-weight:600;font-size:.875rem;color:var(--text);text-decoration:none;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;margin-bottom:.2rem;">${escapeHtml(p.name)}</a><div style="font-weight:700;color:var(--primary);font-size:.875rem;margin-bottom:.4rem;">${formatMoney(p.price)}</div><button class="addRecBtn" data-id="${p.id}" style="padding:.3rem .65rem;font-size:.775rem;background:var(--primary);color:#fff;border:none;border-radius:4px;cursor:pointer;font-weight:500;">+ Add to Cart</button></div>
            </div>`).join("") + `</div>`;
        rc.querySelectorAll(".addRecBtn").forEach(b => b.addEventListener("click", async e => {
            const t = e.currentTarget; t.textContent = "Adding..."; t.disabled = true;
            try { await api.post("/cart", { productId: parseInt(t.dataset.id, 10), quantity: 1 }); if (!buyNowId) await loadCart(); t.textContent = "Added!"; setTimeout(() => { t.textContent = "+ Add to Cart"; t.disabled = false; }, 1000); }
            catch (err) { alert("Error: " + err.message); t.textContent = "+ Add to Cart"; t.disabled = false; }
        }));
    } catch (err) { rc.innerHTML = "<p style='color:var(--muted);font-size:.85rem;'>Recommendations unavailable</p>"; }
}

document.getElementById("checkoutBtn")?.addEventListener("click", async e => {
    const btn = e.currentTarget, msg = document.getElementById("checkoutMessage"), addr = document.getElementById("shippingAddress");
    if (msg) msg.textContent = "";
    if (!addr?.value.trim()) { if(msg) { msg.style.color="var(--error)"; msg.textContent="Please enter a delivery address."; } addr?.focus(); return; }
    try {
        btn.disabled = true; btn.textContent = "Processing...";
        const payload = { mockPaymentConfirmed: true, shippingAddress: addr.value.trim(), ...(window.directCheckoutItem ? { directProductId: window.directCheckoutItem.id, directQuantity: window.directCheckoutItem.quantity } : {}) };
        const order = await api.post("/orders/checkout", payload);
        isOrderPlaced = true;
        if (msg) { msg.style.color="var(--primary)"; msg.textContent = order.message || "Order placed successfully!"; }
        if (addr) addr.value = "";
        setTimeout(() => window.location.href = "orders.jsp", 1000);
    } catch (err) { if (msg) { msg.style.color="var(--error)"; msg.textContent = err.message; } btn.disabled = false; btn.textContent = "Place Order"; }
});

loadCart(); loadRecommendations();