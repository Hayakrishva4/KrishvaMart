async function loadProducts() {
    const grid = document.getElementById("productGrid");
    const keyword = document.getElementById("searchInput").value.trim();
    const category = document.getElementById("categorySelect").value;
    const params = new URLSearchParams();
    if (keyword) params.set("q", keyword);
    if (category) params.set("category", category);

    grid.innerHTML = "<p>Loading products...</p>";
    try {
        const products = await api.get("/products" + (params.toString() ? "?" + params.toString() : ""));
        if (products.length === 0) {
            grid.innerHTML = "<p>No products found.</p>";
            return;
        }
        grid.innerHTML = products.map(renderCard).join("");
    } catch (err) {
        grid.innerHTML = "<p>Could not load products: " + escapeHtml(err.message) + "</p>";
    }
}

function renderCard(p) {
    const img = p.imageUrl ? escapeHtml(p.imageUrl) : "";
    return `
        <a class="product-card" href="product-detail.jsp?id=${p.id}" style="text-decoration:none;color:inherit;">
            ${img ? `<img src="${img}" alt="${escapeHtml(p.name)}">` : ""}
            <strong>${escapeHtml(p.name)}</strong>
            <span class="category">${escapeHtml(p.category)}</span>
            <span class="price">${formatMoney(p.price)}</span>
            <span>${p.stockQty > 0 ? p.stockQty + " in stock" : "Out of stock"}</span>
        </a>
    `;
}

document.getElementById("searchBtn").addEventListener("click", loadProducts);
document.getElementById("searchInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") loadProducts();
});
loadProducts();
