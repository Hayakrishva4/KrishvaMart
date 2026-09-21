<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty sessionScope.authUser}">
    <c:redirect url="/jsp/index.jsp" />
</c:if>

<%@ include file="/WEB-INF/jspf/header.jspf" %>

<style>
    .mascot-container {
        width: 135px;
        height: 120px;
        margin: 0 auto 5px auto;
        position: relative;
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
    #mascot-shadow {
        animation: pulseShadow 4s ease-in-out infinite;
        transform-origin: center;
    }
    @keyframes floatMascot {
        0%, 100% { transform: translateY(0px); }
        50% { transform: translateY(-8px); }
    }
    @keyframes pulseShadow {
        0%, 100% { transform: scale(1); opacity: 0.4; filter: blur(3px); }
        50% { transform: scale(0.7); opacity: 0.15; filter: blur(5px); }
    }

    .auth-form form input, .auth-form form select {
        transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.3s ease, border-color 0.3s ease;
    }
    .auth-form form input:focus, .auth-form form select:focus {
        transform: translateX(8px);
        box-shadow: -4px 4px 15px rgba(0,0,0,0.1);
        border-color: var(--primary);
    }
</style>

<div class="auth-container">
    <section class="auth-form">
        <div class="auth-header">
            <div class="mascot-container" id="mascotBox">
                <svg id="ai-mascot" viewBox="0 0 200 200" xmlns="http://www.w3.org/2000/svg">
                    <defs>
                        <filter id="neonGlow2" x="-30%" y="-30%" width="160%" height="160%">
                            <feGaussianBlur stdDeviation="4" result="blur" />
                            <feComposite in="SourceGraphic" in2="blur" operator="over" />
                        </filter>
                    </defs>
                    <ellipse cx="100" cy="184" rx="38" ry="6" fill="var(--text)" id="mascot-shadow" opacity="0.25"/>

                    <g id="mascot-body">
                        <rect x="38" y="35" width="124" height="98" rx="28" fill="var(--card-bg)" stroke="var(--primary)" stroke-width="4.5" filter="drop-shadow(0 6px 16px rgba(0,0,0,0.12))"/>
                        <path d="M54 58 Q54 45 68 43 H132 Q146 45 146 58" fill="none" stroke="var(--primary)" stroke-width="2" opacity=".35"/>
                        <circle cx="52" cy="113" r="4" fill="var(--primary)" opacity=".7"/>
                        <circle cx="148" cy="113" r="4" fill="var(--primary)" opacity=".7"/>
                        
                        <line x1="100" y1="35" x2="100" y2="20" stroke="var(--primary)" stroke-width="4" stroke-linecap="round"/>
                        <circle cx="100" cy="16" r="4.5" fill="var(--primary)" filter="url(#neonGlow2)"/>

                        <rect x="52" y="48" width="96" height="68" rx="14" fill="#030712" stroke="rgba(255,255,255,0.1)" stroke-width="1.5"/>
                        <path d="M61 57 H139" stroke="var(--primary)" stroke-width="2" stroke-linecap="round" opacity=".22"/>
                        
                        <g id="mascot-eyes" style="transition: transform 0.15s cubic-bezier(0.25, 1, 0.5, 1);">
                            <rect x="68" y="68" width="18" height="28" rx="9" fill="var(--primary)" filter="url(#neonGlow2)"/>
                            <rect x="114" y="68" width="18" height="28" rx="9" fill="var(--primary)" filter="url(#neonGlow2)"/>
                        </g>

                        <g id="mascot-visor" style="transform-origin: 100px 48px; transform: scaleY(0); transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);">
                            <rect x="50" y="46" width="100" height="72" rx="16" fill="var(--card-bg)" stroke="var(--primary)" stroke-width="3"/>
                            <path d="M90 75 V69 A10 10 0 0 1 110 69 V75 H115 V95 H85 V75 Z" fill="var(--text)"/>
                            <circle cx="100" cy="85" r="3.5" fill="var(--card-bg)"/>
                        </g>
                    </g>
                </svg>
            </div>
            
            <h1>Register</h1>
            <p class="auth-slogan">"Your Quality is our First Priority"</p>
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

<script>
document.addEventListener("DOMContentLoaded", function() {
    const eyes = document.getElementById("mascot-eyes");
    const visor = document.getElementById("mascot-visor");
    const mascotBox = document.getElementById("mascotBox");
    const inputs = document.querySelectorAll(".auth-form input, .auth-form select");
    let isPrivacyMode = false;

    document.addEventListener("mousemove", function(e) {
        if (isPrivacyMode || !eyes || !mascotBox) return;

        const rect = mascotBox.getBoundingClientRect();
        const centerX = rect.left + rect.width / 2;
        const centerY = rect.top + rect.height / 2;

        const deltaX = e.clientX - centerX;
        const deltaY = e.clientY - centerY;
        
        const panX = Math.max(-12, Math.min(12, deltaX / 12));
        const panY = Math.max(-8, Math.min(8, deltaY / 12));

        eyes.style.transform = "translate(" + panX + "px, " + panY + "px)";
    });

    inputs.forEach(function(input) {
        input.addEventListener("focus", function(e) {
            if (e.target.type === "password") {
                isPrivacyMode = true;
                visor.style.transform = "scaleY(1)";
                eyes.style.transform = "translate(0px, 0px)";
            } else {
                isPrivacyMode = false;
                visor.style.transform = "scaleY(0)";
            }
        });
        input.addEventListener("blur", function() {
            isPrivacyMode = false;
            visor.style.transform = "scaleY(0)";
            eyes.style.transform = "translate(0px, 0px)";
        });
    });
});
</script>

<script src="${pageContext.request.contextPath}/js/register.js"></script>
<%@ include file="/WEB-INF/jspf/footer.jspf" %>