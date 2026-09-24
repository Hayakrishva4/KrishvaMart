if (typeof window.escapeHtml !== "function") {
    window.escapeHtml = function (str) {
        if (!str) return "";
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
        return String(str).replace(/[&<>"']/g, m => map[m]);
    };
}
if (typeof window.formatMoney !== "function") {
    window.formatMoney = function (v) {
        const num = Number(v);
        return "\u20B9" + (isNaN(num) ? "0.00" : num.toFixed(2));
    };
}
const urlParams = new URLSearchParams(window.location.search);
const buyNowId = urlParams.get('buyNow');
let isOrderPlaced = false;
function injectExitModal() {
    if (document.getElementById("directCheckoutModal")) return;
    const modalHtml = `
     <div id="directCheckoutModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.65); z-index:9999; justify-content:center; align-items:center;">
        <div style="background:var(--card-bg, #1e1e2f); border:1px solid var(--border, #333); padding:1.25rem; border-radius:8px; max-width:280px; width:90%; text-align:center; box-shadow:0 8px 24px rgba(0,0,0,0.5);">
           <h3 style="margin:0 0 .5rem; color:var(--text, #fff); font-size:1.05rem;">Unsaved Item</h3>
            <p style="color:var(--muted, #aaa); font-size:.85rem; margin:0 0 1rem;">Save this item to your cart?</p>
            <div style="display:flex; justify-content:center; gap:.5rem;">
                <button id="modalBuyLaterBtn" style="padding:.4rem .8rem; background:var(--primary, #20c997); color:#fff; border:none; border-radius:4px; font-weight:600; cursor:pointer; font-size:.85rem;">Add to Cart</button>
     <button id="modalDiscardBtn" style="padding:.4rem .8rem; background:transparent; border:1px solid var(--error, #dc3545); color:var(--error, #dc3545); border-radius:4px; font-weight:600; cursor:pointer; font-size:.85rem;">Remove</button>
            </div>
        </div>
    </div>
    `;
    document.body.insertAdjacentHTML("beforeend", modalHtml);
}
function setupNavigationInterceptor() {
  injectExitModal();
    document.addEventListener("click", (e) => {
     if (!buyNowId || isOrderPlaced) return;
        const anchor = e.target.closest("a");
     if (!anchor) return;
        const targetHref = anchor.getAttribute("href");
     if (!targetHref || targetHref.startsWith("#") || targetHref.startsWith("javascript:")) return;
        e.preventDefault();
      showExitModal(targetHref);
    });
  window.addEventListener("beforeunload", (e) => {
    if (buyNowId && !isOrderPlaced && window.directCheckoutItem) {
      e.preventDefault();
      e.returnValue = "";
        }
    });
}
function showExitModal(targetUrl) {
 const modal = document.getElementById("directCheckoutModal");
 const buyLaterBtn = document.getElementById("modalBuyLaterBtn");
 const discardBtn = document.getElementById("modalDiscardBtn");
  modal.style.display = "flex";
  buyLaterBtn.onclick = async () => {
  buyLaterBtn.disabled = true;
  buyLaterBtn.textContent = "Saving...";
    try {
      if (window.directCheckoutItem) {
        await api.post("/cart", {
          productId: window.directCheckoutItem.id,
        quantity: window.directCheckoutItem.quantity || 1
            });
            }
        } catch (err) {
            console.error("Could not save to cart:", err);
        } finally {
            isOrderPlaced = true;
            window.location.href = targetUrl;
        }
    };
    discardBtn.onclick = () => {
     isOrderPlaced = true;
        window.location.href = targetUrl;
    };
}
async function loadCart() {
 const container = document.getElementById("cartItems");
 const totalEl = document.getElementById("cartTotal");
  if (!container) return;
   if (buyNowId) {
    setupNavigationInterceptor();
 const header = document.querySelector(".cart-page h1");
  if (header) header.textContent = "Direct Checkout";
    try {
        const res = await api.get("/products/" + buyNowId);
        const prod = (res && res.data) ? res.data : res;
    if (totalEl) totalEl.textContent = formatMoney(prod.price);
        window.directCheckoutItem = { id: prod.id, price: prod.price, quantity: 1 };
         container.innerHTML = `
            <div class="cart-item" style="display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem; border: 2px solid var(--primary); border-radius: 8px; margin-bottom: 0.85rem; background: var(--card-bg);">
                <div style="flex: 1; min-width: 0;">
                    <strong style="font-size: 1rem; color: var(--text); display: block; margin-bottom: 0.25rem;">
                        ${escapeHtml(prod.name)}
                    </strong>
            <span style="color: var(--primary); font-size: 0.85rem; font-weight: bold; background: rgba(32, 201, 151, 0.1); padding: 0.2rem 0.5rem; border-radius: 4px;">
                Fast Track Checkout
            </span>
        </div>
        <div style="display: flex; align-items: center; gap: 0.5rem;">
          <span style="font-size: 0.9rem; font-weight: 600;">Qty:</span>
    <input type="number" id="directQtyInput" min="1" max="${prod.stockQty}" value="1" class="qtyInput" style="width: 55px; padding: 0.35rem 0.45rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.9rem; text-align: center; background: var(--card-bg); color: var(--text);">     </div>
        <div id="directItemTotal" style="font-weight: 700; font-size: 1.05rem; color: var(--primary); text-align: right; min-width: 80px;">
                        ${formatMoney(prod.price)}
                    </div>
                </div>
            `;
            const directQtyInput = document.getElementById("directQtyInput");
            if (directQtyInput) {
                directQtyInput.addEventListener("change", (e) => {
                 let newQty = parseInt(e.target.value, 10);
                  if (isNaN(newQty) || newQty < 1) newQty = 1;
                    if (newQty > prod.stockQty) {
                     alert("Only " + prod.stockQty + " items available in stock!");
                     newQty = prod.stockQty;
                    }
                e.target.value = newQty;
                 window.directCheckoutItem.quantity = newQty;
            const newTotal = prod.price * newQty;
             document.getElementById("directItemTotal").textContent = formatMoney(newTotal);
                if (totalEl) totalEl.textContent = formatMoney(newTotal);
                });
            }
        } catch (err) {
            container.innerHTML = "<p class='form-error' style='color: var(--error);'>Could not load product for checkout: " + escapeHtml(err.message) + "</p>";
        }
        return;
    }
    try {
     const res = await api.get("/cart");
     const data = (res && res.data) ? res.data : res;
     const items = data.items || [];
     const total = data.total !== undefined ? data.total : (data.totalAmount !== undefined ? data.totalAmount : 0);
      if (totalEl) totalEl.textContent = formatMoney(total);
        if (!items || items.length === 0) {
         container.innerHTML = `
          <style>
            @keyframes floatCart {
             0%, 100% {
              transform: translateY(0px);
            }
        50% {
             transform: translateY(-10px);
            }
        }
    </style>
    <div style="text-align: center; padding: 3.5rem 1rem; display: flex; flex-direction: column; align-items: center;">
     <svg viewBox="0 0 24 24" style="width: 72px; height: 72px; fill: none; stroke: var(--primary, #20c997); stroke-width: 1.5; stroke-linecap: round; stroke-linejoin: round; animation: floatCart 3s ease-in-out infinite; filter: drop-shadow(0 4px 6px rgba(32, 201, 151, 0.2));">
        <circle cx="9" cy="21" r="1"></circle>
        <circle cx="20" cy="21" r="1"></circle>
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"></path>
            <path d="M15 11a3 3 0 0 1-6 0" stroke="var(--muted, #888)" style="opacity: 0.4;"></path>
                </svg>
                    <p style="color: var(--muted); margin: 1.5rem 0 1.25rem; font-size: 1.05rem;">
                        Your cart is currently empty.
                    </p>
                    <a href="index.jsp" class="btn btn-primary" style="padding: 0.55rem 1.4rem; background: var(--primary); color: #fff; border-radius: 6px; text-decoration: none; font-weight: 600; display: inline-block;">
                        Browse Products
                    </a>
                </div>
            `;
            if (totalEl) totalEl.textContent = formatMoney(0);
            return;
        }
  container.innerHTML = items.map(item => `
    <div class="cart-item" data-product-id="${item.productId}" style="display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 1rem; border: 1px solid var(--border); border-radius: 8px; margin-bottom: 0.85rem; background: var(--card-bg);">
      <div style="flex: 1; min-width: 0;">
        <a href="product-detail.jsp?id=${item.productId}" style="font-weight: 600; font-size: 0.95rem; color: var(--text); text-decoration: none; display: block; margin-bottom: 0.25rem;">
         ${escapeHtml(item.productName)}
            </a>
        <span style="color: var(--muted); font-size: 0.85rem;">
         ${formatMoney(item.unitPrice)} each
            </span>
        </div>
    <div style="display: flex; align-items: center; gap: 0.5rem;">
        <input type="number" min="1" max="99" value="${item.quantity}" class="qtyInput" style="width: 55px; padding: 0.35rem 0.45rem; border: 1px solid var(--border); border-radius: 6px; font-size: 0.9rem; text-align: center; background: var(--card-bg); color: var(--text);">
        <button class="updateQtyBtn" style="padding: 0.35rem 0.75rem; font-size: 0.8rem; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--text); cursor: pointer;">Update</button>
        <button class="removeBtn secondary" style="padding: 0.35rem 0.65rem; font-size: 0.8rem; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--error); cursor: pointer;">&#10005;</button>
    </div>
        <div style="font-weight: 600; font-size: 0.95rem; color: var(--primary); min-width: 80px; text-align: right;">
            ${formatMoney(item.unitPrice * item.quantity)}
        </div>
        </div>
        `).join("");
        wireItemButtons();
    } catch (err) {
        container.innerHTML = "<p class='form-error' style='color: var(--error);'>Could not load cart: " + escapeHtml(err.message) + "</p>";
    }
}
async function loadRecommendations() {
 const recContainer = document.getElementById("recommendations");
  if (!recContainer) return;
    try {
     const res = await api.get("/products?page=1&pageSize=6");
     const payload = (res && res.data) ? res.data : res;
     const rawProducts = Array.isArray(payload) ? payload : (payload.items || []);
     const products = rawProducts.slice(0, 3);
      if (!products || products.length === 0) {
        recContainer.innerHTML = "<p style='color: var(--muted); font-size: 0.85rem;'>No recommendations available.</p>";
          return;
        }
        recContainer.innerHTML = `
            <div style="display: flex; flex-direction: column; gap: 0.75rem;">
                ${products.map(prod => {
                    const imgUrl = prod.imageUrl ? prod.imageUrl : "";
                    const productName = prod.name ? prod.name : "";
                    return `
                        <div class="mini-rec-card" style="display: flex; align-items: center; gap: 0.85rem; padding: 0.65rem; border: 1px solid var(--border); border-radius: 8px; background: var(--card-bg);">
                            <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(productName)}" style="width: 72px; height: 72px; object-fit: cover; border-radius: 6px; background: #eee; flex-shrink: 0;">
                            <div style="flex: 1; min-width: 0;">
                                <a href="product-detail.jsp?id=${prod.id}" style="display: block; font-weight: 600; font-size: 0.875rem; color: var(--text); text-decoration: none; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 0.2rem;">
                                    ${escapeHtml(productName)}
                                </a>
                                <div style="font-weight: 700; color: var(--primary); font-size: 0.875rem; margin-bottom: 0.4rem;">
                                    ${formatMoney(prod.price)}
                                </div>
                                <button class="addRecBtn" data-id="${prod.id}" style="padding: 0.3rem 0.65rem; font-size: 0.775rem; background: var(--primary); color: #fff; border: none; border-radius: 4px; cursor: pointer; font-weight: 500;">
                                    + Add to Cart
                                </button>
                            </div>
                        </div>
                    `;
                }).join("")}
            </div>
        `;
    recContainer.querySelectorAll(".addRecBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const targetBtn = e.currentTarget;
            const id = targetBtn.dataset.id;
            const originalText = "+ Add to Cart";
                targetBtn.textContent = "Adding...";
                targetBtn.disabled = true;
                try {
                    await api.post("/cart", { productId: parseInt(id, 10), quantity: 1 });
                    if (!buyNowId) await loadCart();
                    targetBtn.textContent = "Added!";
                    setTimeout(() => {
                        targetBtn.textContent = originalText;
                        targetBtn.disabled = false;
                    }, 1000);
                } catch (err) {
                    alert("Could not add item: " + (err.message || err));
                    targetBtn.textContent = originalText;
                    targetBtn.disabled = false;
                }
            });
        });
    } catch (err) {
        recContainer.innerHTML = "<p style='color: var(--muted); font-size: 0.85rem;'>Recommendations unavailable</p>";
    }
}
function wireItemButtons() {
 document.querySelectorAll(".updateQtyBtn").forEach(btn => {
 btn.addEventListener("click", async (e) => {
  const row = e.target.closest(".cart-item");
  const productId = row.dataset.productId;
  const qty = parseInt(row.querySelector(".qtyInput").value, 10);
    if (isNaN(qty) || qty < 1) {
      alert("Quantity must be at least 1");
        return;
    }
    try {
        await api.put("/cart/" + productId, { quantity: qty });
         loadCart();
    } catch (err) {
    const msg = document.getElementById("checkoutMessage");
        if (msg) msg.textContent = err.message;
        }
      });
    });
 document.querySelectorAll(".removeBtn").forEach(btn => {
 btn.addEventListener("click", async (e) => {
  const row = e.target.closest(".cart-item");
  const productId = row.dataset.productId;
    try {
        await api.del("/cart/" + productId);
        loadCart();
    } catch (err) {
  const msg = document.getElementById("checkoutMessage");
        if (msg) msg.textContent = err.message;
        }
    });
 });
}
const checkoutBtn = document.getElementById("checkoutBtn");
if (checkoutBtn) {
 checkoutBtn.addEventListener("click", async () => {
 const msg = document.getElementById("checkoutMessage");
  if (msg) msg.textContent = "";
 const addrInput = document.getElementById("shippingAddress");
 const shippingAddress = addrInput ? addrInput.value.trim() : "";
  if (!shippingAddress) {
  if (msg) {
    msg.style.color = "var(--error)";
    msg.textContent = "Please enter a delivery address.";
 }
  if (addrInput) addrInput.focus();
    return;
    }
    try {
        checkoutBtn.disabled = true;
        checkoutBtn.textContent = "Processing...";
    const payload = {
        mockPaymentConfirmed: true,
        shippingAddress: shippingAddress
    };
    if (window.directCheckoutItem) {
        payload.directProductId = window.directCheckoutItem.id;
        payload.directQuantity = window.directCheckoutItem.quantity;
        }
    const order = await api.post("/orders/checkout", payload);
     isOrderPlaced = true;
        if (msg) {
            msg.style.color = "var(--primary)";
            msg.textContent = order.message || "Order placed successfully!";
        }
        if (addrInput) addrInput.value = "";
         setTimeout(() => {
            window.location.href = "orders.jsp";
        }, 1000);
    } catch (err) {
        if (msg) {
            msg.style.color = "var(--error)";
            msg.textContent = err.message;
        }
        checkoutBtn.disabled = false;
        checkoutBtn.textContent = "Place Order";
    }
  });
}
loadCart();
loadRecommendations();