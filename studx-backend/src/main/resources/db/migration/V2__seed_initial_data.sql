-- V2__seed_initial_data.sql
-- Seed default categories, sellers, and initial marketplace products

-- 1. Seed Categories
INSERT INTO categories (id, name, slug, image_url) VALUES
(1, 'Phones', 'phones', '/CategoryImages/Mobile.png'),
(2, 'Laptops', 'laptops', '/CategoryImages/Laptop.png'),
(3, 'Chairs', 'chairs', '/CategoryImages/Chair.png'),
(4, 'Study Tables', 'study-tables', '/CategoryImages/StudyTable.png'),
(5, 'Utensils', 'utensils', '/CategoryImages/Utensils.png'),
(6, 'Books', 'books', '/CategoryImages/Books.png'),
(7, 'Headphones', 'headphones', '/CategoryImages/Headphones.png'),
(8, 'Gaming', 'gaming', '/CategoryImages/Gaming.png'),
(9, 'Fitness', 'fitness', '/CategoryImages/Fitness.png'),
(10, 'Chargers', 'chargers', '/CategoryImages/Chargers.png')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Seed Default Sellers (Password: 'Password@123' -> $2a$10$wE96Xw4pY.FmX6d0Wk8.ee9H/wz9.wYlU4p8wGvE.8Yg3Oq8z7q2e)
-- Standard BCrypt for 'Password@123': $2a$10$Dow1hG6XbZ5Qp6Pq8Z5Y8e0b6Sg4Xy2L4M8J9K2L1P7Q4R6S9T2U6
INSERT INTO users (id, name, email, password_hash, college, city, is_verified, role) VALUES
(1, 'Ankit Sharma', 'ankit@delhi.edu.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Delhi University', 'Delhi', TRUE, 'ROLE_USER'),
(2, 'Priya Verma', 'priya@mumbai.ac.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Mumbai University', 'Mumbai', TRUE, 'ROLE_USER'),
(3, 'Admin User', 'admin@studx.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Campus Hub', 'Admin City', TRUE, 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Seed Products
INSERT INTO products (id, title, description, price, image_url, dimensions, condition_grade, status, is_featured, is_deal, category_id, seller_id, version) VALUES
(1, 'HP Pavilion Laptop', 'Refurbished HP Pavilion, 8GB RAM, 512GB SSD, ideal for coding.', 28999.00, 'https://images.unsplash.com/photo-1663354027456-ce6a7e07d212?q=80&w=1074&auto=format&fit=crop', '32 x 22 x 2 cm', 'LIKE_NEW', 'AVAILABLE', TRUE, TRUE, 2, 1, 0),
(2, 'Dell Inspiron Laptop', 'Used Dell Inspiron, 16GB RAM, 1TB HDD, great condition.', 25999.00, 'https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?q=80&w=870&auto=format&fit=crop', '36 x 25 x 2.2 cm', 'GOOD', 'AVAILABLE', TRUE, TRUE, 2, 2, 0),
(3, 'Wooden Study Table', 'Refurbished wooden study table with lockable drawer.', 1499.00, 'https://images.unsplash.com/photo-1616400619175-5beda3a17896?q=80&w=1074&auto=format&fit=crop', '90 x 50 x 75 cm', 'GOOD', 'AVAILABLE', FALSE, TRUE, 4, 1, 0),
(4, 'Ergonomic Office Chair', 'High back breathable mesh ergonomic chair with lumbar support.', 3499.00, 'https://images.unsplash.com/photo-1580481077195-c3f8f94625b0?q=80&w=800&auto=format&fit=crop', '60 x 60 x 110 cm', 'LIKE_NEW', 'AVAILABLE', TRUE, FALSE, 3, 2, 0),
(5, 'Engineering Mathematics Vol 1', 'Standard college textbook by B.S. Grewal, minimal markings.', 399.00, 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?q=80&w=800&auto=format&fit=crop', '24 x 18 x 4 cm', 'GOOD', 'AVAILABLE', TRUE, FALSE, 6, 1, 0),
(6, 'Casio FX-991EX Scientific Calculator', 'Non-programmable scientific calculator approved for engineering exams.', 850.00, 'https://images.unsplash.com/photo-1594980596870-8aa52a78d8cd?q=80&w=800&auto=format&fit=crop', '16 x 8 x 1.5 cm', 'LIKE_NEW', 'AVAILABLE', TRUE, TRUE, 1, 2, 0)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 4. Seed Product Features
INSERT INTO product_features (product_id, feature_text, display_order) VALUES
(1, '8GB DDR4 RAM allows seamless multitasking across development tools.', 1),
(1, '512GB SSD ensures fast boot times and rapid compile cycles.', 2),
(1, 'Intel Core i5 10th Gen processor delivering dependable performance.', 3),
(1, '14-inch Full HD anti-glare display for fatigue-free coding.', 4),
(2, '16GB RAM for heavy virtualization and docker containers.', 1),
(2, '1TB HDD providing extensive storage for academic resources.', 2),
(2, '15.6-inch HD display with anti-reflective finish.', 3),
(3, 'Durable engineered wood with smooth laminate finish.', 1),
(3, 'Spacious tabletop with dedicated drawer for books.', 2),
(4, 'Breathable mesh back with adjustable lumbar support.', 1),
(4, 'Smooth rolling nylon casters suitable for hostel rooms.', 2),
(5, 'Comprehensive coverage of calculus and linear algebra.', 1),
(5, 'Clean pages with zero torn leaves and minimal pencil notes.', 2),
(6, 'Natural Textbook Display with high-resolution spreadsheet support.', 1),
(6, 'Dual power system: solar plus battery backup.', 2);
