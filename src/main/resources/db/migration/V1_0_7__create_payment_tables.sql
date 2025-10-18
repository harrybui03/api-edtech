-- Create PayOS Config and Transaction tables for payment functionality

-- 0. CREATE SEQUENCE for order_code
CREATE SEQUENCE IF NOT EXISTS order_code_seq START WITH 1 INCREMENT BY 1;

-- 1. CREATE PAYOS_CONFIGS TABLE
CREATE TABLE payos_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    instructor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    client_id VARCHAR(255) NOT NULL,
    api_key VARCHAR(255) NOT NULL,
    checksum_key VARCHAR(255) NOT NULL,
    account_number VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Unique constraint: one active config per instructor
    CONSTRAINT unique_active_instructor_config UNIQUE (instructor_id, is_active) DEFERRABLE INITIALLY DEFERRED
);

-- 2. CREATE TRANSACTIONS TABLE
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_code BIGINT UNIQUE NOT NULL DEFAULT nextval('order_code_seq'),
    student_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    instructor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID REFERENCES courses(id) ON DELETE CASCADE,
    batch_id UUID REFERENCES batch(id) ON DELETE CASCADE,
    payment_id VARCHAR(255),
    payment_url TEXT,
    account_number VARCHAR(255),
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    description TEXT,
    return_url TEXT,
    cancel_url TEXT,
    paid_at TIMESTAMPTZ,
    failed_at TIMESTAMPTZ,
    webhook_received BOOLEAN DEFAULT FALSE,
    webhook_signature VARCHAR(500),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    
    -- Check constraint for status values
    CONSTRAINT check_transaction_status CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED'))
    ,
    CONSTRAINT chk_transactions_course_or_batch CHECK (
        (CASE WHEN course_id IS NOT NULL THEN 1 ELSE 0 END)
      + (CASE WHEN batch_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    )
);

-- 3. ADD INDEXES for performance
CREATE INDEX idx_payos_configs_instructor ON payos_configs (instructor_id);
CREATE INDEX idx_payos_configs_active ON payos_configs (is_active) WHERE is_active = TRUE;

CREATE INDEX idx_transactions_order_code ON transactions (order_code);
CREATE INDEX idx_transactions_student ON transactions (student_id);
CREATE INDEX idx_transactions_instructor ON transactions (instructor_id);
CREATE INDEX idx_transactions_course ON transactions (course_id);
CREATE INDEX idx_transactions_batch ON transactions (batch_id);
CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
CREATE INDEX idx_transactions_payment_id ON transactions (payment_id) WHERE payment_id IS NOT NULL;

-- 5. ADD COMMENTS for documentation
COMMENT ON TABLE payos_configs IS 'PayOS configuration for instructors to receive payments';
COMMENT ON COLUMN payos_configs.instructor_id IS 'Reference to the instructor user';
COMMENT ON COLUMN payos_configs.client_id IS 'PayOS client ID';
COMMENT ON COLUMN payos_configs.api_key IS 'PayOS API key';
COMMENT ON COLUMN payos_configs.checksum_key IS 'PayOS checksum key for webhook verification';
COMMENT ON COLUMN payos_configs.account_number IS 'PayOS account number for receiving payments';
COMMENT ON COLUMN payos_configs.is_active IS 'Whether this config is currently active';

COMMENT ON TABLE transactions IS 'Payment transactions for course or batch enrollments';
COMMENT ON COLUMN transactions.order_code IS 'Auto-incrementing numeric order code for the transaction';
COMMENT ON COLUMN transactions.student_id IS 'Reference to the student user';
COMMENT ON COLUMN transactions.instructor_id IS 'Reference to the instructor user';
COMMENT ON COLUMN transactions.course_id IS 'Reference to the course being purchased (nullable)';
COMMENT ON COLUMN transactions.batch_id IS 'Reference to the batch being purchased (nullable)';
COMMENT ON COLUMN transactions.payment_id IS 'PayOS payment ID';
COMMENT ON COLUMN transactions.payment_url IS 'PayOS payment URL for student to complete payment';
COMMENT ON COLUMN transactions.account_number IS 'PayOS account number used for this transaction';
COMMENT ON COLUMN transactions.amount IS 'Transaction amount';
COMMENT ON COLUMN transactions.currency IS 'Transaction currency (default VND)';
COMMENT ON COLUMN transactions.status IS 'Transaction status: PENDING, PAID, FAILED, CANCELLED';
COMMENT ON COLUMN transactions.description IS 'Transaction description';
COMMENT ON COLUMN transactions.return_url IS 'URL to redirect after successful payment';
COMMENT ON COLUMN transactions.cancel_url IS 'URL to redirect after cancelled payment';
COMMENT ON COLUMN transactions.paid_at IS 'Timestamp when payment was completed';
COMMENT ON COLUMN transactions.failed_at IS 'Timestamp when payment failed';
COMMENT ON COLUMN transactions.webhook_received IS 'Whether webhook from PayOS was received';
COMMENT ON COLUMN transactions.webhook_signature IS 'Webhook signature for verification';
