<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ include file="/WEB-INF/jspf/header.jspf" %>
        <section class="cart-page">
            <h1>Your Cart</h1>
            <div id="cartItems"></div>
            <div class="cart-total">Total: <span id="cartTotal">$0.00</span></div>
            <h3>Enter Address</h3>
            <textarea id="shippingAddress" rows="3" placeholder="Street, city, state, PIN code"
                style="width:100%; max-width:400px;"></textarea>
            <h3>Delivery address</h3>
            <br>
            <button id="checkoutBtn">Place order (mock payment)</button>
            <p id="checkoutMessage"></p>
        </section>
        <script src="${pageContext.request.contextPath}/js/cart.js"></script>
        <%@ include file="/WEB-INF/jspf/footer.jspf" %>