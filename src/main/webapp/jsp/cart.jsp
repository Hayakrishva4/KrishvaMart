<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="cart-layout" style="display: flex; gap: 1.5rem; max-width: 1100px; margin: 0 auto; align-items: flex-start; flex-wrap: wrap;">
    <section class="cart-page" style="flex: 2; min-width: 320px; margin-bottom: 1.5rem;">
        <h1>Your Cart</h1>
        <div id="cartItems"></div>
        <div class="cart-total">Total: <span id="cartTotal">$0.00</span></div>
        <h3>Delivery Address</h3>
        <textarea id="shippingAddress" rows="3" placeholder="Street, city, state, PIN code"
            style="width: 100%; max-width: 450px; display: block; margin-bottom: 1rem;"></textarea>
        <button id="checkoutBtn">Place order (mock payment)</button>
        <p id="checkoutMessage" style="margin-top: 0.75rem; font-weight: 500;"></p>
    </section>
    <aside class="recommendations-sidebar" style="flex: 1; min-width: 260px; background: var(--card-bg); border: 1px solid var(--border); border-radius: 8px; padding: 1.25rem;">
        <h3 style="margin-top: 0; margin-bottom: 1rem; font-size: 1.1rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem; color: var(--text);">Recommended for You</h3>
        <div id="recommendations">
            <p style="color: var(--muted); font-size: 0.85rem;">Loading recommendations...</p>
        </div>
    </aside>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/cart.js"></script>
