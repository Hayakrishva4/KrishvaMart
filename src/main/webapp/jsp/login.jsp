<%@ page contentType="text/html;charset=UTF-8" %>
    <%@ include file="/WEB-INF/jspf/header.jspf" %>

        <section class="auth-form">
            <h1>Login to Your Account</h1>
            <div class="auth-box">
                <form id="loginForm" class="login-form">
                    <label for="email">Email</label>
                    <input type="email" id="email" required>
                    <label for="password">Password</label>
                    <input type="password" id="password" required>
                    <button type="submit">Login</button>
                </form>
            </div>
            <p id="loginError" class="form-error"></p>
            <p>No account? <a href="${pageContext.request.contextPath}/jsp/register.jsp">Register</a></p>
        </section>
        <style>
            .auth-box {
                max-width: 420px;
                margin: 20px auto 0;
                padding: 20px;
                background: #ffffff1a;
                border: 1px solid #dddddd;
                border-radius: 10px;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
            }
            .login-form {
                display: flex;
                flex-direction: column;
                gap: 10px;
            }
            .login-form label {
                font-weight: 600;
            }
            .login-form input,
            .login-form button {
                width: 100%;
                padding: 10px 12px;
                box-sizing: border-box;
            }
            .login-form button {
                margin-top: 10px;
            }
        </style>
        <script src="${pageContext.request.contextPath}/js/login.js"></script>
        <%@ include file="/WEB-INF/jspf/footer.jspf" %>