-- Seed PayOS config for a known instructor and a sample paid batch with instructor mapping

DO $$
DECLARE
    v_instructor_id UUID;
    v_batch_id UUID;
BEGIN
    -- Resolve instructor id by known email from initial seed (V1_0_1__seed_data.sql)
    SELECT id INTO v_instructor_id FROM users WHERE email = 'duckie010203@gmail.com' LIMIT 1;

    -- Seed PayOS config if not exists
    IF v_instructor_id IS NOT NULL THEN
        IF NOT EXISTS (
            SELECT 1 FROM payos_configs WHERE instructor_id = v_instructor_id AND is_active = TRUE
        ) THEN
            INSERT INTO payos_configs (instructor_id, client_id, api_key, checksum_key, account_number, is_active)
            VALUES (
                v_instructor_id,
                '92e19512-0d67-40c3-bb75-3c1e8510588f',
                'aae26376-757b-4fcb-a226-4ae1baaa23f9',
                '8c39881aa3a16ed8c73c451a23595e81738d886df4d8836f43fdf71ce1d18d22',
                '0985 8304 76',
                TRUE
            );
        END IF;
    END IF;

    -- Seed a sample paid batch if not exists
    IF NOT EXISTS (SELECT 1 FROM batch WHERE slug = 'spring-boot-cohort-1') THEN
        v_batch_id := uuid_generate_v4();
        INSERT INTO batch (
            id, title, description, slug, paid_batch, actual_price, selling_price, language, status, max_capacity
        ) VALUES (
            v_batch_id,
            'Spring Boot Cohort 1',
            'Cohort-based class for Spring Boot with payments',
            'spring-boot-cohort-1',
            TRUE,
            2000000,
            1500000,
            'vi',
            'PUBLISHED',
            50
        );
    ELSE
        SELECT id INTO v_batch_id FROM batch WHERE slug = 'spring-boot-cohort-1' LIMIT 1;
    END IF;

    -- Map instructor to batch (idempotent)
    IF v_instructor_id IS NOT NULL AND v_batch_id IS NOT NULL THEN
        INSERT INTO batch_instructors (batch_id, instructor_id)
        VALUES (v_batch_id, v_instructor_id)
        ON CONFLICT DO NOTHING;
    END IF;
END $$;


