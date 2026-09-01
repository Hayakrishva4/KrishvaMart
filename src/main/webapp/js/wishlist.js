async function loadWishlist() {
    const container = document.getElementById("wishlistItems");
    try {
        const items = await api.get("/wishlist");
        if (items.length === 0) {
            container.innerHTML = "<p>Your wishlist is empty. Save products you like from their product page.</p>";
            return;
        }
        container.innerHTML = items.map(item => `
            <div class="cart-item" data-product-id="${item.productId}">
                <a href="product-detail.jsp?id=${item.productId}"><strong>${escapeHtml(item.productName)}</strong></a>
                &mdash; ${formatMoney(item.productPrice)}
                &mdash; ${item.productStockQty > 0 ? item.productStockQty + " in stock" : "Out of stock"}
                <div>
                    <button class="moveToCartBtn" ${item.productStockQty > 0 ? "" : "disabled"}>Move to cart</button>
                    <button class="removeWishlistBtn secondary">Remove</button>
                </div>
            </div>
        `).join("");
        wireButtons();
    } catch (err) {
        container.innerHTML = "<p>Could not load wishlist: " + escapeHtml(err.message) + "</p>";
    }
}

function wireButtons() {
    document.querySelectorAll(".moveToCartBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const row = e.target.closest(".cart-item");
            const productId = row.dataset.productId;
            try {
                await api.post("/cart/items", { productId: parseInt(productId, 10), quantity: 1 });
                await api.del("/wishlist/" + productId);
                loadWishlist();
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
                await api.del("/wishlist/" + productId);
                loadWishlist();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}

loadWishlist();
