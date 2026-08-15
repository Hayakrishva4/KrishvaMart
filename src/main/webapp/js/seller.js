async function loadSellerProducts() {
    const container = document.getElementById("sellerProductList");
    try {
        const me = await api.get("/auth/me");
        if (me.role !== "SELLER") {
            container.innerHTML = "<p>Only sellers can manage listings. Log in as a seller to continue.</p>";
            document.getElementById("productForm").classList.add("hidden");
            return;
        }
        const products = await api.get("/products?sellerOnly=true");
        renderList(products);
    } catch (err) {
        container.innerHTML = "<p>" + escapeHtml(err.message) + "</p>";
    }
}

function renderList(products) {
    const container = document.getElementById("sellerProductList");
    if (products.length === 0) {
        container.innerHTML = "<p>You have no listings yet.</p>";
        return;
    }
    container.innerHTML = products.map(p => `
        <div class="listing-row" data-id="${p.id}">
            <strong>${escapeHtml(p.name)}</strong> &mdash; ${formatMoney(p.price)}
            &mdash; ${p.stockQty} in stock &mdash; ${escapeHtml(p.category)}
            <div>
                <button class="editBtn">Edit</button>
                <button class="deleteBtn secondary">Delete</button>
            </div>
        </div>
    `).join("");
    container.querySelectorAll(".editBtn").forEach(btn => {
        btn.addEventListener("click", (e) => {
            const id = e.target.closest(".listing-row").dataset.id;
            const p = products.find(x => String(x.id) === id);
            fillFormForEdit(p);
        });
    });
    container.querySelectorAll(".deleteBtn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const id = e.target.closest(".listing-row").dataset.id;
            if (!confirm("Delete this listing?")) return;
            try {
                await api.del("/products/" + id);
                loadSellerProducts();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}

function fillFormForEdit(p) {
    document.getElementById("editingId").value = p.id;
    document.getElementById("pName").value = p.name;
    document.getElementById("pDescription").value = p.description || "";
    document.getElementById("pPrice").value = p.price;
    document.getElementById("pStock").value = p.stockQty;
    document.getElementById("pCategory").value = p.category;
    document.getElementById("pImageUrl").value = p.imageUrl || "";
    document.getElementById("productSubmitBtn").textContent = "Save changes";
    document.getElementById("productCancelEditBtn").classList.remove("hidden");
}

function resetForm() {
    document.getElementById("productForm").reset();
    document.getElementById("editingId").value = "";
    document.getElementById("productSubmitBtn").textContent = "Add listing";
    document.getElementById("productCancelEditBtn").classList.add("hidden");
}

document.getElementById("productCancelEditBtn").addEventListener("click", resetForm);

document.getElementById("productForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const errorEl = document.getElementById("productFormError");
    errorEl.textContent = "";
    const editingId = document.getElementById("editingId").value;
    const payload = {
        name: document.getElementById("pName").value,
        description: document.getElementById("pDescription").value,
        price: parseFloat(document.getElementById("pPrice").value),
        stockQty: parseInt(document.getElementById("pStock").value, 10),
        category: document.getElementById("pCategory").value,
        imageUrl: document.getElementById("pImageUrl").value
    };
    try {
        if (editingId) {
            await api.put("/products/" + editingId, payload);
        } else {
            await api.post("/products", payload);
        }
        resetForm();
        loadSellerProducts();
    } catch (err) {
        errorEl.textContent = err.message;
    }
});

loadSellerProducts();
