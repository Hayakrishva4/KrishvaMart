<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
.dashboard-container {
    max-width: 1120px;
    margin: 1.5rem auto 3rem;
    padding: 0 1rem;
    position: relative;
    z-index: 1;
}

.dashboard-header {
    margin-bottom: 1.25rem;
}

.dashboard-header h1 {
    font-size: 1.6rem;
    font-weight: 700;
    margin-bottom: 0.25rem;
    color: var(--text);
}

.dashboard-grid {
    display: grid;
    grid-template-columns: 360px 1fr;
    gap: 1.5rem;
    align-items: start;
}

@media (max-width: 860px) {
    .dashboard-grid {
        grid-template-columns: 1fr;
    }
}

.dashboard-card {
    background: var(--card-bg);
    border: 1px solid var(--border);
    border-radius: 12px;
    padding: 1.25rem;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.05);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
}

.dashboard-card h2 {
    font-size: 1.15rem;
    font-weight: 600;
    margin-bottom: 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid var(--border);
    color: var(--text);
}

.compact-form {
    display: flex;
    flex-direction: column;
    gap: 0.65rem;
}

.compact-form label {
    font-size: 0.8rem;
    font-weight: 600;
    color: var(--muted);
    margin-bottom: -0.35rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.compact-form input,
.compact-form textarea {
    padding: 0.5rem 0.65rem;
    font-size: 0.875rem;
    border: 1px solid var(--border);
    border-radius: 6px;
    background: var(--bg);
    color: var(--text);
    width: 100%;
    box-sizing: border-box;
}

.compact-form textarea {
    height: 70px;
    resize: vertical;
}

.compact-form .form-actions {
    display: flex;
    gap: 0.5rem;
    margin-top: 0.5rem;
}

.compact-form button {
    flex: 1;
    padding: 0.55rem;
    font-size: 0.875rem;
    font-weight: 600;
    border-radius: 6px;
    cursor: pointer;
}

#sellerProductList table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.875rem;
}

#sellerProductList th,
#sellerProductList td {
    padding: 0.65rem;
    border-bottom: 1px solid var(--border);
    text-align: left;
}

#sellerProductList th {
    color: var(--muted);
    font-weight: 600;
    font-size: 0.8rem;
    text-transform: uppercase;
}
</style>

<div class="dashboard-container">
    <div class="dashboard-header">
        <h1>Seller Dashboard</h1>
    </div>
    <div class="dashboard-grid">
        <div class="dashboard-card">
            <h2>Manage Listing</h2>
            <form id="productForm" class="compact-form">
                <input type="hidden" id="editingId">
                
                <label for="pName">Name</label>
                <input type="text" id="pName" placeholder="Product name" required>
                <label for="pDescription">Description</label>
                <textarea id="pDescription" maxlength="2000" placeholder="Brief summary..."></textarea>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                    <div>
                        <label for="pPrice">Price (₹)</label>
                        <input type="number" id="pPrice" step="0.01" min="0.01" placeholder="0.00" required>
                    </div>
                    <div>
                        <label for="pStock">Stock</label>
                        <input type="number" id="pStock" min="0" placeholder="0" required>
                    </div>
                </div>

                <label for="pCategory">Category</label>
                <input type="text" id="pCategory" placeholder="e.g. Electronics, Home" required>
                <label for="pImageUrl">Image URL</label>
                <input type="text" id="pImageUrl" placeholder="https://...">

                <div class="form-actions">
                    <button type="submit" id="productSubmitBtn" class="btn btn-primary" style="background: var(--primary); color: #fff; border: none;">Add listing</button>
                    <button type="button" id="productCancelEditBtn" class="hidden" style="background: transparent; border: 1px solid var(--border); color: var(--text);">Cancel</button>
                </div>
            </form>
            <p id="productFormError" class="form-error" style="margin-top: 0.5rem; color: var(--error); font-size: 0.8rem;"></p>
        </div>

        <div class="dashboard-card">
            <h2>Your Listings</h2>
            <div id="sellerProductList" style="overflow-x: auto;"></div>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/seller.js"></script>