<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="wishlist-layout">
    <section class="wishlist-page">
        <h1>Your Wishlist</h1>
        <div id="wishlistContainer"></div>
        <div style="margin-top: 3.5rem;">
            <h2 style="font-size: 1.25rem; margin-bottom: 1rem;">Suggested for You</h2>
            <div id="suggestedContainer" class="product-grid"></div>
        </div>
    </section>
    <aside class="side-banner">
        <div class="side-banner-content">
            <span class="side-banner-badge">Hot Deals</span>
            <h3>Looking for More?</h3>
            <p>Explore thousands of newly listed items in our marketplace.</p>
            <a href="${pageContext.request.contextPath}/jsp/index.jsp#productGrid" class="side-banner-btn">Browse now !</a>
        </div>
    </aside>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/wishlist.js"></script>