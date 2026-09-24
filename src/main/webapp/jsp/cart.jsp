<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="cart-layout" style="display: flex; gap: 2rem; max-width: 1180px; margin: 2rem auto; align-items: flex-start; flex-wrap: wrap; padding: 0 1rem;">
    <section class="cart-page" style="flex: 2; min-width: 320px; background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 1.75rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);">
        <h1 style="margin-top: 0; margin-bottom: 1.25rem; font-size: 1.5rem; color: var(--text); border-bottom: 1px solid var(--border); padding-bottom: 0.75rem;">Your Cart</h1>
        <div id="cartItems" style="margin-bottom: 1.5rem;"></div>
        <div class="cart-total" style="display: flex; justify-content: space-between; align-items: center; font-size: 1.2rem; font-weight: 700; color: var(--text); padding: 1rem 0; border-top: 2px solid var(--border); border-bottom: 1px solid var(--border); margin-bottom: 1.5rem;">
            <span>Order Total:</span>
            <span id="cartTotal" style="color: var(--primary);">&#8377;0.00</span>
        </div>
        <h3 style="margin-top: 0; margin-bottom: 0.5rem; font-size: 1.1rem; color: var(--text);">Delivery Address</h3>
        <textarea id="shippingAddress" rows="3" placeholder="Flat / House No., Street, Landmark, City, State, PIN code"
            style="width: 100%; box-sizing: border-box; padding: 0.75rem; border: 1px solid var(--border); border-radius: 8px; font-family: inherit; font-size: 0.95rem; background: var(--card-bg); color: var(--text); resize: vertical; margin-bottom: 1rem;"></textarea>
        <button id="checkoutBtn" class="btn btn-primary" style="width: 100%; padding: 0.85rem; font-size: 1rem; font-weight: 600; cursor: pointer; border-radius: 8px; border: none; background: var(--primary); color: #fff;">Place Order</button>
        <p id="checkoutMessage" style="margin-top: 0.85rem; font-weight: 500; font-size: 0.95rem; text-align: center; min-height: 1.2rem;"></p>
    </section>
    <aside class="recommendations-sidebar" style="flex: 1; min-width: 300px; background: var(--card-bg); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);">
        <h3 style="margin-top: 0; margin-bottom: 1.25rem; font-size: 1.15rem; border-bottom: 1px solid var(--border); padding-bottom: 0.75rem; color: var(--text); font-weight: 700;">
            Recommended for You
        </h3>
        <div id="recommendations">
            <p style="color: var(--muted); font-size: 0.9rem;">Loading recommendations...</p>
        </div>
    </aside>
</div>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
<script src="${pageContext.request.contextPath}/js/cart.js"></script>