const RECENTLY_VIEWED_KEY="krishvamart-recently-viewed";
const RECENTLY_VIEWED_MAX=8;

function readRecentlyViewed() {
    try {
        const raw=localStorage.getItem(RECENTLY_VIEWED_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch {
        return [];
    }
}

function recordRecentlyViewed(product) {
    if (!product || !product.id) return;
    let list=readRecentlyViewed().filter(p => p.id!==product.id);
    list.unshift({
        id:product.id,
        name:product.name,
        price:product.price,
        imageUrl:product.imageUrl
    });
    localStorage.setItem(RECENTLY_VIEWED_KEY, JSON.stringify(list.slice(0, RECENTLY_VIEWED_MAX)));
}

function renderRecentlyViewedStrip(containerId, excludeId) {
    const container=document.getElementById(containerId);
    if (!container) return;

    const items=readRecentlyViewed().filter(p=> String(p.id)!==String(excludeId));
    if (!items.length) {
        container.classList.add("hidden");
        return;
    }

    container.classList.remove("hidden");
    container.innerHTML= `
        <h2>Recently Viewed</h2>
        <div class="recently-viewed-strip">
            ${items.map(p => `
                <a class="mini-card" href="product-detail.jsp?id=${p.id}">
                    ${p.imageUrl ? `<img src="${window.escapeHtml(p.imageUrl)}" alt="${window.escapeHtml(p.name)}">` : ""}
                    <div>${window.escapeHtml(p.name)}</div>
                    <strong>${window.formatMoney(p.price)}</strong>
                </a>
            `).join("")}
        </div>
    `;
}
window.RecentlyViewed={
    push:recordRecentlyViewed,
    render:renderRecentlyViewedStrip,
    getAll:readRecentlyViewed
};