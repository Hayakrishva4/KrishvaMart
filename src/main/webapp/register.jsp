<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/jspf/header.jspf" %>
<div class="auth-wrapper">
    <section class="auth-card">
        <div class="auth-header">
            <h1>Create Account</h1>
            <p>Join KrishvaMart to start shopping or selling !</p>
        </div>
        <form id="registerForm">
            <!-- Name Input Group -->
            <div class="form-group">
                <label for="name">Full Name</label>
                <input type="text" id="name" name="name" placeholder="Enter Name here" required>
            </div>
            <!-- Email Input Group -->
            <div class="form-group">
                <label for="email">Email Address</label>
                <input type="email" id="email" name="email" placeholder="Enter Email here" required>
            </div>
            <!-- Password Input Group -->
            <div class="form-group">
                <label for="password">Password</label>
                <input type="password" id="password" name="password" minlength="8" placeholder="At least 8 characters" required>
            </div>
            <!-- Role Selection Group -->
            <div class="form-group">
                <label for="role">Account Type</label>
                <div class="select-wrapper">
                    <select id="role" name="role">
                        <option value="BUYER">Buyer (Shop items)</option>
                        <option value="SELLER">Seller (List products)</option>
                    </select>
                </div>
            </div>
            <!-- Submit Button -->
            <button type="submit" class="btn-submit">Create Account</button>
        </form>
        <p id="registerError" class="form-error"></p>
        <p class="auth-footer">Already have an account? <a href="${pageContext.request.contextPath}/jsp/login.jsp">Log in</a></p>
    </section>
</div>
<script src="${pageContext.request.contextPath}/js/register.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>
