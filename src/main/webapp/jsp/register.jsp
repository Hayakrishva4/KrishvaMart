<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="auth-form" style="position: relative; z-index: 1; backdrop-filter: blur(12px); -webkit-backdrop-filter: blur(12px); background: var(--card-bg); border: 1px solid var(--border); box-shadow: 0 10px 35px rgba(0, 0, 0, 0.08);">
    <h1>Register</h1>
    <form id="registerForm">
        <label for="name">Name</label>
        <input type="text" id="name" required>
        <label for="email">Email</label>
        <input type="email" id="email" required>
        <label for="password">Password (min 8 characters)</label>
        <input type="password" id="password" minlength="8" required>
        <label for="role">I am a</label>
        <select id="role">
            <option value="BUYER">Buyer</option>
            <option value="SELLER">Seller</option>
            <option value="ADMIN">Admin</option>
        </select>
        <button type="submit">Create account</button>
    </form>
    <p id="registerError" class="form-error"></p>
    <p>Already have an account? <a href="${pageContext.request.contextPath}/jsp/login.jsp">Log in</a></p>
</section>

<script src="${pageContext.request.contextPath}/js/register.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>