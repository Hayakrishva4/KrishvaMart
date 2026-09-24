<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${empty sessionScope.authUser}"><c:redirect url="/jsp/register.jsp" /></c:if>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
    .promo-container{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin:20px 0 30px}
    .promo-banner{position:relative;border-radius:12px;overflow:hidden;display:flex;align-items:center;justify-content:center;text-align:center;color:#fff;cursor:pointer;transition:transform .2s ease;text-decoration:none;box-shadow:0 4px 10px rgba(0,0,0,.2)}
    .promo-banner:hover{transform:scale(1.02)}
    .promo-banner::before{content:'';position:absolute;inset:0;background:rgba(0,0,0,.55);z-index:1}
    .promo-banner>div{position:relative;z-index:2;padding:30px 20px}
    .promo-discount{grid-column:1/-1;min-height:220px;background:url('https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=1200&q=80') center/cover}
    .promo-elec{min-height:180px;background:url('https://images.unsplash.com/photo-1498049794561-7780e7231661?auto=format&fit=crop&w=800&q=80') center/cover}
    .promo-home{min-height:180px;background:url('https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80') center/cover}
    .browse-credits{margin-top:4rem;padding:4rem 2rem;background:var(--card-bg);border-radius:12px;border:1px solid var(--border);box-shadow:0 4px 20px rgba(0,0,0,.02)}
    .stats-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:1.5rem;text-align:center;margin-bottom:4rem;padding-bottom:3rem;border-bottom:1px solid var(--border)}
    .stat-item{display:flex;flex-direction:column;gap:.5rem}
    .stat-num{font-size:2.75rem;font-weight:700;color:var(--primary);font-family:var(--font-mono);line-height:1.1}
    .stat-label{font-size:.85rem;color:var(--muted);font-weight:600;text-transform:uppercase;letter-spacing:.05em}
    .about-section{display:grid;grid-template-columns:1fr 2fr;gap:3rem;align-items:center}
    .about-brand{display:flex;flex-direction:column;gap:1rem;padding-right:2rem;border-right:1px solid var(--border)}
    .about-brand svg{width:64px;height:64px;fill:var(--primary);filter:drop-shadow(0 2px 4px rgba(0,0,0,.1))}
    .about-brand .slogan{font-family:var(--font-mono);font-size:1rem;font-weight:600;color:var(--primary);margin:0}
    .about-text h3{font-size:1.5rem;margin:0 0 1rem;color:var(--text)}
    .about-text p{color:var(--muted);font-size:.95rem;line-height:1.6;margin:0}
    @media (max-width:768px){.promo-container{grid-template-columns:1fr}.stats-grid{grid-template-columns:1fr 1fr;gap:2rem}.about-section{grid-template-columns:1fr;text-align:center}.about-brand{align-items:center;border-right:none;padding-right:0;border-bottom:1px solid var(--border);padding-bottom:2rem}}
</style>

<section class="promo-container">
    <div class="promo-banner promo-discount" onclick="document.getElementById('productGrid').scrollIntoView({behavior:'smooth'})">
        <div><h1 style="margin:0 0 10px;font-size:2.5rem">🎉 Grand Opening!</h1><p style="margin:0;font-size:1.2rem">Get <strong>10% OFF</strong> your first week.</p></div>
    </div>
    <div class="promo-banner promo-elec" onclick="autoFilterCategory('Electronics')">
        <div><h2 style="margin:0 0 5px;font-size:2rem">Electronics</h2><p style="margin:0">Latest tech & gadgets</p></div>
    </div>
    <div class="promo-banner promo-home" onclick="autoFilterCategory('Home')">
        <div><h2 style="margin:0 0 5px;font-size:2rem">Home & Living</h2><p style="margin:0">Upgrade your space</p></div>
    </div>
</section>

<section class="search-bar">
    <input type="text" id="searchInput" placeholder="Search products...">
    <select id="categorySelect">
        <option value="">All categories</option><option value="Electronics">Electronics</option><option value="Apparel">Apparel</option><option value="Home">Home</option>
    </select>
    <input type="number" id="minPriceInput" placeholder="Min price" min="0" step="0.01" style="width:110px;">
    <input type="number" id="maxPriceInput" placeholder="Max price" min="0" step="0.01" style="width:110px;">
    <select id="sortSelect">
        <option value="RELEVANCE">Newest first</option><option value="PRICE_ASC">Price: Low to High</option><option value="PRICE_DESC">Price: High to Low</option>
    </select>
    <button id="searchBtn">Search</button>
</section>

<section id="recentlyViewed" class="recently-viewed hidden"></section>
<section id="productGrid" class="product-grid"><p>Loading products...</p></section>
<nav id="pagination" class="pagination hidden"></nav>

<section class="browse-credits">
    <div class="stats-grid">
        <div class="stat-item"><div class="stat-num" data-target="15000" data-suffix="+">0</div><div class="stat-label">Orders Delivered</div></div>
        <div class="stat-item"><div class="stat-num" data-target="9800" data-suffix="+">0</div><div class="stat-label">Trusted Customers</div></div>
        <div class="stat-item"><div class="stat-num" data-target="94" data-suffix="%">0</div><div class="stat-label">Satisfaction Rate</div></div>
        <div class="stat-item"><div class="stat-num" data-target="24" data-suffix="/7">0</div><div class="stat-label">Customer Support</div></div>
    </div>
    <div class="about-section">
        <div class="about-brand">
            <svg viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg"><path d="M20 15 H32 V85 H20 Z"/><path d="M36 15 H39 V85 H36 Z"/><polygon points="39,52 68,15 86,15 48,58"/><polygon points="42,50 86,85 68,85 39,55"/></svg>
            <h4 class="slogan">"Your Quality is our First Priority"</h4>
        </div>
        <div class="about-text">
            <h3>About KrishvaMart</h3>
            <p>At KrishvaMart, we redefine retail by delivering meticulously vetted essentials with absolute transparency and lightning-fast logistics. We believe premium quality should be a standard, not a luxury.</p>
        </div>
    </div>
</section>

<script>
    function autoFilterCategory(cat){const s=document.getElementById('categorySelect'),b=document.getElementById('searchBtn');if(s&&b){s.value=cat;b.click();document.getElementById('productGrid').scrollIntoView({behavior:'smooth'})}}
    document.addEventListener("DOMContentLoaded",()=>{const obs=new IntersectionObserver((ents,ob)=>{ents.forEach(e=>{if(e.isIntersecting){const c=e.target,t=+c.dataset.target,s=c.dataset.suffix;const u=()=>{const v=+c.innerText.replace(/\D/g,''),i=t/40;if(v<t){c.innerText=Math.ceil(v+i);setTimeout(u,30)}else c.innerText=t+s};u();ob.unobserve(c)}})},{threshold:.5});document.querySelectorAll('.stat-num').forEach(c=>obs.observe(c))});
</script>

<script src="${pageContext.request.contextPath}/js/products.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>