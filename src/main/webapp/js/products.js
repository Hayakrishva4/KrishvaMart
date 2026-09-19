let currentPage = 1;
const PAGE_SIZE = 8;

async function loadProducts(page) {
    currentPage = Number(page) || 1;
    const grid = document.getElementById("productGrid");
    const keyword = document.getElementById("searchInput").value.trim();
    const category = document.getElementById("categorySelect").value;
    const minPrice = document.getElementById("minPriceInput").value;
    const maxPrice = document.getElementById("maxPriceInput").value;
    const sort = document.getElementById("sortSelect").value;
    const params = new URLSearchParams();
    if (keyword) params.set("q", keyword);
    if (category && category !== "all" && category !== "All categories") {
        params.set("category", category);
    }
    if (minPrice) params.set("minPrice", minPrice);
    if (maxPrice) params.set("maxPrice", maxPrice);
    if (sort) params.set("sort", sort);
    params.set("page", currentPage);
    params.set("pageSize", PAGE_SIZE);

    grid.innerHTML = "<p>Loading products...</p>";
    try {
        const res = await api.get("/products?" + params.toString());
        const payload = (res && res.data) ? res.data : res;
        const items = Array.isArray(payload) ? payload : (payload.items || []);
        
        let rawTotal = payload.totalItems ?? payload.totalCount ?? payload.total ?? res.totalItems ?? res.totalCount ?? res.total;
        let totalCount = (rawTotal !== undefined && rawTotal !== null) ? Number(rawTotal) : NaN;
        let rawPages = payload.totalPages ?? res.totalPages;
        let totalPages = (rawPages !== undefined && rawPages !== null) ? Number(rawPages) : NaN;
        
        if (isNaN(totalPages) || totalPages < 1) {
            if (!isNaN(totalCount) && totalCount >= 0) {
                totalPages = Math.max(1, Math.ceil(totalCount / PAGE_SIZE));
            } else {
                totalPages = items.length < PAGE_SIZE ? currentPage : currentPage;
            }
        }

        if (items.length === 0 && currentPage > 1) {
            loadProducts(1);
            return;
        }

        if (items.length === 0) {
            grid.innerHTML = "<p>No products found.</p>";
            document.getElementById("pagination").classList.add("hidden");
            return;
        }

        grid.innerHTML = items.map(renderCard).join("");
        triggerScrollCascade();
        renderPagination(currentPage, totalPages, items.length);
    } catch (err) {
        grid.innerHTML = "<p>Could not load products: " + escapeHtml(err.message) + "</p>";
    }
}

function renderCard(p) {
    const img = p.imageUrl ? escapeHtml(p.imageUrl) : "";
    return `
        <a class="product-card" href="product-detail.jsp?id=${p.id}" style="text-decoration:none;color:inherit;">
            ${img ? `<img src="${img}" alt="${escapeHtml(p.name)}">` : ""}
            <span class="product-id" style="font-size:0.8rem; opacity:0.75;">ID: #${p.id}</span>
            <strong>${escapeHtml(p.name)}</strong>
            <span class="category">${escapeHtml(p.category)}</span>
            <span class="price">&#8377;${Number(p.price).toFixed(2)}</span>          
            <span>${p.stockQty > 0 ? p.stockQty + " in stock" : "Out of stock"}</span>
        </a>
    `;
}

function triggerScrollCascade() {
    const cards = document.querySelectorAll(".product-grid .product-card");
    
    if (!("IntersectionObserver" in window)) {
        cards.forEach(card => card.classList.add("in-view"));
        return;
    }

    const observer = new IntersectionObserver((entries, obs) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add("in-view");
                obs.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: "0px 0px -40px 0px"
    });

    cards.forEach((card, index) => {
        card.style.transitionDelay = `${(index % PAGE_SIZE) * 45}ms`;
        observer.observe(card);
    });
}

function renderPagination(page, totalPages, currentItemCount) {
    const nav = document.getElementById("pagination");

    if (page === 1 && totalPages <= 1) {
        nav.classList.add("hidden");
        return;
    }
    nav.classList.remove("hidden");

    const isFirstPage = page <= 1;
    const isLastPage = page >= totalPages;

    let html = "";
    html += `<button class="page-btn prev-btn" ${isFirstPage ? "disabled style='opacity:0.35;cursor:not-allowed;'" : ""} data-page="${page - 1}">&lt;&lt; Prev</button>`;

    for (let p = 1; p <= totalPages; p++) {
        html += `<button class="page-btn${p === page ? " active" : ""}" data-page="${p}">${p}</button>`;
    }

    html += `<button class="page-btn next-btn" ${isLastPage ? "disabled style='opacity:0.35;cursor:not-allowed;'" : ""} data-page="${page + 1}">Next &gt;&gt;</button>`;

    nav.innerHTML = html;

    nav.querySelectorAll(".page-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            if (btn.disabled) return;
            const targetPage = parseInt(btn.dataset.page, 10);
            if (isNaN(targetPage) || targetPage < 1) return;
            if (isLastPage && targetPage > page) return;

            loadProducts(targetPage);
            const gridEl = document.getElementById("productGrid");
            if (gridEl) {
                gridEl.scrollIntoView({ behavior: 'smooth' });
            }
        });
    });
}

document.getElementById("searchBtn").addEventListener("click", () => loadProducts(1));
document.getElementById("searchInput").addEventListener("keydown", (e) => {
    if (e.key === "Enter") loadProducts(1);
});

const catSelect = document.getElementById("categorySelect");
if (catSelect) {
    catSelect.addEventListener("change", () => loadProducts(1));
}

window.addEventListener('load', () => {
    loadProducts(1);
});

function escapeHtml(text) {
    if (!text) return '';
    const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}