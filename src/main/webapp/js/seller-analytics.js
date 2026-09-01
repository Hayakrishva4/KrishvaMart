async function loadSalesSummary() {
    const summaryEl = document.getElementById("salesSummary");
    const byProductEl = document.getElementById("salesByProduct");
    try {
        const summary = await api.get("/seller/analytics");
        summaryEl.innerHTML = `
            <div class="stat-card">
                <div class="stat-value">${summary.totalOrders}</div>
                <div class="stat-label">Delivered orders</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${summary.totalUnitsSold}</div>
                <div class="stat-label">Units sold</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${formatMoney(summary.totalRevenue)}</div>
                <div class="stat-label">Total revenue</div>
            </div>
        `;
        if (summary.byProduct.length === 0) {
            byProductEl.innerHTML = "<p>No sales yet.</p>";
            return;
        }
        byProductEl.innerHTML = `
            <table class="sales-table">
                <thead><tr><th>Product</th><th>Units sold</th><th>Revenue</th></tr></thead>
                <tbody>
                    ${summary.byProduct.map(p => `
                        <tr>
                            <td>${escapeHtml(p.productName)}</td>
                            <td>${p.unitsSold}</td>
                            <td>${formatMoney(p.revenue)}</td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
    } catch (err) {
        summaryEl.innerHTML = "<p>" + escapeHtml(err.message) + "</p>";
    }
}

loadSalesSummary();
