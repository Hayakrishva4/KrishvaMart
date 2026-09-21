<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- SECURITY CHECK: Redirect unauthenticated users straight to Register -->
<c:if test="${empty sessionScope.authUser}">
    <c:redirect url="/jsp/register.jsp" />
</c:if>

<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
    /* Promo Banners */
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
        color: #ffffff; 
        cursor: pointer;
        transition: transform 0.2s ease;
        text-decoration: none;
        box-shadow: 0 4px 10px rgba(0,0,0,0.2);
    }
    .promo-banner:hover { transform: scale(1.02); }
    .promo-banner::before {
        content: ''; position: absolute; top: 0; left: 0; right: 0; bottom: 0;
        background: rgba(0, 0, 0, 0.55); z-index: 1;
    }
    .promo-banner > div { position: relative; z-index: 2; padding: 30px 20px; }
    .promo-discount {
        grid-column: 1 / -1; min-height: 220px;
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

    /* Browse Page Credits & About Section */
    .browse-credits {
        margin-top: 4rem;
        padding: 4rem 2rem;
        background: var(--card-bg);
        border-radius: 12px;
        border: 1px solid var(--border);
        box-shadow: 0 4px 20px rgba(0,0,0,0.02);
    }
    
    .stats-grid {
        display: grid;
        grid-template-columns: repeat(4, 1fr);
        gap: 1.5rem;
        text-align: center;
        margin-bottom: 4rem;
        padding-bottom: 3rem;
        border-bottom: 1px solid var(--border);
    }
    .stat-item { display: flex; flex-direction: column; gap: 0.5rem; }
    .stat-num {
        font-size: 2.75rem;
        font-weight: 700;
        color: var(--primary);
        font-family: var(--font-mono);
        line-height: 1.1;
    }
    .stat-label {
        font-size: 0.85rem;
        color: var(--muted);
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .about-section {
        display: grid;
        grid-template-columns: 1fr 2fr;
        gap: 3rem;
        align-items: center;
    }
    .about-brand {
        display: flex;
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
        padding-right: 2rem;
        border-right: 1px solid var(--border);
    }
    .about-brand .k-logo-svg {
        width: 64px;
        height: 64px;
        fill: var(--primary);
        filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
    }
    .about-brand .slogan {
        font-family: var(--font-mono);
        font-size: 1rem;
        font-weight: 600;
        color: var(--primary);
        margin: 0;
        line-height: 1.4;
    }
    .about-text h3 {
        font-size: 1.5rem;
        margin: 0 0 1rem 0;
        color: var(--text);
        letter-spacing: -0.02em;
    }
    .about-text p {
        color: var(--muted);
        font-size: 0.95rem;
        line-height: 1.7;
        margin: 0 0 1rem 0;
    }

    @media (max-width: 768px) {
        .promo-container { grid-template-columns: 1fr; }
        .stats-grid { grid-template-columns: 1fr 1fr; gap: 2rem; }
        .about-section { grid-template-columns: 1fr; text-align: center; }
        .about-brand { align-items: center; border-right: none; padding-right: 0; border-bottom: 1px solid var(--border); padding-bottom: 2rem; }
    }
</style>

<script>
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

<section class="promo-container">
    <div class="promo-banner promo-discount" onclick="document.getElementById('productGrid').scrollIntoView({ behavior: 'smooth' });">
        <div>
            <h1 style="margin: 0 0 10px 0; font-size: 2.5rem; text-shadow: 2px 2px 4px rgba(0,0,0,0.5);">🎉 Grand Opening!</h1>
            <p style="margin: 0; font-size: 1.2rem; text-shadow: 1px 1px 3px rgba(0,0,0,0.5);">Get <strong>10% OFF</strong> all items during your first week. Start exploring below!</p>
        </div>
    </div>
    <div class="promo-banner promo-elec" onclick="autoFilterCategory('Electronics')">
        <div>
            <h2 style="margin: 0 0 5px 0; font-size: 2rem;">Electronics</h2>
            <p style="margin: 0;">Shop latest gadgets & tech</p>
        </div>
    </div>
    <div class="promo-banner promo-home" onclick="autoFilterCategory('Home')">
        <div>
            <h2 style="margin: 0 0 5px 0; font-size: 2rem;">Home & Living</h2>
            <p style="margin: 0;">Upgrade your space</p>
        </div>
    </div>
</section>

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

<!-- Custom Browse Page Credits & About Section -->
<section class="browse-credits">
    <div class="stats-grid">
        <div class="stat-item">
            <div class="stat-num" data-target="15000" data-suffix="+">0</div>
            <div class="stat-label">Orders Delivered</div>
        </div>
        <div class="stat-item">
            <div class="stat-num" data-target="9800" data-suffix="+">0</div>
            <div class="stat-label">Trusted Customers</div>
        </div>
        <div class="stat-item">
            <div class="stat-num" data-target="99" data-suffix="%">0</div>
            <div class="stat-label">Satisfaction Rate</div>
        </div>
        <div class="stat-item">
            <div class="stat-num" data-target="24" data-suffix="/7">0</div>
            <div class="stat-label">Customer Support</div>
        </div>
    </div>

    <div class="about-section">
        <div class="about-brand">
            <svg viewBox="0 0 100 100" class="k-logo-svg" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 15 H32 V85 H20 Z" />
                <path d="M36 15 H39 V85 H36 Z" />
                <polygon points="39,52 68,15 86,15 48,58" />
                <polygon points="42,50 86,85 68,85 39,55" />
            </svg>
            <h4 class="slogan">"Your Quality is our First Priority"</h4>
        </div>
        <div class="about-text">
            <h3>About KrishvaMart</h3>
            <p>At KrishvaMart, we believe that premium quality shouldn't be a luxury. From our carefully curated electronics to our finest home and living essentials, every product in our catalog is rigorously vetted to meet the highest standards.</p>
            <p>Our mission is simple: to redefine your online shopping experience by delivering exceptional goods with absolute transparency, lightning-fast logistics, and a support team that truly cares about your satisfaction. Welcome to the future of retail.</p>
        </div>
    </div>
</section>

<script>
    // Intersection Observer for counting animation
    document.addEventListener("DOMContentLoaded", () => {
        const counters = document.querySelectorAll('.stat-num');
        const speed = 60; // Adjust for counting speed

        const animateCounters = (entries, observer) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    const counter = entry.target;
                    const target = +counter.getAttribute('data-target');
                    const suffix = counter.getAttribute('data-suffix');
                    
                    const updateCount = () => {
                        const count = +counter.innerText.replace(/\D/g, ''); // Extract just the numbers
                        const inc = target / speed;

                        if (count < target) {
                            counter.innerText = Math.ceil(count + inc);
                            setTimeout(updateCount, 25); // Frame delay
                        } else {
                            counter.innerText = target + suffix;
                        }
                    };
                    updateCount();
                    observer.unobserve(counter); // Only animate once
                }
            });
        };

        const observer = new IntersectionObserver(animateCounters, { threshold: 0.5 });
        counters.forEach(counter => {
            observer.observe(counter);
        });
    });
</script>

<script src="${pageContext.request.contextPath}/js/products.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>