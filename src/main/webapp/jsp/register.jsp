<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<div class="auth-container">
    <section class="auth-form">
        <div class="auth-header">
            <svg viewBox="0 0 100 100" class="k-logo-svg" fill="#ffffff" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 15 H32 V85 H20 Z" />
                <path d="M36 15 H39 V85 H36 Z" />
                <polygon points="39,52 68,15 86,15 48,58" />
                <polygon points="42,50 86,85 68,85 39,55" />
            </svg>
            <h1>Register</h1>
            <p class="auth-slogan">" You're Quality is our First Priority "</p>
        </div>
        
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
        
        <p id="registerError" class="form-error" style="text-align: center; margin-top: 10px;"></p>
        
        <div class="auth-footer">
            Already have an account? <a href="${pageContext.request.contextPath}/jsp/login.jsp">Log in</a>
        </div>
    </section>
</div>

<script src="${pageContext.request.contextPath}/js/register.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>