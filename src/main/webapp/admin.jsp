<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="admin-page">
    <h1>Admin</h1>

    <h2>Users</h2>
    <div id="adminUsers"></div>

    <h2>All orders</h2>
    <div id="adminOrders"></div>

    <h2>Remove a listing</h2>
    <form id="moderateForm">
        <label for="moderateProductId">Product ID</label>
        <input type="number" id="moderateProductId" required>
        <button type="submit">Deactivate listing</button>
    </form>
    <p id="moderateMessage"></p>
</section>

<script src="${pageContext.request.contextPath}/js/admin.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
