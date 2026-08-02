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
    <button id="searchBtn">Search</button>
</section>

<section id="productGrid" class="product-grid">
    <p>Loading products...</p>
</section>

<script src="${pageContext.request.contextPath}/js/products.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
