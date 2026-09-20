<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="auth-container">
    <section class="auth-form">
        <div class="auth-header">
            <!-- Exact Custom K Symbol -->
            <svg viewBox="0 0 100 100" class="k-logo-svg" fill="#ffffff" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 15 H32 V85 H20 Z" />
                <path d="M36 15 H39 V85 H36 Z" />
                <polygon points="39,52 68,15 86,15 48,58" />
                <polygon points="42,50 86,85 68,85 39,55" />
            </svg>
            <h1>Login to Your Account</h1>
            <p class="auth-slogan">" You're Quality is our First Priority "</p>
        </div>
        
        <form id="loginForm">
            <label for="email">Email</label>
            <input type="email" id="email" required>
            
            <label for="password">Password</label>
            <input type="password" id="password" required>
            
            <button type="submit">Login</button>
        </form>
        
        <p id="loginError" class="form-error" style="text-align: center; margin-top: 10px;"></p>
        
        <div class="auth-footer">
            No account? <a href="${pageContext.request.contextPath}/jsp/register.jsp">Register</a>
        </div>
    </section>
</div>

<script src="${pageContext.request.contextPath}/js/login.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>