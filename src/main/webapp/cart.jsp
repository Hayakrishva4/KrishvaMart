<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="cart-container">
    <section class="cart-page">
        <h1>Your Shopping Cart</h1>
        <!-- Empty Cart State -->
        <div id="emptyCart" class="empty-cart-view hidden">
            <div class="empty-icon">🛒</div>
            <h2>Your cart is empty</h2>
            <p>Looks like you haven't added anything to your cart yet.</p>
            <a href="${pageContext.request.contextPath}/jsp/products.jsp" class="btn-primary">Continue Shopping</a>
        </div>
        <!-- Main Cart Layout -->
        <div id="cartContent" class="cart-layout">            
            <!-- Left Side: Items List -->
            <div class="cart-items-section">
                <div class="cart-header-actions">
                    <span id="itemCount">0 Items</span>
                    <button id="clearCartBtn" class="btn-text-danger">Clear All</button>
                </div>
                <div id="cartItems" class="cart-items-list">
                    <!-- Dynamic Cart Items Rendered Here -->
                </div>
            </div>
            <!-- Right Side: Order Summary -->
            <div class="cart-summary-section">
                <h2>Order Summary</h2>
                
                <!-- Promo Code Box -->
                <div class="promo-box">
                    <input type="text" id="promoCodeInput" placeholder="Promo code (e.g. SAVE10)" />
                    <button id="applyPromoBtn" class="btn-secondary">Apply</button>
                </div>
                <p id="promoMessage" class="promo-message"></p>
                <!-- Cost Breakdown -->
                <div class="summary-details">
                    <div class="summary-row">
                        <span>Subtotal</span>
                        <span id="cartSubtotal">$0.00</span>
                    </div>
                    <div class="summary-row discount-row hidden" id="discountRow">
                        <span>Discount</span>
                        <span id="cartDiscount">-$0.00</span>
                    </div>
                    <div class="summary-row">
                        <span>Estimated Tax (8%)</span>
                        <span id="cartTax">$0.00</span>
                    </div>
                    <hr />
                    <div class="summary-row total-row">
                        <span>Total</span>
                        <span id="cartTotal">$0.00</span>
                    </div>
                </div>
                <!-- Checkout Actions -->
                <button id="checkoutBtn" class="btn-checkout">
                    <span class="btn-text">Place Order (Mock Payment)</span>
                    <span class="spinner hidden"></span>
                </button>
                <p id="checkoutMessage" class="checkout-message"></p>
                <a href="${pageContext.request.contextPath}/jsp/products.jsp" class="keep-shopping-link">← Continue Shopping</a>
            </div>
        </div>
    </section>
</div>
<!-- Order Confirmation Modal -->
<div id="orderModal" class="modal-overlay hidden">
    <div class="modal-card">
        <div class="modal-icon">🎉</div>
        <h2>Order Placed Successfully!</h2>
        <p>Thank you for your purchase. Your mock payment was processed.</p>
        <p class="order-id">Order ID: <strong id="modalOrderId">#000000</strong></p>
        <button id="closeModalBtn" class="btn-primary">Return to Store</button>
    </div>
</div>
<script src="${pageContext.request.contextPath}/js/cart.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
