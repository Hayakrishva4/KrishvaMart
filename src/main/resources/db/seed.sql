-- KrishvaMart seed data
-- NOTE: Seed users (admin/seller/buyer) are inserted by
-- com.krishva.krishvamart.util.DbSeeder (run once after schema.sql) because
-- their passwords must go through jBCrypt at insert time -- a raw INSERT
-- here would either store a plaintext password or an unverifiable hash,
-- which section 2 (engineering rules) forbids. Run:
--   mvn compile exec:java -Dexec.mainClass="com.krishva.krishvamart.util.DbSeeder"
-- before applying this file. This file only seeds data with no password
-- material, and assumes the seller ids created by DbSeeder are 2 and 3.

INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url) VALUES
  (2, 'Wireless Mouse',        'Ergonomic 2.4GHz wireless mouse with USB receiver.',        699.00,  50, 'Electronics', 'https://picsum.photos/seed/mouse/400'),
  (2, 'Mechanical Keyboard',   '87-key hot-swappable mechanical keyboard, blue switches.',  2499.00, 30, 'Electronics', 'https://picsum.photos/seed/keyboard/400'),
  (2, 'USB-C Hub',             '7-in-1 USB-C hub with HDMI, SD card and PD passthrough.',    1299.00, 40, 'Electronics', 'https://picsum.photos/seed/hub/400'),
  (3, 'Cotton T-Shirt',        'Breathable 100% cotton crew-neck t-shirt.',                   499.00, 100, 'Apparel',     'https://picsum.photos/seed/tshirt/400'),
  (3, 'Denim Jacket',          'Classic fit unisex denim jacket.',                           1899.00,  25, 'Apparel',     'https://picsum.photos/seed/jacket/400'),
  (3, 'Running Shoes',         'Lightweight breathable running shoes.',                      2199.00,  35, 'Apparel',     'https://picsum.photos/seed/shoes/400'),
  (2, 'Stainless Steel Bottle','1L double-wall insulated stainless steel water bottle.',      599.00,  60, 'Home',        'https://picsum.photos/seed/bottle/400'),
  (3, 'Desk Lamp',             'LED desk lamp with adjustable brightness and color temp.',    899.00,  45, 'Home',        'https://picsum.photos/seed/lamp/400');
