<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="seller-dashboard">
    <h1>Seller Dashboard</h1>

    <form id="productForm" class="product-form">
        <input type="hidden" id="editingId">
        <label for="pName">Name</label>
        <input type="text" id="pName" required>
        <label for="pDescription">Description</label>
        <textarea id="pDescription" maxlength="2000"></textarea>
        <label for="pPrice">Price</label>
        <input type="number" id="pPrice" step="0.01" min="0.01" required>
        <label for="pStock">Stock quantity</label>
        <input type="number" id="pStock" min="0" required>
        <label for="pCategory">Category</label>
        <input type="text" id="pCategory" required>
        <label for="pImageUrl">Image URL</label>
        <input type="text" id="pImageUrl">
        <button type="submit" id="productSubmitBtn">Add listing</button>
        <button type="button" id="productCancelEditBtn" class="hidden">Cancel edit</button>
    </form>
    <p id="productFormError" class="form-error"></p>

    <h2>Your listings</h2>
    <div id="sellerProductList"></div>
</section>

<script src="${pageContext.request.contextPath}/js/seller.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
