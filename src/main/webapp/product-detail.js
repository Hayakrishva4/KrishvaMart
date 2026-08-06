function getProductIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get("id");
}

async function loadProduct() {
    const container = document.getElementById("productDetail");
    const id = getProductIdFromUrl();
    if (!id) {
        container.innerHTML = "<p>No product specified.</p>";
        return;
    }
    try {
        const [product, reviewData] = await Promise.all([
            api.get("/products/" + id),
            api.get("/reviews/product/" + id)
        ]);
        container.innerHTML = `
            ${product.imageUrl ? `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" style="max-width:280px;">` : ""}
            <h1>${escapeHtml(product.name)}</h1>
            <p>${escapeHtml(product.description || "")}</p>
            <p class="price">${formatMoney(product.price)}</p>
            <p>${product.stockQty > 0 ? product.stockQty + " in stock" : "Out of stock"}</p>
            <p>Average rating: ${reviewData.averageRating ? reviewData.averageRating.toFixed(1) : "No ratings yet"} / 5</p>
            <label for="qty">Quantity</label>
            <input type="number" id="qty" min="1" value="1" style="width:80px;">
            <button id="addToCartBtn" ${product.stockQty > 0 ? "" : "disabled"}>Add to cart</button>
            <p id="addToCartMsg"></p>
        `;
        document.getElementById("addToCartBtn").addEventListener("click", () => addToCart(product.id));
        renderReviews(reviewData.reviews);
        document.getElementById("reviewForm").classList.remove("hidden");
        document.getElementById("reviewForm").dataset.productId = product.id;
    } catch (err) {
        container.innerHTML = "<p>Could not load product: " + escapeHtml(err.message) + "</p>";
    }
}

function renderReviews(reviews) {
    const list = document.getElementById("reviewList");
    if (!reviews || reviews.length === 0) {
        list.innerHTML = "<p>No reviews yet.</p>";
        return;
    }
    list.innerHTML = reviews.map(r => `
        <div class="order-card">
            <strong>${"&#9733;".repeat(r.rating)}${"&#9734;".repeat(5 - r.rating)}</strong>
            by ${escapeHtml(r.userName)}
            <p>${escapeHtml(r.comment || "")}</p>
        </div>
    `).join("");
}

async function addToCart(productId) {
    const msg = document.getElementById("addToCartMsg");
    const qty = parseInt(document.getElementById("qty").value, 10) || 1;
    try {
        await api.post("/cart/items", { productId, quantity: qty });
        msg.textContent = "Added to cart.";
    } catch (err) {
        msg.textContent = err.message;
    }
}

document.getElementById("reviewForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById("reviewError");
    errorEl.textContent = "";
    const productId = parseInt(e.target.dataset.productId, 10);
    const orderId = parseInt(document.getElementById("orderIdForReview").value, 10);
    const rating = parseInt(document.getElementById("rating").value, 10);
    const comment = document.getElementById("comment").value;
    try {
        await api.post("/reviews", { productId, orderId, rating, comment });
        loadProduct();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

loadProduct();
