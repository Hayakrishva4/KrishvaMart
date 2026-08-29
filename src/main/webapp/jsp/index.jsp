<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
    /* Banner Layout & Styling (Theme Independent) */
    .promo-container {
        display: grid;
        grid-template-columns: 1fr 1fr;
        gap: 20px;
        margin: 20px 0 30px 0;
    }
    .promo-banner {
        position: relative;
        border-radius: 12px;
        overflow: hidden;
        display: flex;
        align-items: center;
        justify-content: center;
        text-align: center;
        color: #ffffff; /* Always white text over images */
        cursor: pointer;
        transition: transform 0.2s ease;
        text-decoration: none;
        box-shadow: 0 4px 10px rgba(0,0,0,0.2);
    }
    .promo-banner:hover {
        transform: scale(1.02);
    }
    /* Dark overlay so text is readable on ANY image */
    .promo-banner::before {
        content: '';
        position: absolute;
        top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(0, 0, 0, 0.55); 
        z-index: 1;
    }
    .promo-banner > div {
        position: relative;
        z-index: 2;
        padding: 30px 20px;
    }
    
    /* Specific Banner Images */
    .promo-discount {
        grid-column: 1 / -1; /* Makes this banner full width */
        min-height: 220px;
        background: url('https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=1200&q=80') center/cover;
    }
    .promo-elec {
        min-height: 180px;
        background: url('https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=800&q=80') center/cover;
    }
    .promo-home {
        min-height: 180px;
        background: url('https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80') center/cover;
    }

    /* Mobile Responsiveness */
    @media (max-width: 768px) {
        .promo-container { grid-template-columns: 1fr; }
    }
</style>

<script>
    // This perfectly links your banners to your existing search filters
    function autoFilterCategory(category) {
        const select = document.getElementById('categorySelect');
        const btn = document.getElementById('searchBtn');
        if (select && btn) {
            select.value = category;
            btn.click();
            document.getElementById('productGrid').scrollIntoView({ behavior: 'smooth' });
        }
    }
</script>

<!-- === PROMOTIONAL BANNERS === -->
<section class="promo-container">
    <!-- Full Width Discount Banner -->
    <div class="promo-banner promo-discount" onclick="document.getElementById('productGrid').scrollIntoView({ behavior: 'smooth' });">
        <div>
            <h1 style="margin: 0 0 10px 0; font-size: 2.5rem; text-shadow: 2px 2px 4px rgba(0,0,0,0.5);">🎉 Grand Opening!</h1>
            <p style="margin: 0; font-size: 1.2rem; text-shadow: 1px 1px 3px rgba(0,0,0,0.5);">Get <strong>10% OFF</strong> all items during your first week. Start exploring below!</p>
        </div>
    </div>

    <!-- Electronics Category Banner -->
    <div class="promo-banner promo-elec" onclick="autoFilterCategory('Electronics')">
        <div>
            <h2 style="margin: 0 0 5px 0; font-size: 2rem;">Electronics</h2>
            <p style="margin: 0;">Shop latest gadgets & tech</p>
        </div>
    </div>

    <!-- Home Category Banner -->
    <div class="promo-banner promo-home" onclick="autoFilterCategory('Home')">
        <div>
            <h2 style="margin: 0 0 5px 0; font-size: 2rem;">Home & Living</h2>
            <p style="margin: 0;">Upgrade your space</p>
        </div>
    </div>
</section>
<!-- =========================== -->

<section class="search-bar">
    <input type="text" id="searchInput" placeholder="Search products...">
    <select id="categorySelect">
        <option value="">All categories</option>
        <option value="Electronics">Electronics</option>
        <option value="Apparel">Apparel</option>
        <option value="Home">Home</option>
    </select>
    <input type="number" id="minPriceInput" placeholder="Min price" min="0" step="0.01" style="width:110px;">
    <input type="number" id="maxPriceInput" placeholder="Max price" min="0" step="0.01" style="width:110px;">
    <select id="sortSelect">
        <option value="RELEVANCE">Newest first</option>
        <option value="PRICE_ASC">Price: Low to High</option>
        <option value="PRICE_DESC">Price: High to Low</option>
    </select>
    <button id="searchBtn">Search</button>
</section>

<section id="recentlyViewed" class="recently-viewed hidden"></section>

<section id="productGrid" class="product-grid">
    <p>Loading products...</p>
</section>

<nav id="pagination" class="pagination hidden"></nav>

<!-- FIXED SCRIPT TAGS: Added api.js and removed the broken recently-viewed.js -->
<script src="${pageContext.request.contextPath}/js/products.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>