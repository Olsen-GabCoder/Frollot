SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;

CREATE DATABASE IF NOT EXISTS coiffure_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE coiffure_db;

CREATE TABLE users (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    user_type ENUM('client', 'hairstylist', 'salon_owner', 'admin') NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20) UNIQUE,
    is_verified BOOLEAN DEFAULT FALSE,
    verification_type ENUM('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED') NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    email_verification_token VARCHAR(100),
    email_verification_token_expires_at TIMESTAMP NULL,
    email_verification_sent_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    avatar_url VARCHAR(500),
    cover_image_url VARCHAR(500),
    bio TEXT,
    years_experience INT,
    certifications TEXT,
    instagram_handle VARCHAR(100),
    preferred_language VARCHAR(2) DEFAULT 'fr',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    INDEX idx_email (email),
    INDEX idx_user_type (user_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE salons (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    owner_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) DEFAULT 'France',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    phone_number VARCHAR(20),
    email VARCHAR(255),
    website_url VARCHAR(500),
    instagram_handle VARCHAR(100),
    facebook_page VARCHAR(255),
    opening_hours JSON,
    rating_average DECIMAL(3, 2) DEFAULT 0.00,
    total_reviews INT DEFAULT 0,
    is_accepting_walk_ins BOOLEAN DEFAULT TRUE,
    is_premium BOOLEAN DEFAULT FALSE,
    subscription_plan ENUM('free', 'basic', 'premium', 'enterprise') DEFAULT 'free',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_owner (owner_id),
    INDEX idx_slug (slug),
    INDEX idx_city (city),
    INDEX idx_location (latitude, longitude),
    INDEX idx_rating (rating_average),
    FULLTEXT idx_search (name, description, city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE salon_staff (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    specialties JSON,
    is_active BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_salon_user (salon_id, user_id),
    INDEX idx_salon_staff_salon_id (salon_id),
    INDEX idx_salon_staff_user_id (user_id),
    INDEX idx_salon_staff_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE salon_staff_specialties (
    staff_id CHAR(36) NOT NULL,
    specialty VARCHAR(100) NOT NULL,
    FOREIGN KEY (staff_id) REFERENCES salon_staff(id) ON DELETE CASCADE,
    INDEX idx_staff (staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE services (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category ENUM('coupe', 'coloration', 'balayage', 'soins', 'coiffage', 'extensions', 'barbier') NOT NULL,
    duration_minutes INT NOT NULL,
    price_min DECIMAL(10, 2) NOT NULL,
    price_max DECIMAL(10, 2),
    currency VARCHAR(3) DEFAULT 'EUR',
    is_available BOOLEAN DEFAULT TRUE,
    staff_ids JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    INDEX idx_salon (salon_id),
    INDEX idx_category (category),
    INDEX idx_price (price_min)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE salon_services (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category ENUM('COUPE', 'COLORATION', 'SOIN', 'BARBE', 'COIFFAGE', 'TECHNIQUE', 'AUTRE') NOT NULL,
    duration_minutes INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    INDEX idx_salon (salon_id),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE bookings (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    client_id CHAR(36) NOT NULL,
    staff_id CHAR(36),
    service_id CHAR(36) NOT NULL,
    booking_datetime TIMESTAMP NOT NULL,
    duration_minutes INT NOT NULL,
    status ENUM('pending', 'confirmed', 'in_progress', 'completed', 'cancelled', 'no_show') DEFAULT 'pending',
    price_final DECIMAL(10, 2),
    payment_status ENUM('unpaid', 'paid', 'refunded') DEFAULT 'unpaid',
    payment_method VARCHAR(50),
    notes_client TEXT,
    notes_salon TEXT,
    reminder_sent_at TIMESTAMP NULL,
    confirmed_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES salon_staff(id) ON DELETE SET NULL,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    INDEX idx_salon_datetime (salon_id, booking_datetime),
    INDEX idx_client (client_id),
    INDEX idx_staff (staff_id),
    INDEX idx_status (status),
    INDEX idx_datetime (booking_datetime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE waiting_queue (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    client_id CHAR(36) NOT NULL,
    service_id CHAR(36),
    preferred_staff_id CHAR(36),
    position INT NOT NULL,
    estimated_wait_minutes INT,
    status ENUM('waiting', 'called', 'in_service', 'completed', 'cancelled', 'expired') DEFAULT 'waiting',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    notes TEXT,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL,
    FOREIGN KEY (preferred_staff_id) REFERENCES salon_staff(id) ON DELETE SET NULL,
    INDEX idx_salon_status (salon_id, status),
    INDEX idx_position (position),
    INDEX idx_joined_at (joined_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE waiting_queues (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    INDEX idx_salon (salon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE queue_entries (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    queue_id CHAR(36),
    client_id CHAR(36) NOT NULL,
    service_id CHAR(36),
    requested_duration INT DEFAULT 30 NOT NULL,
    status ENUM('WAITING', 'CALLED', 'CANCELLED', 'COMPLETED') DEFAULT 'WAITING' NOT NULL,
    notes TEXT,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP NULL,
    left_at TIMESTAMP NULL,
    FOREIGN KEY (queue_id) REFERENCES waiting_queues(id) ON DELETE CASCADE,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE SET NULL,
    INDEX idx_queue_entry_queue (queue_id),
    INDEX idx_queue_entry_client (client_id),
    INDEX idx_queue_entry_status (status),
    INDEX idx_queue_entry_joined_at (joined_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reviews (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    salon_id CHAR(36) NOT NULL,
    staff_id CHAR(36),
    client_id CHAR(36) NOT NULL,
    booking_id CHAR(36) UNIQUE,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title VARCHAR(255),
    content TEXT,
    response_salon TEXT,
    response_at TIMESTAMP NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    is_visible BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (salon_id) REFERENCES salons(id) ON DELETE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES salon_staff(id) ON DELETE SET NULL,
    FOREIGN KEY (client_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    INDEX idx_salon (salon_id),
    INDEX idx_staff (staff_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE posts (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    author_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    post_type ENUM('GENERAL', 'AVANT_APRES', 'PORTFOLIO', 'TENDANCE', 'CONSEIL', 'REALISATION', 'INSPIRATION') NOT NULL DEFAULT 'GENERAL',
    image_url VARCHAR(500),
    likes_count INT DEFAULT 0 NOT NULL,
    comments_count INT DEFAULT 0 NOT NULL,
    shares_count INT DEFAULT 0 NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE NOT NULL,
    visibility ENUM('PUBLIC', 'FOLLOWERS', 'PRIVATE') DEFAULT 'PUBLIC' NOT NULL,
    is_hidden BOOLEAN DEFAULT FALSE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post_author (author_id),
    INDEX idx_post_created (created_at),
    INDEX idx_post_type (post_type),
    INDEX idx_post_hidden (is_hidden, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comments (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    author_id CHAR(36) NOT NULL,
    content TEXT NOT NULL,
    is_hidden BOOLEAN DEFAULT FALSE NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_author (author_id),
    INDEX idx_created_at (created_at),
    INDEX idx_comment_hidden (is_hidden, is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE post_likes (
    id CHAR(36) PRIMARY KEY DEFAULT (UUID()),
    post_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_post_user (post_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (id, email, password_hash, user_type, first_name, last_name, is_verified) VALUES
('admin-001', 'admin@coiffure.com', '$2a$12$encrypted_password_here', 'admin', 'Admin', 'System', TRUE);

INSERT INTO users (id, email, password_hash, user_type, first_name, last_name, phone_number, is_verified) VALUES
('owner-001', 'sophie@elysee-coiffure.com', '$2a$12$encrypted_password_here', 'salon_owner', 'Sophie', 'Martin', '+33612345678', TRUE);

INSERT INTO salons (id, owner_id, name, slug, description, address, city, postal_code, latitude, longitude, phone_number, email, is_accepting_walk_ins) VALUES
('salon-001', 'owner-001', 'Élysée Coiffure', 'elysee-coiffure-lyon', 'Salon de coiffure haut de gamme au coeur de Lyon', '15 Rue de la République', 'Lyon', '69002', 45.764043, 4.835659, '+33478123456', 'contact@elysee-coiffure.com', TRUE);

INSERT INTO salon_staff (id, salon_id, user_id, role, specialties, bio, years_experience, is_accepting_bookings) VALUES
('staff-001', 'salon-001', 'owner-001', 'owner', '["coupe", "coloration", "balayage"]', 'Passionnée depuis 15 ans', 15, TRUE);

INSERT INTO services (id, salon_id, name, description, category, duration_minutes, price_min, price_max, staff_ids) VALUES
('service-001', 'salon-001', 'Coupe Femme', 'Coupe + Brushing', 'coupe', 45, 45.00, 60.00, '["staff-001"]'),
('service-002', 'salon-001', 'Coloration Complète', 'Coloration racines + longueurs', 'coloration', 120, 80.00, 150.00, '["staff-001"]');

SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;