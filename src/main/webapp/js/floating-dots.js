(function () {
    let canvas = document.getElementById("floatingDotsCanvas");
    if (!canvas) {
        canvas = document.createElement("canvas");
        canvas.id = "floatingDotsCanvas";
        canvas.style.position = "fixed";
        canvas.style.top = "0";
        canvas.style.left = "0";
        canvas.style.width = "100vw";
        canvas.style.height = "100vh";
        canvas.style.pointerEvents = "none";
        canvas.style.zIndex = "-1";
        document.body.prepend(canvas);
    }
    const ctx = canvas.getContext("2d");
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);
    window.addEventListener("resize", () => {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    });
    const colors = [
        "#ff4d4f",
        "#52c41a",
        "#1890ff",
        "#fadb14",
        "#fa8c16",
        "#9254de"
    ];
    const mouse = { x: -1000, y: -1000, radius: 130 };
    window.addEventListener("mousemove", (e) => {
        mouse.x = e.clientX;
        mouse.y = e.clientY;
    });
    window.addEventListener("touchmove", (e) => {
        if (e.touches.length > 0) {
            mouse.x = e.touches[0].clientX;
            mouse.y = e.touches[0].clientY;
        }
    }, { passive: true });

    window.addEventListener("mouseleave", () => {
        mouse.x = -1000;
        mouse.y = -1000;
    });
    window.addEventListener("touchend", () => {
        mouse.x = -1000;
        mouse.y = -1000;
    });
    class Dot {
        constructor() {
            this.reset(true);
        }
        reset(randomY = false) {
            this.x = Math.random() * width;
            this.y = randomY ? Math.random() * height : height + 10;
            this.radius = Math.random() * 1.2 + 0.8;
            this.color = colors[Math.floor(Math.random() * colors.length)];
            this.baseVx = (Math.random() - 0.5) * 0.6;
            this.baseVy = -(Math.random() * 0.5 + 0.3);
            this.vx = this.baseVx;
            this.vy = this.baseVy;
            this.alpha = Math.random() * 0.45 + 0.5;
        }
        update() {
            const dx = this.x - mouse.x;
            const dy = this.y - mouse.y;
            const dist = Math.hypot(dx, dy);
            if (dist < mouse.radius) {
                const force = Math.pow((1 - dist / mouse.radius), 1.5) * 6.5;
                const angle = Math.atan2(dy, dx);
                this.vx += Math.cos(angle) * force;
                this.vy += Math.sin(angle) * force;
            }
            this.vx += (this.baseVx - this.vx) * 0.05;
            this.vy += (this.baseVy - this.vy) * 0.05;
            this.x += this.vx;
            this.y += this.vy;
            if (this.y < -10 || this.x < -10 || this.x > width + 10) {
                this.reset(false);
            }
        }
        draw() {
            ctx.save();
            ctx.globalAlpha = this.alpha;
            ctx.fillStyle = this.color;
            ctx.shadowBlur = 3;
            ctx.shadowColor = this.color;
            ctx.beginPath();
            ctx.arc(this.x, this.y, this.radius, 0, Math.PI * 2);
            ctx.fill();
            ctx.restore();
        }
    }
    const dotCount = Math.floor((width * height) / 4500);
    const dots = Array.from({ length: Math.max(160, dotCount) }, () => new Dot());
    function animate() {
        ctx.clearRect(0, 0, width, height);
        for (let i = 0; i < dots.length; i++) {
            dots[i].update();
            dots[i].draw();
        }
        requestAnimationFrame(animate);
    }
    animate();
})();