<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
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

<script src="${pageContext.request.contextPath}/js/recently-viewed.js"></script>
<script src="${pageContext.request.contextPath}/js/products.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
