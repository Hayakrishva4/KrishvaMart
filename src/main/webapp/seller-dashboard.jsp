<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="dashboard-wrapper">
    <!-- Top Header Banner -->
    <header class="dashboard-header">
        <div>
            <h1>Seller Hub</h1>
            <p>Manage your inventory, pricing, and product listings in real-time.</p>
        </div>
        <button id="resetFormTopBtn" class="btn-outline">+ Add New Product</button>
    </header>
    <!-- Quick Stats Bar -->
    <div class="stats-grid">
        <div class="stat-card">
            <span class="stat-label">Total Listings :</span>
            <span class="stat-value" id="statTotalProducts">0</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">In Stock :</span>
            <span class="stat-value text-success" id="statInStock">0</span>
        </div>
        <div class="stat-card">
            <span class="stat-label">Low / Out of Stock :</span>
            <span class="stat-value text-warning" id="statLowStock">0</span>
        </div>
    </div>
    <!-- Main Content Layout (Form + Table) -->
    <div class="dashboard-grid">
        <!-- Left Panel: Product Form Card -->
        <aside class="card form-card">
            <div class="card-header">
                <h2 id="formTitle">Add New Product</h2>
                <span class="badge badge-info" id="formModeBadge">Create Mode</span>
            </div>
            <form id="productForm" class="product-form">
                <input type="hidden" id="editingId">
                <!-- Product Name -->
                <div class="form-group">
                    <label for="pName">Product Title <span class="required">*</span></label>
                    <input type="text" id="pName" placeholder="e.g. Mouse, Keyboard,etc..." required>
                </div>
                <!-- Category Dropdown -->
                <div class="form-group">
                    <label for="pCategory">Category <span class="required">*</span></label>
                    <select id="pCategory" required>
                        <option value="" disabled selected>Select a Category</option>
                        <option value="2">Electronics</option>
                        <option value="3">Apparel</option>
                        <option value="4">Kitchen</option>
                        <option value="5">Books</option>
                        <option value="6">Toys</option>
                        <option value="7">Furniture</option>
                    </select>
                </div>
                <!-- Price & Stock Row -->
                <div class="form-row">
                    <div class="form-group col">
                        <label for="pPrice">Price (₹) <span class="required">*</span></label>
                        <input type="number" id="pPrice" step="0.01" min="0.01" placeholder="0.00" required>
                    </div>
                    <div class="form-group col">
                        <label for="pStock">Stock Qty <span class="required">*</span></label>
                        <input type="number" id="pStock" min="0" placeholder="0" required>
                    </div>
                </div>
                <!-- Image URL & Live Preview -->
                <div class="form-group">
                    <label for="pImageUrl">Image URL</label>
                    <input type="url" id="pImageUrl" placeholder="https://example.com/image.jpg">                   
                    <div id="imagePreviewBox" class="image-preview-box">
                        <img id="imagePreview" src="" alt="Preview" class="hidden">
                        <span id="previewPlaceholder">Image preview will appear here</span>
                    </div>
                </div>
                <!-- Description -->
                <div class="form-group">
                    <label for="pDescription">Description</label>
                    <textarea id="pDescription" rows="4" maxlength="2000" placeholder="Highlight key features, material, specs..."></textarea>
                </div>
                <p id="productFormError" class="form-error"></p>
                <!-- Action Buttons -->
                <div class="form-actions">
                    <button type="submit" id="productSubmitBtn" class="btn-primary">Publish Listing</button>
                    <button type="button" id="productCancelEditBtn" class="btn-secondary hidden">Cancel</button>
                </div>
            </form>
        </aside>
        <!-- Right Panel: Inventory Table -->
        <main class="card table-card">
            <div class="table-header-bar">
                <h2>Active Listings</h2>
                <div class="table-search">
                    <input type="text" id="searchInventory" placeholder="Search product title or category...">
                </div>
            </div>
            <div class="table-responsive">
                <div id="sellerProductList">
                    <!-- Javascript will inject table or empty state here -->
                </div>
            </div>
        </main>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/seller.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
