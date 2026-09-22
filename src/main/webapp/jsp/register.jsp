<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty sessionScope.authUser}">
    <c:redirect url="/jsp/index.jsp" />
</c:if>

<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
    .register-page {
        min-height: calc(100vh - 90px);
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 15px;
        position: relative;
        overflow: hidden;
    }

    .register-card {
        width: min(900px, 100%);
        display: grid;
        grid-template-columns: 0.9fr 1.1fr;
        background: var(--card-bg);
        border: 1px solid rgba(128, 128, 128, 0.15);
        border-radius: 24px;
        overflow: hidden;
        box-shadow: 0 20px 50px rgba(0, 0, 0, 0.12);
        position: relative;
        z-index: 1;
        animation: cardAppear 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    }

    @keyframes cardAppear {
        from { opacity: 0; transform: translateY(20px) scale(0.98); }
        to { opacity: 1; transform: translateY(0) scale(1); }
    }

    .register-brand {
        position: relative;
        padding: 30px 25px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        text-align: center;
        background: var(--primary);
        color: #fff;
        overflow: hidden;
    }

    .brand-content {
        position: relative;
        z-index: 2;
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
    }

    .mascot-container {
        width: 120px;
        height: 110px;
        margin: 0 auto 12px;
        position: relative;
        cursor: pointer;
    }

    #ai-mascot {
        width: 100%;
        height: 100%;
        overflow: visible;
    }

    #mascot-body {
        animation: floatMascot 4s ease-in-out infinite;
        transform-origin: center;
    }

    @keyframes floatMascot {
        0%, 100% { transform: translateY(0); }
        50% { transform: translateY(-6px); }
    }

    /* Professional Blink Animation Class */
    #mascot-eyes.blinking {
        transform: scaleY(0.05) !important;
        transition: transform 0.08s ease-in-out !important;
    }

    .brand-title {
        font-size: clamp(22px, 3vw, 30px);
        line-height: 1.15;
        font-weight: 800;
        margin: 0 0 8px;
        letter-spacing: -0.8px;
    }

    .brand-text {
        max-width: 270px;
        margin: 0 auto;
        line-height: 1.45;
        font-size: 12.5px;
        opacity: 0.9;
    }

    .register-form-section {
        padding: 30px 35px;
        display: flex;
        flex-direction: column;
        justify-content: center;
    }

    .form-heading {
        margin-bottom: 14px;
    }

    .form-heading .eyebrow {
        color: var(--primary);
        font-size: 10px;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 1.2px;
        margin-bottom: 3px;
    }

    .form-heading h1 {
        margin: 0;
        font-size: 24px;
        line-height: 1.15;
        letter-spacing: -0.5px;
    }

    .form-heading p {
        margin: 3px 0 0;
        color: var(--text-muted, #777);
        font-size: 11.5px;
    }

    #registerForm {
        display: flex;
        flex-direction: column;
        gap: 4px;
    }

    #registerForm label {
        font-size: 11px;
        font-weight: 700;
        margin-top: 4px;
    }

    #registerForm input,
    #registerForm select {
        width: 100%;
        box-sizing: border-box;
        height: 38px;
        padding: 0 12px;
        border-radius: 9px;
        border: 1px solid rgba(128,128,128,0.25);
        background: var(--bg, #fff);
        color: var(--text);
        outline: none;
        font-size: 12.5px;
        transition: border-color 0.2s ease, box-shadow 0.2s ease;
    }

    #registerForm input:focus,
    #registerForm select:focus {
        border-color: var(--primary);
        box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary) 15%, transparent);
    }

    #registerForm button {
        width: 100%;
        height: 41px;
        margin-top: 12px;
        border: none;
        border-radius: 9px;
        background: var(--primary);
        color: #fff;
        font-size: 12.5px;
        font-weight: 800;
        cursor: pointer;
        transition: filter 0.2s ease, transform 0.2s ease;
    }

    #registerForm button:hover {
        filter: brightness(1.05);
        transform: translateY(-1px);
    }

    .form-error {
        min-height: 14px;
        color: #dc3545;
        font-size: 11px;
        font-weight: 600;
    }

    .auth-footer {
        text-align: center;
        margin-top: 12px;
        padding-top: 10px;
        border-top: 1px solid rgba(128,128,128,0.12);
        font-size: 11.5px;
        color: var(--text-muted, #777);
    }

    .auth-footer a {
        color: var(--primary);
        font-weight: 800;
        text-decoration: none;
        margin-left: 3px;
    }

    @media (max-width: 820px) {
        .register-card { grid-template-columns: 1fr; max-width: 460px; }
        .register-brand { padding: 25px 20px; }
        .register-form-section { padding: 25px 20px; }
    }
</style>

<div class="register-page">
    <div class="register-card">
        
        <section class="register-brand">
            <div class="brand-content">
                <div class="mascot-container" id="mascotBox" title="Click me to blink!">
                    <svg id="ai-mascot" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <filter id="neonGlowReg" x="-30%" y="-30%" width="160%" height="160%">
                                <feGaussianBlur stdDeviation="4" result="blur"/>
                                <feComposite in="SourceGraphic" in2="blur" operator="over"/>
                            </filter>
                        </defs>

                        <ellipse cx="100" cy="184" rx="38" ry="6" fill="#000" id="mascot-shadow" opacity="0.25"/>

                        <g id="mascot-body">
                            <rect x="38" y="35" width="124" height="98" rx="28" fill="#ffffff" stroke="#ffffff" stroke-width="4.5" filter="drop-shadow(0 6px 14px rgba(0,0,0,0.15))"/>
                            <line x1="100" y1="35" x2="100" y2="20" stroke="#ffffff" stroke-width="4" stroke-linecap="round"/>
                            <circle cx="100" cy="16" r="4.5" fill="#ffffff" filter="url(#neonGlowReg)"/>

                            <rect x="52" y="48" width="96" height="68" rx="14" fill="#030712" stroke="rgba(255,255,255,0.1)" stroke-width="1.5"/>

                            <!-- Inertial Eyes -->
                            <g id="mascot-eyes" style="transform-origin: 100px 82px; transition: transform 0.15s cubic-bezier(0.25, 1, 0.5, 1);">
                                <rect x="68" y="68" width="18" height="28" rx="9" fill="var(--primary)" filter="url(#neonGlowReg)"/>
                                <rect x="114" y="68" width="18" height="28" rx="9" fill="var(--primary)" filter="url(#neonGlowReg)"/>
                            </g>

                            <!-- Privacy Visor -->
                            <g id="mascot-visor" style="transform-origin: 100px 48px; transform: scaleY(0); transition: transform 0.4s cubic-bezier(0.34,1.56,0.64,1);">
                                <rect x="50" y="46" width="100" height="72" rx="16" fill="#ffffff" stroke="var(--primary)" stroke-width="3"/>
                                <path d="M90 75 V69 A10 10 0 0 1 110 69 V75 H115 V95 H85 V75 Z" fill="var(--primary)"/>
                                <circle cx="100" cy="85" r="3.5" fill="#ffffff"/>
                            </g>
                        </g>
                    </svg>
                </div>

                <h2 class="brand-title">Shop Smarter.<br>Live Better.</h2>
                <p class="brand-text">Create your KrishvaMart account and experience the future of digital retail.</p>
            </div>
        </section>

        <section class="register-form-section">
            <div class="form-heading">
                <div class="eyebrow">Get Started</div>
                <h1>Create Account</h1>
                <p>Enter your details below to set up your profile.</p>
            </div>

            <form id="registerForm">
                <label for="name">Full Name</label>
                <input type="text" id="name" placeholder="" autocomplete="name" required>

                <label for="email">Email Address</label>
                <input type="email" id="email" placeholder="" autocomplete="email" required>

                <label for="password">Password</label>
                <input type="password" id="password" placeholder="Minimum 8 characters" minlength="8" autocomplete="new-password" required>

                <label for="role">Account Type</label>
                <select id="role">
                    <option value="BUYER">Buyer</option>
                    <option value="SELLER">Seller</option>
                    <option value="ADMIN">Admin</option>
                </select>

                <button type="submit">Create Account</button>
            </form>

            <p id="registerError" class="form-error" style="text-align:center; margin-top:6px;"></p>

            <div class="auth-footer">
                Already have an account? <a href="${pageContext.request.contextPath}/jsp/login.jsp">Log in</a>
            </div>
        </section>

    </div>
</div>

<script>
document.addEventListener("DOMContentLoaded", function () {
    const eyes = document.getElementById("mascot-eyes");
    const visor = document.getElementById("mascot-visor");
    const mascotBox = document.getElementById("mascotBox");
    const inputs = document.querySelectorAll("#registerForm input, #registerForm select");
    
    let isPrivacyMode = false;
    let mouseX = 0, mouseY = 0;
    let currentX = 0, currentY = 0;

    document.addEventListener("mousemove", function (e) {
        if (!mascotBox) return;
        const rect = mascotBox.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;
        const deltaX = e.clientX - centerX;
        const deltaY = e.clientY - centerY;
        
        mouseX = Math.max(-10, Math.min(10, deltaX / 14));
        mouseY = Math.max(-6, Math.min(6, deltaY / 14));
    });

    function animateEyes() {
        if (!isPrivacyMode && eyes && !eyes.classList.contains("blinking")) {
            currentX += (mouseX - currentX) * 0.12;
            currentY += (mouseY - currentY) * 0.12;
            eyes.style.transform = "translate(" + currentX + "px, " + currentY + "px)";
        }
        requestAnimationFrame(animateEyes);
    }
    requestAnimationFrame(animateEyes);

    if (mascotBox) {
        mascotBox.addEventListener("click", function () {
            if (isPrivacyMode || eyes.classList.contains("blinking")) return;
            eyes.classList.add("blinking");
            setTimeout(function() {
                eyes.classList.remove("blinking");
            }, 180);
        });
    }

    inputs.forEach(function (input) {
        input.addEventListener("focus", function (e) {
            if (e.target.type === "password") {
                isPrivacyMode = true;
                visor.style.transform = "scaleY(1)";
                eyes.style.transform = "translate(0px, 0px)";
            } else {
                isPrivacyMode = false;
                visor.style.transform = "scaleY(0)";
            }
        });

        input.addEventListener("blur", function () {
            isPrivacyMode = false;
            visor.style.transform = "scaleY(0)";
        });
    });
});
</script>

<script src="${pageContext.request.contextPath}/js/register.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>