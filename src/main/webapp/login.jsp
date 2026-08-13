<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="auth-wrapper">
    <section class="auth-form">
        <h1>Login</h1>
        <form id="loginForm">
            <div class="form-group">
                <label for="email">Email</label>
                <input type="email" id="email" name="email" placeholder="Enter your email" required>
            </div>
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" placeholder="Enter your password" required>
            </div>
            <button type="submit" class="btn-submit">Login</button>
        </form>
        <p id="loginError" class="form-error"></p>
        <p class="auth-switch">No account? <a href="${pageContext.request.contextPath}/jsp/register.jsp">Register</a></p>
      </section>
</div>
<script src="${pageContext.request.contextPath}/js/login.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>