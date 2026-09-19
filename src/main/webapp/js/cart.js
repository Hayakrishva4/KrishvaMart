if (typeof window.escapeHtml !== "function") {
    window.escapeHtml = function (str) {
        if (!str) return "";
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(str).replace(/[&<>"']/g, m => map[m]);
    };
}

if (typeof window.formatMoney !== "function") {
    window.formatMoney = function (v) {
        const num = Number(v);
        return "\u20B9" + (isNaN(num) ? "0.00" : num.toFixed(2));
    };
}

async function loadCart() {
    const container = document.getElementById("cartItems");
    const totalEl = document.getElementById("cartTotal");
    if (!container) return;

    try {
        const res = await api.get("/cart");
        const data = (res && res.data) ? res.data : res;
        const items = data.items || [];
        const total = data.total !== undefined ? data.total : (data.totalAmount !== undefined ? data.totalAmount : 0);

        if (totalEl) {
            totalEl.textContent = formatMoney(total);
        }

        if (!items || items.length === 0) {
            container.innerHTML = `
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--muted);">
                    <p style="margin-bottom: 1rem; font-size: 1.05rem;">Your cart is currently empty.</p>
                    <a href="index.jsp" class="btn btn-primary" style="text-decoration: none; display: inline-block;">Browse Products</a>
                </div>
            `;
            if (totalEl) {
                totalEl.textContent = formatMoney(0);
            }
            return;
        }

        container.innerHTML = items.map(item => `
            <div class="cart-item" data-product-id="${item.productId}" style="display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem; border: 1px solid var(--border); border-radius: 8px; margin-bottom: 0.85rem; background: var(--card-bg);">
                <div style="flex: 1; min-width: 0;">
                    <a href="product-detail.jsp?id=${item.productId}" style="font-weight: 600; font-size: 0.95rem; color: var(--text); text-decoration: none; display: block; margin-bottom: 0.25rem;">
                        ${escapeHtml(item.productName)}
                    </a>
                    <span style="color: var(--muted); font-size: 0.85rem;">
                        ${formatMoney(item.unitPrice)} each
                    </span>
                </div>

                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <input type="number" min="1" max="99" value="${item.quantity}" class="qtyInput" style="width: 55px; padding: 0.35rem 0.45rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.9rem; text-align: center; background: var(--card-bg); color: var(--text);">
                    <button class="updateQtyBtn" style="padding: 0.35rem 0.75rem; font-size: 0.8rem; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--text); cursor: pointer;">Update</button>
                    <button class="removeBtn secondary" style="padding: 0.35rem 0.65rem; font-size: 0.8rem; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--error); cursor: pointer;">&#10005;</button>
                </div>

                <div style="font-weight: 600; font-size: 0.95rem; color: var(--primary); min-width: 80px; text-align: right;">
                    ${formatMoney(item.unitPrice * item.quantity)}
                </div>
            </div>
        `).join("");

        wireItemButtons();
    } catch (err) {
        container.innerHTML = "<p class='form-error' style='color: var(--error);'>Could not load cart: " + escapeHtml(err.message) + "</p>";
    }
}

async function loadRecommendations() {
    const recContainer = document.getElementById("recommendations");
    if (!recContainer) return;

    try {
        const res = await api.get("/products?page=1&pageSize=6");
        const payload = (res && res.data) ? res.data : res;
        const rawProducts = Array.isArray(payload) ? payload : (payload.items || []);
        
        const products = rawProducts.slice(0, 3);

        if (!products || products.length === 0) {
            recContainer.innerHTML = "<p style='color: var(--muted); font-size: 0.85rem;'>No recommendations available.</p>";
            return;
        }

        recContainer.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                ${products.map(prod => {
                    const imgUrl = prod.imageUrl ? prod.imageUrl : "";
                    const productName = prod.name ? prod.name : "";
                    return `
                        <div class="mini-rec-card" style="display: flex; align-items: center; gap: 0.85rem; padding: 0.65rem; border: 1px solid var(--border); border-radius: 8px; background: var(--card-bg);">
                            <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(productName)}" style="width: 72px; height: 72px; object-fit: cover; border-radius: 6px; background: #eee; flex-shrink: 0;">
                            
                            <div style="flex: 1; min-width: 0;">
                                <a href="product-detail.jsp?id=${prod.id}" style="display: block; font-weight: 600; font-size: 0.875rem; color: var(--text); text-decoration: none; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 0.2rem;">
                                    ${escapeHtml(productName)}
                                </a>
                                <div style="font-weight: 700; color: var(--primary); font-size: 0.875rem; margin-bottom: 0.4rem;">
                                    ${formatMoney(prod.price)}
                                </div>
                                <button class="addRecBtn" data-id="${prod.id}" style="padding: 0.3rem 0.65rem; font-size: 0.775rem; background: var(--primary); color: #fff; border: none; border-radius: 4px; cursor: pointer; font-weight: 500;">
                                    + Add to Cart
                                </button>
                            </div>
                        </div>
                    `;
                }).join("")}
            </div>
        `;

        recContainer.querySelectorAll(".addRecBtn").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                const targetBtn = e.currentTarget;
                const id = targetBtn.dataset.id;
                const originalText = "+ Add to Cart";
                
                targetBtn.textContent = "Adding...";
                targetBtn.disabled = true;

                try {
                    await api.post("/cart", { productId: parseInt(id, 10), quantity: 1 });
                    await loadCart();
                    targetBtn.textContent = "Added!";
                    setTimeout(() => {
                        targetBtn.textContent = originalText;
                        targetBtn.disabled = false;
                    }, 1000);
                } catch (err) {
                    alert("Could not add item: " + (err.message || err));
                    targetBtn.textContent = originalText;
                    targetBtn.disabled = false;
                }
            });
        });
    } catch (err) {
        recContainer.innerHTML = "<p style='color: var(--muted); font-size: 0.85rem;'>Recommendations unavailable</p>";
    }
}

function wireItemButtons() {
    document.querySelectorAll(".updateQtyBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest(".cart-item");
            const productId = row.dataset.productId;
            const qty = parseInt(row.querySelector(".qtyInput").value, 10);

            if (isNaN(qty) || qty < 1) {
                alert("Quantity must be at least 1");
                return;
            }

            try {
                await api.put("/cart/" + productId, { quantity: qty });
                loadCart();
            } catch (err) {
                const msg = document.getElementById("checkoutMessage");
                if (msg) msg.textContent = err.message;
            }
        });
    });

    document.querySelectorAll(".removeBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest(".cart-item");
            const productId = row.dataset.productId;
            try {
                await api.del("/cart/" + productId);
                loadCart();
            } catch (err) {
                const msg = document.getElementById("checkoutMessage");
                if (msg) msg.textContent = err.message;
            }
        });
    });
}

const checkoutBtn = document.getElementById("checkoutBtn");
if (checkoutBtn) {
    checkoutBtn.addEventListener("click", async () => {
        const msg = document.getElementById("checkoutMessage");
        if (msg) msg.textContent = "";
        
        const addrInput = document.getElementById("shippingAddress");
        const shippingAddress = addrInput ? addrInput.value.trim() : "";
        
        if (!shippingAddress) {
            if (msg) {
                msg.style.color = "var(--error)";
                msg.textContent = "Please enter a delivery address.";
            }
            if (addrInput) addrInput.focus();
            return;
        }

        try {
            checkoutBtn.disabled = true;
            checkoutBtn.textContent = "Processing...";
            const order = await api.post("/orders/checkout", { 
                mockPaymentConfirmed: true, 
                shippingAddress: shippingAddress 
            });
            
            if (msg) {
                msg.style.color = "var(--primary)";
                msg.textContent = order.message || "Order placed successfully!";
            }
            
            if (addrInput) addrInput.value = "";
            await loadCart();
            
            setTimeout(() => {
                window.location.href = "orders.jsp";
            }, 1000);
        } catch (err) {
            if (msg) {
                msg.style.color = "var(--error)";
                msg.textContent = err.message;
            }
            checkoutBtn.disabled = false;
            checkoutBtn.textContent = "Place Order";
        }
    });
}

loadCart();
loadRecommendations();