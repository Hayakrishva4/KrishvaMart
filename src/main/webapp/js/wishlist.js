if (typeof window.escapeHtml !== "function") {
    window.escapeHtml = function(str) {
        if (!str) return "";
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(str).replace(/[&<>"']/g, m => map[m]);
    };
}

if (typeof window.formatMoney !== "function") {
    window.formatMoney = function(v) {
        const num = Number(v);
        return "\u20B9" + (isNaN(num) ? "0.00" : num.toFixed(2));
    };
}

async function loadWishlist() {
    const container = document.getElementById("wishlistContainer") || document.getElementById("wishlistItems");
    if (!container) return [];
    try {
        const res = await window.api.get("/wishlist");
        const items = Array.isArray(res) ? res : (res && res.data ? res.data : []);

        if (!items || items.length === 0) {
            container.innerHTML = "<p>Your wishlist is empty. Save products you like from their product page.</p>";
            return [];
        }

        container.innerHTML = items.map(item => `
            <div class="cart-item" data-product-id="${item.productId}">
                <a href="product-detail.jsp?id=${item.productId}"><strong>${window.escapeHtml(item.productName)}</strong></a>
                &mdash; ${window.formatMoney(item.productPrice)}
                &mdash; ${item.productStockQty > 0 ? item.productStockQty + " in stock" : "Out of stock"}
                <div style="margin-top: 0.5rem;">
                    <button class="moveToCartBtn btn btn-primary" ${item.productStockQty > 0 ? "" : "disabled"}>Move to cart</button>
                    <button class="removeWishlistBtn btn secondary">Remove</button>
                </div>
            </div>
        `).join("");
        wireButtons();
        return items;
    } catch (err) {
        container.innerHTML = "<p>Could not load wishlist: " + window.escapeHtml(err.message) + "</p>";
        return [];
    }
}

function wireButtons() {
    document.querySelectorAll(".moveToCartBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest(".cart-item");
            const productId = row.dataset.productId;
            try {
                await window.api.post("/cart", { productId: parseInt(productId, 10), quantity: 1 });
                await window.api.del("/wishlist/" + productId);
                await refreshAll();
            } catch (err) {
                alert(err.message);
            }
        });
    });

    document.querySelectorAll(".removeWishlistBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest(".cart-item");
            const productId = row.dataset.productId;
            try {
                await window.api.del("/wishlist/" + productId);
                await refreshAll();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}

async function loadSuggestedProducts(existingWishlist = []) {
    const container = document.getElementById("suggestedContainer");
    if (!container) return;

    try {
        const result = await window.api.get("/products?pageSize=10");
        
        let allProducts = [];
        if (Array.isArray(result)) {
            allProducts = result;
        } else if (result && result.data) {
            allProducts = Array.isArray(result.data) ? result.data : (result.data.items || []);
        } else if (result && result.items) {
            allProducts = result.items;
        }

        const wishlistedIds = new Set(existingWishlist.map(w => Number(w.productId)));

        const suggestions = allProducts
            .filter(p => !wishlistedIds.has(Number(p.id)))
            .slice(0, 3);

        if (suggestions.length === 0) {
            container.innerHTML = "<p>No suggestions available.</p>";
            return;
        }

        container.className = "product-grid";
        container.innerHTML = suggestions.map(p => `
            <div class="product-card in-view">
                ${p.imageUrl ? `<img src="${window.escapeHtml(p.imageUrl)}" alt="${window.escapeHtml(p.name)}">` : ""}
                <span class="product-id" style="font-size:0.8rem; opacity:0.75;">ID: #${p.id}</span>
                <a href="product-detail.jsp?id=${p.id}" style="text-decoration:none; color:inherit;">
                    <strong>${window.escapeHtml(p.name)}</strong>
                </a>
                <span class="category">${window.escapeHtml(p.category || 'General')}</span>
                <span class="price">${window.formatMoney(p.price)}</span>
                <span style="font-size: 0.85rem; color: var(--muted);">${p.stockQty > 0 ? p.stockQty + " in stock" : "Out of stock"}</span>
                <button class="addSuggestedBtn btn btn-primary" data-product-id="${p.id}" style="margin-top: 0.75rem;">
                    &#10084; Add to Wishlist
                </button>
            </div>
        `).join("");

        container.querySelectorAll(".addSuggestedBtn").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                const productId = e.currentTarget.dataset.productId;
                try {
                    await window.api.post("/wishlist", { productId: parseInt(productId, 10) });
                    await refreshAll();
                } catch (err) {
                    alert(err.message);
                }
            });
        });
    } catch (err) {
        container.innerHTML = "<p>Could not load suggestions: " + window.escapeHtml(err.message) + "</p>";
    }
}

async function refreshAll() {
    const wishlistItems = await loadWishlist();
    await loadSuggestedProducts(wishlistItems);
}

refreshAll();