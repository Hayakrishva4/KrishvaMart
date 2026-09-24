async function loadAdminData() {
    try {
        const users = await api.get("/admin/users");
        document.getElementById("adminUsers").innerHTML = users.map(u => `
         <div class="user-row">
            ${escapeHtml(u.name)} &lt;${escapeHtml(u.email)}&gt; &mdash; <strong>${escapeHtml(u.role)}</strong>
         </div>
        `).join("") || "<p>No users.</p>";
        const orders = await api.get("/admin/orders");
        document.getElementById("adminOrders").innerHTML = orders.map(o => `
         <div class="order-card">
           Order #${o.id} &mdash; <span class="status-badge">${escapeHtml(o.status)}</span>
            &mdash; ${formatMoney(o.totalAmount)}
         </div>
        `).join("") || "<p>No orders.</p>";
    } catch (err) {
      document.getElementById("adminUsers").innerHTML = "<p style='color: var(--error);'>" + escapeHtml(err.message) + "</p>";
    }
}
document.getElementById("moderateForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const msg = document.getElementById("moderateMessage");
    const productId = document.getElementById("moderateProductId").value;   
    msg.textContent = "Processing...";
    msg.style.color = "var(--text)";
    try {
     await api.del("/admin/products/" + productId);
      msg.style.color = "var(--primary)"; 
      msg.textContent = "Listing deactivated successfully.";
      document.getElementById("moderateProductId").value = ""; 
    } catch (err) {
       msg.style.color = "var(--error)";
       msg.textContent = err.message;
    }
});
const activateForm = document.getElementById("activateForm");
if (activateForm) {
    activateForm.addEventListener("submit", async (e) => {
     e.preventDefault();
      const msg = document.getElementById("activateMessage");
      const productId = document.getElementById("activateProductId").value;     
        msg.textContent = "Processing...";
        msg.style.color = "var(--text)";
        try {
            await api.put("/admin/products/" + productId + "/activate");
            msg.style.color = "var(--primary)"; 
            msg.textContent = "Listing reactivated successfully.";
            document.getElementById("activateProductId").value = "";
        } catch (err) {
            msg.style.color = "var(--error)";
            msg.textContent = err.message;
        }
    });
}
loadAdminData();