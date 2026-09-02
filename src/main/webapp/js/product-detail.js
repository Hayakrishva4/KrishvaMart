document.addEventListener('DOMContentLoaded', () => 
{
    const params = new URLSearchParams(window.location.search);
    const productId = params.get('id');
    const detailContainer = document.getElementById('productDetail');
    const reviewList = document.getElementById('reviewList');
    const reviewForm = document.getElementById('reviewForm');
    const reviewError = document.getElementById('reviewError');
    const ratingSummary = document.getElementById('ratingSummary');

    if (!productId)
    {
     if (detailContainer)
     {
       detailContainer.innerHTML = '<p class="error-text">No product ID specified.</p>';
     }
      return;
    }
    fetch('/api/v1/products/' + productId)
     .then(res => 
        {
            if (!res.ok && res.status !== 404) 
            {
              return fetch('/api/v1/products?id=' + productId).then(r => r.json());
            }
            return res.json();
        })
        .then(res => 
        {
            if (!res.success || !res.data) 
            {
                throw new Error(res.error ? res.error.message : 'Product not found');
            }
            renderProduct(res.data);
          if (window.RecentlyViewed && typeof window.RecentlyViewed.push === 'function') 
          {
            window.RecentlyViewed.push(res.data);
          }
        })
        .catch(err => 
        {
         if (detailContainer)
         {
          detailContainer.innerHTML = '<p class="error-text">' + (err.message || 'Failed to load product details.') + '</p>';
         }
        });
    fetchReviews(productId);
    function renderProduct(p) {
        if (!detailContainer) return;
        const inStock = p.stockQty > 0;
        const stockBadge = inStock 
            ? '<span class="badge in-stock">In Stock (' + p.stockQty + ' available)</span>'
            : '<span class="badge out-of-stock">Out of Stock</span>';
        detailContainer.innerHTML = 
            '<div class="product-card-detail">' +
                '<div class="product-image">' +
                    '<img src="' + escapeHtml(p.imageUrl || 'https://via.placeholder.com/400') + '" alt="' + escapeHtml(p.name) + '">' +
                '</div>' +
                '<div class="product-info">' +
                    '<span class="category-tag">' + escapeHtml(p.category || 'General') + '</span>' +
                    '<h1>' + escapeHtml(p.name) + '</h1>' +
                    '<p class="price">\u20B9' + Number(p.price).toFixed(2) + '</p>' +
                    '<div class="stock-status">' + stockBadge + '</div>' +
                    '<p class="description">' + escapeHtml(p.description || '') + '</p>' +
                    '<div class="actions">' +
                        '<input type="number" id="quantityInput" value="1" min="1" max="' + p.stockQty + '" ' + (!inStock ? 'disabled' : '') + '>' +
                        '<button id="addToCartBtn" class="btn btn-primary" ' + (!inStock ? 'disabled' : '') + '>Add to Cart</button>' +
                    '</div>' +
                    '<p id="cartFeedback" class="feedback-msg"></p>' +
                '</div>' +
            '</div>';
        const addToCartBtn = document.getElementById('addToCartBtn');
        if (addToCartBtn) 
        {
            addToCartBtn.addEventListener('click', () => 
            {
             const qty = parseInt(document.getElementById('quantityInput').value, 10) || 1;
                addToCart(p.id, qty);
            });
        }
    }
    function addToCart(prodId, quantity) 
    {
        const feedback = document.getElementById('cartFeedback');
        if (feedback) feedback.textContent = 'Adding to cart...';
        fetch('/api/v1/cart', 
        {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ productId: prodId, quantity: quantity })
        })
        .then(res => res.json())
        .then(res => 
        {
         if (feedback)
         {
          feedback.textContent = res.success ? 'Added to cart!' : (res.error ? res.error.message : 'Failed to add.');
         }
        })
        .catch(() => 
        {
          if (feedback) feedback.textContent = 'Please log in to add items.';
        });
    }
    function fetchReviews(pId) 
    {
        if (!reviewList) return;
        fetch('/api/v1/reviews/product/' + pId)
            .then(res => res.json())
            .then(res => 
            {
                if (res.success && res.data) 
                {
                    const { reviews, averageRating } = res.data;
                    renderReviews(reviews || [], averageRating || 0);
                }
            })
            .catch(() => {});
    }
    function renderReviews(reviews, avgRating) 
    {
        if (!reviewList) return;
        const roundedRating = Math.round(avgRating);
        const stars = '\u2605'.repeat(roundedRating) + '\u2606'.repeat(5 - roundedRating);
        if (ratingSummary) 
        {
         ratingSummary.innerHTML = '<strong>Average Rating: <span class="stars">' + stars + '</span> (' 
         + (avgRating ? avgRating.toFixed(1) : '0.0') + ' / 5.0)</strong> \u2014 ' + reviews.length + ' review(s)';
        }
        if (reviews.length === 0) 
        {
         reviewList.innerHTML = '<p class="muted-text">No reviews yet for this product.</p>';
            return;
        }
        reviewList.innerHTML = reviews.map(r => 
         '<div class="review-item">' +
            '<div class="review-header">' +
              '<span class="stars">' + '\u2605'.repeat(r.rating) + '\u2606'.repeat(5 - r.rating) + '</span>' +
                '<small class="muted-text">' + (r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '') + '</small>' +
            '</div>' + '<p>' + escapeHtml(r.comment || '') + '</p>' + '</div>').join('');
    }
    if (reviewForm) {
        reviewForm.classList.remove('hidden');
        reviewForm.addEventListener('submit', (e) => {
            e.preventDefault();
            if (reviewError) reviewError.textContent = 'Submitting...';
            const orderIdInput = document.getElementById('orderIdForReview');
            const ratingInput = document.getElementById('rating');
            const commentInput = document.getElementById('comment');
            fetch('/api/v1/reviews', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    productId: parseInt(productId, 10),
                    orderId: parseInt(orderIdInput.value, 10),
                    rating: parseInt(ratingInput.value, 10),
                    comment: commentInput ? commentInput.value.trim() : ''
                })
            })
            .then(res => res.json())
            .then(res => {
                if (res.success) {
                    reviewForm.reset();
                    if (reviewError) reviewError.textContent = 'Review submitted!';
                    fetchReviews(productId);
                } else {
                    if (reviewError) reviewError.textContent = res.error ? res.error.message : 'Failed to submit.';
                }
            })
            .catch(() => {
                if (reviewError) reviewError.textContent = 'Login required or invalid order ID.';
            });
        });
    }
    function escapeHtml(text) 
    {
     if (!text) return '';
      const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
     return String(text).replace(/[&<>"']/g, m => map[m]);
    }
});
