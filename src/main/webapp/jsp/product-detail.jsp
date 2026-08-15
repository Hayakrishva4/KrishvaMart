<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section id="productDetail" class="product-detail">
    <p>Loading...</p>
</section>

<section id="recentlyViewed" class="recently-viewed hidden"></section>
<!--
<section id="reviewSection" class="review-section">
    <h2>Reviews</h2>
    <div id="reviewList"></div>
    <form id="reviewForm" class="hidden">
        <h3>Leave a review</h3>
        <label for="orderIdForReview">Order ID (must be a delivered order containing this product)</label>
        <input type="number" id="orderIdForReview" required>
        <label for="rating">Rating</label>
        <select id="rating">
            <option value="5">5 - Excellent</option>
            <option value="4">4 - Good</option>
            <option value="3">3 - Average</option>
            <option value="2">2 - Poor</option>
            <option value="1">1 - Very poor</option>
        </select>
        <label for="comment">Comment</label>
        <textarea id="comment" maxlength="2000"></textarea>
        <button type="submit">Submit review</button>
    </form>
    <p id="reviewError" class="form-error"></p>
</section>
-->
<script src="${pageContext.request.contextPath}/js/recently-viewed.js"></script>
<script src="${pageContext.request.contextPath}/js/product-detail.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
