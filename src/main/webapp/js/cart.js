async function loadCart() {
    const container = document.getElementById("cartItems");
    try {
        const data = await api.get("/cart");
        document.getElementById("cartTotal").textContent = formatMoney(data.total);
        if (!data.items || data.items.length === 0) {
            container.innerHTML = "<p style='color: var(--muted);'>Your cart is empty.</p>";
            return;
        }
        container.innerHTML = data.items.map(item => `
            <div class="cart-item" data-product-id="${item.productId}">
                <strong>${escapeHtml(item.productName)}</strong>
                &mdash; ${formatMoney(item.unitPrice)} each
                <div style="margin: 8px 0;">
                    <label>Qty
                        <input type="number" min="1" value="${item.quantity}" class="qtyInput" style="width:70px;">
                    </label>
                    <button class="updateQtyBtn">Update</button>
                    <button class="removeBtn secondary">Remove</button>
                </div>
                <div>Line total: ${formatMoney(item.unitPrice * item.quantity)}</div>
            </div>
        `).join("");
        wireItemButtons();
    } catch (err) {
        container.innerHTML = "<p class='form-error'>Could not load cart: " + escapeHtml(err.message) + "</p>";
    }
}

async function loadRecommendations() {
    const recContainer = document.getElementById("recommendations");
    if (!recContainer) return;

    try {
        const res = await api.get("/products?page=1&pageSize=3");
        const rawProducts = res.items || res.data || (Array.isArray(res) ? res : []);
        const products = rawProducts.slice(0, 3);

        if (!products || products.length === 0) {
            recContainer.innerHTML = "<p style='color: var(--muted);'>No recommendations available.</p>";
            return;
        }

        recContainer.innerHTML = products.map(prod => `
            <div class="rec-card" style="border: 1px solid var(--border); border-radius: 6px; padding: 0.75rem; margin-bottom: 1rem; background: var(--card-bg); color: var(--text);">
                <img src="${escapeHtml(prod.imageUrl)}" alt="${escapeHtml(prod.name)}" style="width: 100%; height: 120px; object-fit: cover; border-radius: 4px; background: #eee; margin-bottom: 0.5rem;">
                <h4 style="margin: 0.25rem 0; font-size: 0.95rem; color: var(--text);">${escapeHtml(prod.name)}</h4>
                <div style="font-weight: 700; color: var(--primary); margin-bottom: 0.5rem;">${formatMoney(prod.price)}</div>
                <button class="addRecBtn" data-id="${prod.id}" style="width: 100%; padding: 0.4rem; font-size: 0.85rem;">Add to Cart</button>
            </div>
        `).join("");

        recContainer.querySelectorAll(".addRecBtn").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                const id = e.target.dataset.id;
                try {
                    await api.post("/cart", { productId: parseInt(id, 10), quantity: 1 });
                    loadCart();
                } catch (err) {
                    alert("Could not add item: " + err.message);
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
            try {
                await api.put("/cart/" + productId, { quantity: qty });
                loadCart();
            } catch (err) {
                document.getElementById("checkoutMessage").textContent = err.message;
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
                document.getElementById("checkoutMessage").textContent = err.message;
            }
        });
    });
}

document.getElementById("checkoutBtn").addEventListener("click", async () => {
    const msg = document.getElementById("checkoutMessage");
    msg.textContent = "";
    const shippingAddress = document.getElementById("shippingAddress").value.trim();
    if (!shippingAddress) {
        msg.textContent = "Please enter a delivery address.";
        return;
    }
    try {
        const order = await api.post("/orders/checkout", { shippingAddress });
        msg.textContent = order.message || ("Order #" + order.orderId + " placed successfully!");
        loadCart();
    } catch (err) {
        msg.textContent = err.message;
    }
});

loadCart();
loadRecommendations();
