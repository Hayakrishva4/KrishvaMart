function getBaseContext() {
    const pathname = window.location.pathname;
    const knownDirs = ["/jsp/", "/css/", "/js/", "/api/"];
    for (const dir of knownDirs) {
        const idx = pathname.indexOf(dir);
        if (idx !== -1) {
            return pathname.substring(0, idx);
        }
    }
    return "";
}

if (typeof window.API_BASE === "undefined") {
    window.API_BASE = getBaseContext() + "/api/v1";
}
var API_BASE = window.API_BASE;

async function apiRequest(method, path, body) {
    const opts = {
        method,
        headers: { "Content-Type": "application/json" },
        credentials: "same-origin"
    };
    if (body !== undefined) {
        opts.body = JSON.stringify(body);
    }
    const res = await fetch(API_BASE + path, opts);
    let envelope;
    try {
        envelope = await res.json();
    } catch (e) {
        throw new Error("Unexpected server response");
    }
    if (!envelope.success) {
        const message = envelope.error ? envelope.error.message : "Request failed";
        const err = new Error(message);
        err.code = envelope.error ? envelope.error.code : "UNKNOWN";
        err.status = res.status;
        throw err;
    }
    return envelope.data;
}

var api = window.api || {
    get: (path) => apiRequest("GET", path),
    post: (path, body) => apiRequest("POST", path, body),
    put: (path, body) => apiRequest("PUT", path, body),
    patch: (path, body) => apiRequest("PATCH", path, body),
    del: (path) => apiRequest("DELETE", path)
};
window.api = api;

function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

function formatMoney(value) {
    const num = Number(value);
    return "$" + (isNaN(num) ? "0.00" : num.toFixed(2));
}