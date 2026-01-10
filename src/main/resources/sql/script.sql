CREATE TABLE brand (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(120) NOT NULL UNIQUE,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(120) NOT NULL,
                          slug VARCHAR(160) NOT NULL UNIQUE,
                          parent_id BIGINT REFERENCES category(id),
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_category_parent ON category(parent_id);

CREATE TABLE product (
                         id BIGSERIAL PRIMARY KEY,
                         sku VARCHAR(64) NOT NULL UNIQUE,
                         name VARCHAR(200) NOT NULL,
                         description TEXT,
                         price NUMERIC(12,2) NOT NULL CHECK (price >= 0),
                         discount_percent NUMERIC(5,2) DEFAULT 0 CHECK (discount_percent >= 0 AND discount_percent <= 100),
                         stock INT NOT NULL DEFAULT 0,
                         brand_id BIGINT REFERENCES brand(id),
                         rating NUMERIC(3,2) DEFAULT 0 CHECK (rating >= 0 AND rating <= 5),
                         active BOOLEAN NOT NULL DEFAULT TRUE,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_product_active ON product(active);
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_product_price ON product(price);
CREATE INDEX idx_product_created ON product(created_at);

CREATE TABLE product_category (
                                  product_id BIGINT REFERENCES product(id) ON DELETE CASCADE,
                                  category_id BIGINT REFERENCES category(id) ON DELETE CASCADE,
                                  PRIMARY KEY (product_id, category_id)
);

CREATE TABLE product_image (
                               id BIGSERIAL PRIMARY KEY,
                               product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                               url TEXT NOT NULL,
                               alt TEXT
);
CREATE INDEX idx_product_image_product ON product_image(product_id);

CREATE TABLE product_attribute (
                                   id BIGSERIAL PRIMARY KEY,
                                   product_id BIGINT NOT NULL REFERENCES product(id) ON DELETE CASCADE,
                                   attr_key VARCHAR(100) NOT NULL,
                                   attr_value VARCHAR(255) NOT NULL
);
CREATE INDEX idx_attr_key_value ON product_attribute(attr_key, attr_value);
CREATE INDEX idx_attr_product ON product_attribute(product_id);
