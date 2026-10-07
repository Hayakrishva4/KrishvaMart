window.escapeHtml ??= (str)=> !str ? "" : String(str).replace(/[&<>"']/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[m]));
window.formatMoney ??= (v)=> "\u20B9" + (isNaN(Number(v)) ? "0.00" : Number(v).toFixed(2));
async function loadWishlist() {
    const container=document.getElementById("wishlistContainer") || document.getElementById("wishlistItems");
    if (!container) return [];
    try {
        const res=await window.api.get("/wishlist");
        const items=res?.data || (Array.isArray(res) ? res : (res?.items || []));
        if (!items.length) {
            container.innerHTML="<p style='color:var(--muted);padding:1rem 0;'>Your wishlist is empty. Save products you like from their product page.</p>";
            return [];
        }
        container.innerHTML=items.map(item => `
            <div class="cart-item" data-product-id="${item.productId}" style="display:flex;justify-content:space-between;align-items:center;padding:1rem;border:1px solid var(--border);border-radius:8px;margin-bottom:0.85rem;background:var(--card-bg);">
                <div>
                    <a href="product-detail.jsp?id=${item.productId}" style="font-weight:600;font-size:1rem;color:var(--text);text-decoration:none;">
                        ${window.escapeHtml(item.productName)}
                    </a>
                    <div style="margin-top:0.25rem;font-size:0.9rem;color:var(--muted);">
                        <strong style="color:var(--primary);">${window.formatMoney(item.productPrice)}</strong> &bull; 
                        <span>${item.productStockQty > 0 ? `${item.productStockQty} in stock` : "Out of stock"}</span>
                    </div>
                </div>
                <div style="display:flex;gap:0.5rem;">
                    <button class="moveToCartBtn btn btn-primary" ${item.productStockQty > 0 ? "" : "disabled"} style="padding:0.4rem 0.85rem;font-size:0.85rem;">Move to Cart</button>
                    <button class="removeWishlistBtn btn secondary" style="padding:0.4rem 0.75rem;font-size:0.85rem;color:var(--error);border-color:var(--border);background:transparent;">Remove</button>
                </div>
            </div>`).join("");
        wireButtons();
        return items;
    } catch (err) {
        container.innerHTML=`<p class="form-error" style="color:var(--error);">Could not load wishlist: ${window.escapeHtml(err.message)}</p>`;
        return [];
    }
}
function wireButtons() {
    document.querySelectorAll(".moveToCartBtn").forEach(btn=> {
        btn.addEventListener("click", async (e)=> {
            const pid=e.target.closest(".cart-item")?.dataset.productId;
            try {
                await window.api.post("/cart", { productId:parseInt(pid, 10), quantity:1 });
                await window.api.del("/wishlist/" +pid);
                refreshAll();
            } catch (err) {
                alert(err.message);
            }
        });
    });
    document.querySelectorAll(".removeWishlistBtn").forEach(btn=> {
        btn.addEventListener("click", async (e)=> {
            const pid=e.target.closest(".cart-item")?.dataset.productId;
            try {
                await window.api.del("/wishlist/" +pid);
                refreshAll();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}
async function loadSuggestedProducts(existingWishlist=[]) {
    const container=document.getElementById("suggestedContainer");
    if (!container) return;
    try {
        const res=await window.api.get("/products?pageSize=12");
        const list=res?.data || (Array.isArray(res) ? res : (res?.items || []));
        const wishlisted=new Set(existingWishlist.map(w => Number(w.productId)));
        const suggestions=list.filter(p => !wishlisted.has(Number(p.id))).slice(0, 3);
        if (!suggestions.length) {
            container.innerHTML="<p style='color:var(--muted);'>No suggestions available.</p>";
            return;
        }
        container.className="product-grid";
        container.innerHTML=suggestions.map(p => `
            <div class="product-card in-view">
                ${p.imageUrl ? `<img src="${window.escapeHtml(p.imageUrl)}" alt="${window.escapeHtml(p.name)}">` : ""}
                <span class="product-id" style="font-size:0.8rem;opacity:0.75;">ID: #${p.id}</span>
                <a href="product-detail.jsp?id=${p.id}" style="text-decoration:none;color:inherit;">
                    <strong>${window.escapeHtml(p.name)}</strong>
                </a>
                <span class="category">${window.escapeHtml(p.category || "General")}</span>
                <span class="price">${window.formatMoney(p.price)}</span>
                <span style="font-size:0.85rem;color:var(--muted);">${p.stockQty > 0 ? `${p.stockQty} in stock` : "Out of stock"}</span>
                <button class="addSuggestedBtn btn btn-primary" data-product-id="${p.id}" style="margin-top:0.75rem;">
                    &#10084; Add to Wishlist
                </button>
            </div>`).join("");
        container.querySelectorAll(".addSuggestedBtn").forEach(btn => {
            btn.addEventListener("click", async (e) => {
                try {
                    await window.api.post("/wishlist", { productId: parseInt(e.currentTarget.dataset.productId, 10) });
                    refreshAll();
                } catch (err) {
                    alert(err.message);
                }
            });
        });
    } catch (err) {
        container.innerHTML=`<p style="color:var(--muted);font-size:0.85rem;">Could not load suggestions: ${window.escapeHtml(err.message)}</p>`;
    }
}
async function refreshAll() {
    const items=await loadWishlist();
    await loadSuggestedProducts(items);
}
refreshAll();