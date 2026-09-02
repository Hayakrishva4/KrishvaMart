const RECENTLY_VIEWED_KEY = "krishvamart-recently-viewed";
const RECENTLY_VIEWED_MAX = 8;
function recordRecentlyViewed(product) {
    let list = readRecentlyViewed();
    list = list.filter(p => p.id !== product.id);
    list.unshift({
        id: product.id,
        name: product.name,
        price: product.price,
        imageUrl: product.imageUrl
    });
    list = list.slice(0, RECENTLY_VIEWED_MAX);
    localStorage.setItem(RECENTLY_VIEWED_KEY, JSON.stringify(list));
}
function readRecentlyViewed() {
    try {
        const raw = localStorage.getItem(RECENTLY_VIEWED_KEY);
        return raw ? JSON.parse(raw) : [];
    } catch (e) {
        return [];
    }
}
function renderRecentlyViewedStrip(containerId, excludeId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const items = readRecentlyViewed().filter(p => String(p.id) !== String(excludeId));
    if (items.length === 0) {
        container.classList.add("hidden");
        return;
    }
    container.classList.remove("hidden");
    container.innerHTML = `
        <h2>Recently viewed</h2>
        <div class="recently-viewed-strip">
            ${items.map(p => `
                <a class="mini-card" href="product-detail.jsp?id=${p.id}">
                    ${p.imageUrl ? `<img src="${escapeHtml(p.imageUrl)}" alt="${escapeHtml(p.name)}">` : ""}
                    <div>${escapeHtml(p.name)}</div>
                    <strong>${formatMoney(p.price)}</strong>
                </a>
            `).join("")}
        </div>
    `;
}
