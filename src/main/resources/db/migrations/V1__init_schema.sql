CREATE TABLE IF NOT EXISTS users 
 (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(120)  NOT NULL,
    email         VARCHAR(180)  NOT NULL,
    password_hash VARCHAR(100)  NOT NULL,
    role          VARCHAR(10)   NOT NULL CHECK (role IN ('BUYER','SELLER','ADMIN')),
    created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
 );
CREATE TABLE IF NOT EXISTS products 
 (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id   BIGINT         NOT NULL,
    name        VARCHAR(160)   NOT NULL,
    description VARCHAR(2000),
    price       DECIMAL(10,2)  NOT NULL CHECK (price >= 0),
    stock_qty   INT            NOT NULL DEFAULT 0 CHECK (stock_qty >= 0),
    category    VARCHAR(80)    NOT NULL,
    image_url   VARCHAR(500),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_seller FOREIGN KEY (seller_id) REFERENCES users(id)
 );
CREATE INDEX IF NOT EXISTS idx_products_seller ON products(seller_id);
CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE TABLE IF NOT EXISTS orders 
 (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    buyer_id     BIGINT         NOT NULL,
    status       VARCHAR(12)    NOT NULL DEFAULT 'PENDING'
    CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    total_amount DECIMAL(10,2)  NOT NULL CHECK (total_amount >= 0),
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_buyer FOREIGN KEY (buyer_id) REFERENCES users(id)
 );
CREATE INDEX IF NOT EXISTS idx_orders_buyer ON orders(buyer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE TABLE IF NOT EXISTS order_items 
 (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id   BIGINT         NOT NULL,
    product_id BIGINT         NOT NULL,
    quantity   INT            NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2)  NOT NULL CHECK (unit_price >= 0),
    created_at TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orderitems_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_orderitems_product FOREIGN KEY (product_id) REFERENCES products(id)
 );
CREATE INDEX IF NOT EXISTS idx_orderitems_order ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_orderitems_product ON order_items(product_id);
CREATE TABLE IF NOT EXISTS cart_items 
 (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    product_id BIGINT      NOT NULL,
    quantity   INT         NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cartitems_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_cartitems_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT uq_cart_user_product UNIQUE (user_id, product_id)
 );
CREATE INDEX IF NOT EXISTS idx_cartitems_user ON cart_items(user_id);
CREATE INDEX IF NOT EXISTS idx_cartitems_product ON cart_items(product_id);
CREATE TABLE IF NOT EXISTS reviews 
 (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    order_id   BIGINT      NOT NULL,
    rating     INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment    VARCHAR(2000),
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_reviews_order FOREIGN KEY (order_id) REFERENCES orders(id)
 );
CREATE INDEX IF NOT EXISTS idx_reviews_product ON reviews(product_id);
CREATE INDEX IF NOT EXISTS idx_reviews_user ON reviews(user_id);