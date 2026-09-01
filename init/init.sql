DO $$ BEGIN
CREATE TYPE invoice_type AS ENUM ('ARRIVAL', 'SHIPMENT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'COMPLETED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;




CREATE TABLE IF NOT EXISTS product (
                                       id          BIGSERIAL PRIMARY KEY,
                                       name        VARCHAR(255) NOT NULL,
    sku         VARCHAR(100) UNIQUE NOT NULL,
    unit        VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );




CREATE TABLE IF NOT EXISTS stock (
                                     id          BIGSERIAL PRIMARY KEY,
                                     product_id  BIGINT NOT NULL,
                                     quantity    NUMERIC(15, 3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
    );



CREATE TABLE IF NOT EXISTS invoice (
                                       id          BIGSERIAL PRIMARY KEY,
                                       type        invoice_type NOT NULL,
                                       status      invoice_status NOT NULL DEFAULT 'DRAFT',
                                       comment     TEXT,
                                       created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );



CREATE TABLE IF NOT EXISTS invoice_item (
                                            id          BIGSERIAL PRIMARY KEY,
                                            invoice_id  BIGINT NOT NULL,
                                            product_id  BIGINT NOT NULL,
                                            quantity    NUMERIC(15, 3) NOT NULL CHECK (quantity > 0),
    price       NUMERIC(15, 2),
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id) on delete cascade,
    CONSTRAINT fk_invoice_item_product FOREIGN KEY (product_id) REFERENCES product(id) on delete restrict
    );

CREATE TABLE IF NOT EXISTS user_coeff (
    id          BIGSERIAL PRIMARY KEY,
    group_name  VARCHAR(50) UNIQUE NOT NULL,
    coefficient NUMERIC(10, 4) NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at  TIMESTAMP WITH TIME ZONE DEFAULT NOW()
    );
