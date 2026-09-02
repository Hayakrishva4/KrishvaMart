let currentPage = 1;
async function loadProducts(page) {
    currentPage = page || 1;
    const grid = document.getElementById("productGrid");
    const keyword = document.getElementById("searchInput").value.trim();
    const category = document.getElementById("categorySelect").value;
    const minPrice = document.getElementById("minPriceInput").value;
    const maxPrice = document.getElementById("maxPriceInput").value;
    const sort = document.getElementById("sortSelect").value;
    const params = new URLSearchParams();
    if (keyword) params.set("q", keyword);
    if (category) params.set("category", category);
    if (minPrice) params.set("minPrice", minPrice);
    if (maxPrice) params.set("maxPrice", maxPrice);
    if (sort) params.set("sort", sort);
    params.set("page", currentPage);
    params.set("pageSize", 50);
    grid.innerHTML = "<p>Loading products...</p>";
    try {
        const result = await api.get("/products?" + params.toString());
        if (result.items.length === 0) {
            grid.innerHTML = "<p>No products found.</p>";
            document.getElementById("pagination").classList.add("hidden");
            return;
        }
        grid.innerHTML = result.items.map(renderCard).join("");
        renderPagination(result);
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
            <span class="price">&#8377;${Number(p.price).toFixed(2)}</span>           
            <span>${p.stockQty > 0 ? p.stockQty + " in stock" : "Out of stock"}</span>
        </a>
    `;
}
function renderPagination(result) {
    const nav = document.getElementById("pagination");
    const totalPages = result.totalPages;
    if (totalPages <= 1) {
        nav.classList.add("hidden");
        return;
    }
    nav.classList.remove("hidden");
    let html = "";
    for (let p = 1; p <= totalPages; p++) {
        html += `<button class="page-btn${p === result.page ? " active" : ""}" data-page="${p}">${p}</button>`;
    }
    nav.innerHTML = html;
    nav.querySelectorAll(".page-btn").forEach(btn => {
        btn.addEventListener("click", () => loadProducts(parseInt(btn.dataset.page, 10)));
    });
}
document.getElementById("searchBtn").addEventListener("click", () => loadProducts(1));
document.getElementById("searchInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") loadProducts(1);
});
window.addEventListener('load', () => {
    loadProducts(1);
});
// I HAVE COMMENTED THIS OUT SO IT STOPS CRASHING
// renderRecentlyViewedStrip("recentlyViewed");
function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}