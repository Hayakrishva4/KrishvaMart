let currentPage=1;
const PAGE_SIZE=8;

async function loadProducts(page) {
    currentPage=Number(page) || 1;
    const grid=document.getElementById("productGrid");
    const keyword=document.getElementById("searchInput")?.value.trim();
    const category=document.getElementById("categorySelect")?.value;
    const minPrice=document.getElementById("minPriceInput")?.value;
    const maxPrice=document.getElementById("maxPriceInput")?.value;
    const sort=document.getElementById("sortSelect")?.value;

    const params=new URLSearchParams({ page: currentPage, pageSize: PAGE_SIZE });
    if (keyword) params.set("q", keyword);
    if (category && !category.toLowerCase().includes("all")) params.set("category", category);
    if (minPrice) params.set("minPrice", minPrice);
    if (maxPrice) params.set("maxPrice", maxPrice);
    if (sort) params.set("sort", sort);
    if (grid) grid.innerHTML = "<p>Loading products...</p>";

    try {
        const res = await api.get("/products?" + params.toString());
        const payload = res?.data || res || {};
        const items = Array.isArray(payload) ? payload : (payload.items || []);
        const rawTotal = payload.totalItems ?? payload.totalCount ?? payload.total ?? res.totalItems ?? res.totalCount;
        let totalPages = payload.totalPages ?? res.totalPages ?? (!isNaN(rawTotal) ? Math.max(1, Math.ceil(Number(rawTotal) / PAGE_SIZE)) : currentPage);

        if (!items.length && currentPage > 1) {
            loadProducts(1);
            return;
        }
        if (!items.length) {
            if (grid) grid.innerHTML = "<p>No products found.</p>";
            document.getElementById("pagination")?.classList.add("hidden");
            return;
        }
        if (grid) grid.innerHTML = items.map(renderCard).join("");
        triggerScrollCascade();
        renderPagination(currentPage, totalPages);
    } catch (err) {
        if (grid) grid.innerHTML = `<p>Could not load products: ${escapeHtml(err.message)}</p>`;
    }
}
function renderCard(p) {
    const isOutOfStock = p.stockQty <= 0;
    return `
        <div class="product-card" style="display:flex;flex-direction:column;position:relative;height:100%;">
            <a href="product-detail.jsp?id=${p.id}" style="text-decoration:none;color:inherit;flex-grow:1;display:flex;flex-direction:column;">
                ${p.imageUrl ? `<img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}">` : ""}
                <span class="product-id" style="font-size:0.8rem;opacity:0.75;margin-top:0.5rem;">ID: #${p.id}</span>
                <strong style="margin:0.25rem 0;">${escapeHtml(p.name)}</strong>
                <span class="category" style="margin-bottom:0.5rem;">${escapeHtml(p.category || "")}</span>
                <span class="price" style="font-size:1.1rem;font-weight:bold;color:var(--primary);">&#8377;${Number(p.price).toFixed(2)}</span>          
                <span style="font-size:0.85rem;margin-top:0.25rem;">
                    ${isOutOfStock ? '<span style="color:var(--error);">Out of stock</span>' : `${p.stockQty} in stock`}
                </span>
            </a>
            <div style="margin-top:15px;">
                <button onclick="window.location.href='cart.jsp?buyNow=${p.id}'" 
                        style="width:100%;padding:0.65rem;background:var(--primary);color:#fff;border:none;border-radius:8px;font-weight:700;cursor:${isOutOfStock ? 'not-allowed' : 'pointer'};opacity:${isOutOfStock ? 0.5 : 1};transition:filter 0.2s;"
                        ${isOutOfStock ? "disabled" : ""}>
                    Buy Now
                </button>
            </div>
        </div>`;
}
function triggerScrollCascade() {
    const cards=document.querySelectorAll(".product-grid .product-card");
    if (!("IntersectionObserver" in window)) {
        cards.forEach(card=> card.classList.add("in-view"));
        return;
    }
    const observer=new IntersectionObserver((entries, obs)=> {
        entries.forEach(entry=> {
            if (entry.isIntersecting) {
                entry.target.classList.add("in-view");
                obs.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: "0px 0px -40px 0px" });
    cards.forEach((card, index) => {
        card.style.transitionDelay = `${(index % PAGE_SIZE) * 45}ms`;
        observer.observe(card);
    });
}
function renderPagination(page, totalPages) {
    const nav=document.getElementById("pagination");
    if (!nav) return;
    if (page===1 && totalPages <= 1) {
        nav.classList.add("hidden");
        return;
    }
    nav.classList.remove("hidden");

    let html = `<button class="page-btn prev-btn" ${page <= 1 ? "disabled style='opacity:0.35;cursor:not-allowed;'" : ""} data-page="${page - 1}">&lt;&lt; Prev</button>`;
    for (let p=1;p<=totalPages;p++) {
        html += `<button class="page-btn${p===page ? " active" : ""}" data-page="${p}">${p}</button>`;
    }
    html+=`<button class="page-btn next-btn" ${page >= totalPages ? "disabled style='opacity:0.35;cursor:not-allowed;'" : ""} data-page="${page + 1}">Next &gt;&gt;</button>`;
    nav.innerHTML=html;
    nav.querySelectorAll(".page-btn").forEach(btn=> {
        btn.addEventListener("click", ()=> {
            if (btn.disabled) return;
            const targetPage=parseInt(btn.dataset.page, 10);
            if (targetPage >= 1 && targetPage <= totalPages) {
                loadProducts(targetPage);
                document.getElementById("productGrid")?.scrollIntoView({ behavior: "smooth" });
            }
        });
    });
}
document.getElementById("searchBtn")?.addEventListener("click", ()=> loadProducts(1));
document.getElementById("searchInput")?.addEventListener("keydown", (e)=> {
    if (e.key==="Enter") loadProducts(1);
});
document.getElementById("categorySelect")?.addEventListener("change", ()=> loadProducts(1));
document.getElementById("sortSelect")?.addEventListener("change", ()=> loadProducts(1));
window.addEventListener("load", ()=> loadProducts(1));

function escapeHtml(text) {
    if (!text) return "";
    const map={ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" };
    return String(text).replace(/[&<>"']/g, m => map[m]);
}