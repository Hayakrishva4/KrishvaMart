(function () {
    let canvas = document.getElementById("floatingDotsCanvas");
    if (!canvas) {
        canvas = document.createElement("canvas"); canvas.id = "floatingDotsCanvas";
        Object.assign(canvas.style, { position:"fixed",
           top:"0", left:"0", width:"100vw", height:"100vh", pointerEvents:"none", zIndex:"-1" });
        document.body.prepend(canvas);
    }
    const ctx = canvas.getContext("2d"), 
    colors = ["#ff4d4f","#52c41a","#1890ff","#fadb14","#fa8c16","#9254de"], 
    m = { x: -1000, y: -1000, r: 130 };
    let w = (canvas.width = window.innerWidth), h = (canvas.height = window.innerHeight);
    
    window.addEventListener("resize", () => { w = canvas.width = window.innerWidth; h = canvas.height = window.innerHeight; });
    window.addEventListener("mousemove", e => { m.x = e.clientX; m.y = e.clientY; });
    window.addEventListener("touchmove", e => { if (e.touches[0]) { m.x = e.touches[0].clientX; m.y = e.touches[0].clientY; } }, { passive: true });
    window.addEventListener("mouseleave", () => m.x = m.y = -1000);
    window.addEventListener("touchend", () => m.x = m.y = -1000);

    class Dot {
        constructor() { this.reset(true); }
        reset(randY) {
            this.x = Math.random() * w; this.y = randY ? Math.random() * h : h + 10;
            this.r = Math.random() * 1.2 + 0.8; this.c = colors[Math.floor(Math.random() * colors.length)];
            this.vx = this.bx = (Math.random() - 0.5) * 0.6; this.vy = this.by = -(Math.random() * 0.5 + 0.3);
            this.a = Math.random() * 0.45 + 0.5;
        }
        update() {
            let dx = this.x - m.x, dy = this.y - m.y, dist = Math.hypot(dx, dy);
            if (dist < m.r) {
                let f = Math.pow(1 - dist / m.r, 1.5) * 6.5, ang = Math.atan2(dy, dx);
                this.vx += Math.cos(ang) * f; this.vy += Math.sin(ang) * f;
            }
            this.x += (this.vx += (this.bx - this.vx) * 0.05);
            this.y += (this.vy += (this.by - this.vy) * 0.05);
            if (this.y < -10 || this.x < -10 || this.x > w + 10) this.reset();
        }
        draw() {
            ctx.save(); ctx.globalAlpha = this.a; ctx.fillStyle = ctx.shadowColor = this.c; ctx.shadowBlur = 3;
            ctx.beginPath(); ctx.arc(this.x, this.y, this.r, 0, Math.PI * 2); ctx.fill(); ctx.restore();
        }
    }
    const dots = Array.from({ length: Math.max(160, Math.floor(w * h / 4500)) }, () => new Dot());
    (function animate() {
        ctx.clearRect(0, 0, w, h); dots.forEach(d => { d.update(); d.draw(); }); requestAnimationFrame(animate);
    })();
})();