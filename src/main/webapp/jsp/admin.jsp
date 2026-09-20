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

    <h2>Restore a listing</h2>
    <form id="activateForm">
        <label for="activateProductId">Product ID</label>
        <input type="number" id="activateProductId" required>
        <button type="submit">Activate listing</button>
    </form>
    <p id="activateMessage"></p>
</section>

<script src="${pageContext.request.contextPath}/js/admin.js"></script>

<!-- Professional Full-Screen Edge-Gathering Particle Engine -->
<script>
document.addEventListener("DOMContentLoaded", () => {
    // 1. Create a Full-Screen Canvas behind the content
    const canvas = document.createElement("canvas");
    canvas.style.position = "fixed";
    canvas.style.top = "0";
    canvas.style.left = "0";
    canvas.style.width = "100vw";
    canvas.style.height = "100vh";
    canvas.style.pointerEvents = "none";
    canvas.style.zIndex = "-1"; // Ensures it stays completely behind your admin card
    document.body.appendChild(canvas);

    const ctx = canvas.getContext("2d");
    let width, height;
    let dpr = window.devicePixelRatio || 1;

    const particles = [];
    const numParticles = 220; // Increased density for a richer fluid look
    let isAssembling = false;

    // 2. Responsive Resizing
    function resize() {
        width = window.innerWidth;
        height = window.innerHeight;
        canvas.width = width * dpr;
        canvas.height = height * dpr;
        ctx.scale(dpr, dpr);
        updateTargets();
    }
    window.addEventListener("resize", resize);

    // 3. Calculate 'K' coordinates dynamically on the right side of the screen
    function updateTargets() {
        // Places the 'K' at 75% of the screen width, centered vertically
        const offsetX = Math.min(width - 250, width * 0.75); 
        const offsetY = height / 2 - 80;

        for (let i = 0; i < numParticles; i++) {
            let tx, ty;
            if (i < 70) { // Vertical Stem
                tx = offsetX;
                ty = offsetY + (i * (160 / 70));
            } else if (i < 145) { // Upper Diagonal Arm
                let t = (i - 70) / 75;
                tx = offsetX + (t * 90);
                ty = offsetY + 80 - (t * 80);
            } else { // Lower Diagonal Arm
                let t = (i - 145) / 75;
                tx = offsetX + (t * 90);
                ty = offsetY + 80 + (t * 80);
            }
            
            if(particles[i]) {
                particles[i].kTarget = { x: tx, y: ty };
            }
        }
    }

    // 4. Initialize particles at the invisible edges of the screen
    function initParticles() {
        for (let i = 0; i < numParticles; i++) {
            const angle = Math.random() * Math.PI * 2;
            const radius = Math.max(width, height) + 100; // Off-screen distance
            const cx = width / 2;
            const cy = height / 2;
            
            particles.push({
                x: cx + Math.cos(angle) * radius,
                y: cy + Math.sin(angle) * radius,
                vx: 0,
                vy: 0,
                kTarget: { x: 0, y: 0 }, 
                edgeTarget: { // Rest state coordinates (outside the screen)
                    x: cx + Math.cos(angle) * radius, 
                    y: cy + Math.sin(angle) * radius
                },
                size: Math.random() * 2 + 1.2,
                angle: Math.random() * Math.PI * 2,
                speed: Math.random() * 0.05 + 0.02,
                friction: Math.random() * 0.04 + 0.88 // Smooth stopping variance
            });
        }
    }

    resize();
    initParticles();
    updateTargets();

    // 5. Advanced Physics Render Loop
    function animate() {
        ctx.clearRect(0, 0, width, height);
        const isDark = document.documentElement.getAttribute("data-theme") === "dark";
        ctx.fillStyle = isDark ? "rgba(74, 222, 128, 0.85)" : "rgba(26, 93, 58, 0.85)";

        particles.forEach(p => {
            if (isAssembling) {
                // Gather: Strong magnetic pull from edges to the 'K'
                const dx = p.kTarget.x - p.x;
                const dy = p.kTarget.y - p.y;
                
                p.vx += dx * 0.02;
                p.vy += dy * 0.02;
                
                // Tight friction to snap into the shape
                p.vx *= 0.82;
                p.vy *= 0.82;

                // Micro-shimmer while holding shape
                p.angle += p.speed;
                p.x += Math.cos(p.angle) * 0.35;
                p.y += Math.sin(p.angle) * 0.35;

            } else {
                // Disperse: Float back into the ambient edges
                const dx = p.edgeTarget.x - p.x;
                const dy = p.edgeTarget.y - p.y;
                
                // Gentle drift
                p.vx += dx * 0.0015;
                p.vy += dy * 0.0015;
                
                // Loose friction for drifting
                p.vx *= p.friction;
                p.vy *= p.friction;

                // Organic meandering
                p.angle += p.speed;
                p.vx += Math.cos(p.angle) * 0.12;
                p.vy += Math.sin(p.angle) * 0.12;
            }

            p.x += p.vx;
            p.y += p.vy;

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
            ctx.fill();
        });

        requestAnimationFrame(animate);
    }
    
    animate();

    // 6. Choreography: Pull from edges, hold, explode outward
    function triggerAssembly() {
        isAssembling = true;
        
        setTimeout(() => {
            isAssembling = false;
            
            // Assign new random edge targets far off-screen
            particles.forEach(p => {
                const angle = Math.random() * Math.PI * 2;
                const radius = Math.max(width, height) + 200;
                p.edgeTarget = {
                    x: width / 2 + Math.cos(angle) * radius,
                    y: height / 2 + Math.sin(angle) * radius
                };
                
                // Explosive outward kick
                p.vx = (Math.random() - 0.5) * 45;
                p.vy = (Math.random() - 0.5) * 45;
            });
        }, 5000); // Holds the 'K' for 5 seconds
    }
    setTimeout(triggerAssembly, 500);
    setInterval(triggerAssembly, 14000); 
});
</script>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>