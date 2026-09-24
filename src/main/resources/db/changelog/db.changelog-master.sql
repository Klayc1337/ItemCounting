--liquibase formatted sql

--changeset daniil:001-create-invoice-enums splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invoice_type') THEN
        CREATE TYPE invoice_type AS ENUM ('ARRIVAL', 'SHIPMENT');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'invoice_status') THEN
        CREATE TYPE invoice_status AS ENUM ('DRAFT', 'COMPLETED', 'CANCELLED');
    END IF;
END
$$;

--changeset daniil:017-update-invoice-types splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
ALTER TYPE invoice_type ADD VALUE IF NOT EXISTS 'TRANSFER';
EXCEPTION
    WHEN duplicate_object THEN null;
END
$$;

--changeset daniil:018-update-invoice-statuses splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'CONFIRMED';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'DELIVERING';
ALTER TYPE invoice_status ADD VALUE IF NOT EXISTS 'RECEIVED';
EXCEPTION
    WHEN duplicate_object THEN null;
END
$$;

--changeset daniil:020-create-store-request-status-enum splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'request_status') THEN
CREATE TYPE request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED');
END IF;
END
$$;

--changeset daniil:002-create-product-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) UNIQUE NOT NULL,
    unit VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset daniil:003-create-stock-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS stock (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(15, 3) NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_product UNIQUE (product_id),
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
);

--changeset daniil:004-create-invoice-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS invoice (
    id BIGSERIAL PRIMARY KEY,
    type invoice_type NOT NULL,
    status invoice_status NOT NULL DEFAULT 'DRAFT',
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset daniil:005-create-invoice-item-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS invoice_item (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(15, 3) NOT NULL CHECK (quantity > 0),
    price NUMERIC(15, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
);

--changeset daniil:006-create-user-coeff-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS user_coeff (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(50) UNIQUE NOT NULL,
    coefficient NUMERIC(10, 4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset daniil:007-create-outbox-event-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS outbox_event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(255) NOT NULL,
    json_event TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    sent_at TIMESTAMP
);

--changeset daniil:008-create-delivery-time-coefficient-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS delivery_time_coefficient (
    id BIGSERIAL PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    coefficient NUMERIC(10, 4) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset daniil:009-add-stock-product-unique-constraint splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_stock_product'
    ) THEN
        ALTER TABLE stock ADD CONSTRAINT uq_stock_product UNIQUE (product_id);
    END IF;
END
$$;

--changeset daniil:010-create-user-role-enum splitStatements:false
--preconditions onFail:MARK_RAN
DO $$
BEGIN
IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
CREATE TYPE user_role AS ENUM ('ADMIN', 'WAREHOUSE_MANAGER', 'STORE_MANAGER');
END IF;
END
$$;

--changeset daniil:013-create-warehouse-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS warehouse (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address TEXT,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CENTRAL', 'STORE')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

--changeset daniil:010-create-users-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL,
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--changeset daniil:011-create-store-request-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS store_request (
    id BIGSERIAL PRIMARY KEY,
    from_warehouse_id BIGINT NOT NULL,
    to_warehouse_id BIGINT NOT NULL,
    status request_status NOT NULL DEFAULT 'PENDING',
    requested_by_user_id BIGINT NOT NULL,
    approved_by_user_id BIGINT,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_from_warehouse FOREIGN KEY (from_warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_to_warehouse FOREIGN KEY (to_warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_requested_by FOREIGN KEY (requested_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_request_approved_by FOREIGN KEY (approved_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

--changeset daniil:012-create-store-request-item-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS store_request_item (
    id BIGSERIAL PRIMARY KEY,
    request_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    requested_quantity NUMERIC(15, 3) NOT NULL CHECK (requested_quantity > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_request_item_request FOREIGN KEY (request_id) REFERENCES store_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT
);



--changeset daniil:014-create-warehouse-location-table
--preconditions onFail:MARK_RAN
CREATE TABLE IF NOT EXISTS warehouse_location (
    id BIGSERIAL PRIMARY KEY,
    warehouse_id BIGINT NOT NULL,
    rack VARCHAR(50) NOT NULL,
    shelf VARCHAR(50) NOT NULL,
    max_width NUMERIC(10, 2),
    max_height NUMERIC(10, 2),
    max_depth NUMERIC(10, 2),
    max_weight NUMERIC(10, 2),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_location_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE CASCADE,
    CONSTRAINT uq_warehouse_location UNIQUE (warehouse_id, rack, shelf)
);

--changeset daniil:015-add-product-dimensions
--preconditions onFail:MARK_RAN
ALTER TABLE product
ADD COLUMN IF NOT EXISTS width NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS height NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS depth NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS weight NUMERIC(10, 2),
ADD COLUMN IF NOT EXISTS price NUMERIC(15, 2),
ADD COLUMN IF NOT EXISTS min_stock_level NUMERIC(15, 3) DEFAULT 0;

--changeset daniil:016-add-stock-warehouse-and-location
--preconditions onFail:MARK_RAN
ALTER TABLE stock
DROP CONSTRAINT IF EXISTS uq_stock_product,
ADD COLUMN IF NOT EXISTS warehouse_id BIGINT,
ADD COLUMN IF NOT EXISTS location_id BIGINT,
ADD CONSTRAINT fk_stock_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
ADD CONSTRAINT fk_stock_location FOREIGN KEY (location_id) REFERENCES warehouse_location(id) ON DELETE SET NULL,
ADD CONSTRAINT uq_stock_product_warehouse_location UNIQUE (product_id, warehouse_id, location_id);

--changeset daniil:019-add-invoice-warehouse-fields
--preconditions onFail:MARK_RAN
ALTER TABLE invoice
ADD COLUMN IF NOT EXISTS supplier_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS from_warehouse_id BIGINT,
ADD COLUMN IF NOT EXISTS to_warehouse_id BIGINT,
ADD COLUMN IF NOT EXISTS created_by_user_id BIGINT,
ADD COLUMN IF NOT EXISTS confirmed_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS confirmed_by_user_id BIGINT,
ADD CONSTRAINT fk_invoice_from_warehouse FOREIGN KEY (from_warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
ADD CONSTRAINT fk_invoice_to_warehouse FOREIGN KEY (to_warehouse_id) REFERENCES warehouse(id) ON DELETE RESTRICT,
ADD CONSTRAINT fk_invoice_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_invoice_confirmed_by FOREIGN KEY (confirmed_by_user_id) REFERENCES users(id) ON DELETE SET NULL;

