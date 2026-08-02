<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<section class="auth-form">
    <h1>Log in</h1>
    <form id="loginForm">
        <label for="email">Email</label>
        <input type="email" id="email" required>
        <label for="password">Password</label>
        <input type="password" id="password" required>
        <button type="submit">Log in</button>
    </form>
    <p id="loginError" class="form-error"></p>
    <p>No account? <a href="${pageContext.request.contextPath}/jsp/register.jsp">Register</a></p>
</section>

<script src="${pageContext.request.contextPath}/js/login.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
