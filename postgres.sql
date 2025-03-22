-- Xoá toàn bộ schema và tất cả các đối tượng bên trong
DROP SCHEMA public CASCADE;
-- Tạo lại schema public sau khi đã xoá
Create SCHEMA public;
CREATE EXTENSION IF NOT EXISTS unaccent;

-- Bảng thông tin nhân viên cơ bản
Create TABLE employees
(
    employee_id       SERIAL PRIMARY KEY,
    employee_code     text UNIQUE NOT NULL,
    first_name        VARCHAR(50) NOT NULL,
    last_name         VARCHAR(50) NOT NULL,
    picture           bytea,
    birth_date        DATE        NOT NULL,
    gender            VARCHAR(10),
    id_number         text UNIQUE NOT NULL,
    permanent_address TEXT,
    temporary_address TEXT,
    personal_email    VARCHAR(100),
    company_email     text UNIQUE,
    phone_number      VARCHAR(15),
    marital_status    VARCHAR(20),
    bank_account      VARCHAR(50),
    bank_name         VARCHAR(100),
    tax_code          VARCHAR(20),
    Created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted        bool      default false
);

-- Bảng phòng ban (với trường manager_id mới)
Create TABLE departments
(
    department_id   SERIAL PRIMARY KEY,
    department_name VARCHAR(100)       NOT NULL,
    department_code VARCHAR(20) UNIQUE NOT NULL,
    description     TEXT,
    manager_id      INTEGER REFERENCES employees (employee_id),
    Created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng chức vụ
Create TABLE positions
(
    position_id   SERIAL PRIMARY KEY,
    department_id INTEGER REFERENCES departments (department_id),
    position_name VARCHAR(100)       NOT NULL,
    position_code VARCHAR(20) UNIQUE NOT NULL,
    level         INTEGER            NOT NULL,
    description   TEXT,
    Created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng quá trình công tác
Create TABLE employment_history
(
    history_id      SERIAL PRIMARY KEY,
    employee_id     INTEGER REFERENCES employees (employee_id),
    department_id   INTEGER REFERENCES departments (department_id),
    position_id     INTEGER REFERENCES positions (position_id),
    start_date      DATE NOT NULL,
    end_date        DATE,
    is_current      BOOLEAN   DEFAULT true,
    transfer_reason TEXT,
    decision_number VARCHAR(50),
    Created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng hợp đồng lao động
Create TABLE contracts
(
    contract_id   SERIAL PRIMARY KEY,
    employee_id   INTEGER REFERENCES employees (employee_id),
    contract_type VARCHAR(50)        NOT NULL,
    contract_code VARCHAR(20) UNIQUE NOT NULL,
    start_date    DATE               NOT NULL,
    end_date      DATE,
    base_salary   DECIMAL(15, 2)     NOT NULL,
    sign_date     DATE               NOT NULL,
    is_present    bool               not null,
    Created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng phụ cấp của nhân viên
Create TABLE allowances
(
    allowance_id   SERIAL PRIMARY KEY,
    employee_id    INTEGER REFERENCES employees (employee_id),
    allowance_type VARCHAR(50)    NOT NULL,
    amount         DECIMAL(15, 2) NOT NULL,
    start_date     DATE           NOT NULL,
    end_date       DATE,
    description    TEXT,
    Created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng chấm công
Create TABLE attendance
(
    attendance_id  SERIAL PRIMARY KEY,
    employee_id    INTEGER REFERENCES employees (employee_id),
    date           DATE NOT NULL,
    check_in       TIME,
    check_out      TIME,
    status         VARCHAR(20),
    overtime_hours DECIMAL(4, 2),
    note           TEXT,
    Created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng nghỉ phép

CREATE TABLE leave_policies
(
    leave_policy_id   SERIAL PRIMARY KEY,
    leave_policy_name text,
    leave_policy_amount INTEGER
);

-- Bảng nghỉ phép
CREATE TABLE leaves
(
    leave_id    SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees (employee_id),
    leave_type  VARCHAR(50) NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    total_days  INTEGER     NOT NULL,
    status      VARCHAR(20) NOT NULL,
    reason      TEXT,
    leave_allowed_day INTEGER NOT NULL,
    leave_policy_id INTEGER REFERENCES leave_policies(leave_policy_id),
    approved_by INTEGER REFERENCES employees (employee_id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);



-- Bảng lương tháng
Create TABLE salary_records
(
    salary_id           SERIAL PRIMARY KEY,
    employee_id         INTEGER REFERENCES employees (employee_id),
    month               INTEGER        NOT NULL,
    year                INTEGER        NOT NULL,
    base_salary         DECIMAL(15, 2) NOT NULL,
    total_allowance     DECIMAL(15, 2) NOT NULL,
    overtime_pay        DECIMAL(15, 2) NOT NULL,
    deductions          DECIMAL(15, 2) NOT NULL,
    insurance_deduction DECIMAL(15, 2) NOT NULL,
    tax_amount          DECIMAL(15, 2) NOT NULL,
    net_salary          DECIMAL(15, 2) NOT NULL,
    payment_status      VARCHAR(20)    NOT NULL,
    Created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted bool default false
);

-- Bảng người phụ thuộc
Create TABLE dependents
(
    dependent_id     SERIAL PRIMARY KEY,
    employee_id      INTEGER REFERENCES employees (employee_id),
    full_name        VARCHAR(100) NOT NULL,
    relationship     VARCHAR(50)  NOT NULL,
    birth_date       DATE         NOT NULL,
    id_number        VARCHAR(20),
    is_tax_dependent BOOLEAN   DEFAULT true,
    Created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng người dùng
Create TABLE users
(
    user_id             SERIAL PRIMARY KEY,
    employee_id         INTEGER REFERENCES employees (employee_id),
    username            text NOT NULL,
    password_hash       text NOT NULL,
    email               text,
    role                text      default 'USER',
    status              text      default 'Active',
    last_Authentication TIMESTAMP DEFAULT NULL,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng nhật ký hệ thống
Create TABLE audit_logs
(
    log_id       SERIAL PRIMARY KEY,
    user_id      INTEGER REFERENCES users (user_id),
    action_type  text,
    action_info  text,
    action_level text,
    Created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng yêu cầu
Create TABLE requests
(
    request_id      SERIAL PRIMARY KEY,
    requester_id    INTEGER,
    user_id         INTEGER REFERENCES users (user_id),
    request_type    TEXT,
    reason          TEXT,
    request_id_list JSON,
    approval_id     INTEGER REFERENCES users (user_id),
    status          TEXT,
    is_process      BOOLEAN   DEFAULT FALSE,
    Created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date      DATE,
    end_date        DATE,
    total_days      INTEGER,
    note            TEXT
);

-- Bảng chính sách tài chính
Create TABLE financial_policies
(
    financial_policy_id     serial primary key,
    financial_policy_name   text,
    financial_policy_type   text,
    financial_policy_amount DECIMAL
);

-- Indexes
Create INDEX idx_employee_code ON employees (employee_code);
Create INDEX idx_department_code ON departments (department_code);
Create INDEX idx_position_code ON positions (position_code);
Create INDEX idx_contract_employee ON contracts (employee_id);
Create INDEX idx_attendance_employee_date ON attendance (employee_id, date);
Create INDEX idx_salary_employee_month_year ON salary_records (employee_id, month, year);

-- Thêm dữ liệu chính sách tài chính
INSERT INTO financial_policies(financial_policy_name, financial_policy_type, financial_policy_amount)
VALUES ('BHYT cá nhân trả', 'RATE', 1.5),
       ('BHYT doanh nghiệp trả', 'RATE', 1.5),
       ('BHXH cá nhân trả', 'RATE', 8),
       ('BHXH doanh nghiệp trả', 'RATE', 17.5),
       ('KPCĐ cá nhân trả', 'RATE', 1),
       ('KPCĐ doanh nghiệp trả', 'RATE', 1),
       ('BHTN cá nhân trả', 'RATE', 1),
       ('BHTN doanh nghiệp trả', 'RATE', 1),
       ('Lương làm thêm ngày thường', 'RATE', 1.5),
       ('Giảm trừ cá nhân', 'FIXED', 11000000),
       ('Giảm trừ phụ thuộc', 'FIXED', 4000000),
       ('Số ngày tính công', 'FIXED', 26),
       ('Phạt đi trễ', 'FIXED', 200000),
       ('Công giờ làm', 'FIXED', 8);

-- Hàm tạo tên ngẫu nhiên
CREATE OR REPLACE FUNCTION random_first_name() RETURNS VARCHAR AS $$
DECLARE
    first_names VARCHAR[] := ARRAY['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ', 'Hồ', 'Ngô', 'Dương', 'Lý', 'Đào', 'Đinh', 'Mai', 'Trịnh', 'Lương', 'Hà', 'Tạ', 'Châu', 'Cao', 'Thái', 'Tô', 'Vương', 'Quách', 'Tống', 'Lâm', 'Lưu', 'Triệu', 'Diệp', 'Đoàn', 'Mạc', 'Trang', 'Từ', 'Hứa', 'Thân', 'Trương'];
BEGIN
    RETURN first_names[floor(random() * array_length(first_names, 1)) + 1];
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION random_last_name() RETURNS VARCHAR AS $$
DECLARE
    last_names VARCHAR[] := ARRAY['An', 'Bình', 'Cường', 'Dũng', 'Đức', 'Giang', 'Hải', 'Hiếu', 'Hùng', 'Huy', 'Khang', 'Khánh', 'Khoa', 'Kiên', 'Lâm', 'Long', 'Minh', 'Nam', 'Nghĩa', 'Phong', 'Phúc', 'Quân', 'Quang', 'Quốc', 'Sơn', 'Thắng', 'Thành', 'Thiện', 'Thịnh', 'Trung', 'Trường', 'Tuấn', 'Tùng', 'Việt', 'Vinh', 'Anh', 'Chi', 'Diệp', 'Hà', 'Hạnh', 'Hiền', 'Hoa', 'Hồng', 'Huệ', 'Hương', 'Lan', 'Liên', 'Linh', 'Loan', 'Mai', 'My', 'Nga', 'Ngọc', 'Nhung', 'Nhi', 'Như', 'Phương', 'Quỳnh', 'Thảo', 'Thiên', 'Thủy', 'Trang', 'Trinh', 'Tuyết', 'Vân', 'Yến'];
BEGIN
    RETURN last_names[floor(random() * array_length(last_names, 1)) + 1];
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo ngày sinh ngẫu nhiên
CREATE OR REPLACE FUNCTION random_birth_date() RETURNS DATE AS $$
BEGIN
    RETURN CURRENT_DATE - ((20 + floor(random() * 40))::integer * INTERVAL '1 year') - ((floor(random() * 365))::integer * INTERVAL '1 day');
END;
$$ LANGUAGE plpgsql;


-- Hàm tạo số điện thoại ngẫu nhiên
CREATE OR REPLACE FUNCTION random_phone() RETURNS VARCHAR AS $$
BEGIN
    RETURN '09' || floor(random() * 90000000 + 10000000)::text;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo email cá nhân ngẫu nhiên
CREATE OR REPLACE FUNCTION random_personal_email(first_name VARCHAR, last_name VARCHAR) RETURNS VARCHAR AS $$
DECLARE
    domains VARCHAR[] := ARRAY['gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com'];
    domain VARCHAR;
    email_prefix VARCHAR;
BEGIN
    domain := domains[floor(random() * array_length(domains, 1)) + 1];
    email_prefix := lower(unaccent(first_name)) || lower(unaccent(last_name)) || floor(random() * 1000)::text;
    RETURN email_prefix || '@' || domain;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo email công ty ngẫu nhiên
CREATE OR REPLACE FUNCTION random_company_email(first_name VARCHAR, last_name VARCHAR) RETURNS VARCHAR AS $$
BEGIN
    RETURN lower(unaccent(last_name)) || '.' || lower(unaccent(first_name)) || floor(random() * 100)::text || '@company.com';
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo địa chỉ ngẫu nhiên
CREATE OR REPLACE FUNCTION random_address() RETURNS TEXT AS $$
DECLARE
    streets TEXT[] := ARRAY['Lê Lợi', 'Nguyễn Huệ', 'Trần Hưng Đạo', 'Lê Duẩn', 'Điện Biên Phủ', 'Nguyễn Thị Minh Khai', 'Cách Mạng Tháng 8', 'Võ Văn Ngân', 'Nguyễn Văn Linh', 'Lý Tự Trọng', 'Nguyễn Đình Chiểu', 'Lê Hồng Phong', 'Nguyễn Trãi', 'Lê Thánh Tôn', 'Phạm Ngũ Lão', 'Bùi Viện', 'Hai Bà Trưng', 'Võ Thị Sáu', 'Phan Đình Phùng', 'Phan Xích Long'];
    districts TEXT[] := ARRAY['Quận 1', 'Quận 2', 'Quận 3', 'Quận 4', 'Quận 5', 'Quận 6', 'Quận 7', 'Quận 8', 'Quận 9', 'Quận 10', 'Quận 11', 'Quận 12', 'Quận Bình Thạnh', 'Quận Tân Bình', 'Quận Tân Phú', 'Quận Phú Nhuận', 'Quận Gò Vấp', 'Quận Thủ Đức', 'Huyện Bình Chánh', 'Huyện Củ Chi'];
    cities TEXT[] := ARRAY['TP.HCM', 'Hà Nội', 'Đà Nẵng', 'Cần Thơ', 'Hải Phòng', 'Nha Trang', 'Vũng Tàu', 'Đà Lạt', 'Huế', 'Vinh'];
    street TEXT;
    district TEXT;
    city TEXT;
    house_number INTEGER;
BEGIN
    street := streets[floor(random() * array_length(streets, 1)) + 1];
    district := districts[floor(random() * array_length(districts, 1)) + 1];
    city := cities[floor(random() * array_length(cities, 1)) + 1];
    house_number := floor(random() * 1000 + 1)::integer;

    RETURN house_number || ' ' || street || ', ' || district || ', ' || city;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo số CMND/CCCD ngẫu nhiên
CREATE OR REPLACE FUNCTION random_id_number() RETURNS TEXT AS $$
BEGIN
    RETURN floor(random() * 900000000 + 100000000)::text;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo mã số thuế ngẫu nhiên
CREATE OR REPLACE FUNCTION random_tax_code() RETURNS VARCHAR AS $$
BEGIN
    RETURN floor(random() * 9000000000 + 1000000000)::text;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo số tài khoản ngân hàng ngẫu nhiên
CREATE OR REPLACE FUNCTION random_bank_account() RETURNS VARCHAR AS $$
BEGIN
    RETURN floor(random() * 90000000000 + 10000000000)::text;
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo ngân hàng ngẫu nhiên
CREATE OR REPLACE FUNCTION random_bank_name() RETURNS VARCHAR AS $$
DECLARE
    banks VARCHAR[] := ARRAY['Vietcombank', 'BIDV', 'Agribank', 'Techcombank', 'VPBank', 'ACB', 'MB Bank', 'Sacombank', 'TPBank', 'OCB', 'HDBank', 'VIB', 'SeABank', 'SHB', 'LienVietPostBank', 'Eximbank', 'MSB', 'Vietinbank', 'Bac A Bank', 'Nam A Bank'];
BEGIN
    RETURN banks[floor(random() * array_length(banks, 1)) + 1];
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo trạng thái hôn nhân ngẫu nhiên
CREATE OR REPLACE FUNCTION random_marital_status() RETURNS VARCHAR AS $$
DECLARE
    statuses VARCHAR[] := ARRAY['Độc thân', 'Đã kết hôn', 'Ly hôn', 'Góa'];
    weights NUMERIC[] := ARRAY[0.4, 0.5, 0.05, 0.05];
    r NUMERIC;
    cumulative NUMERIC := 0;
    i INTEGER;
BEGIN
    r := random();
    FOR i IN 1..array_length(statuses, 1) LOOP
            cumulative := cumulative + weights[i];
            IF r <= cumulative THEN
                RETURN statuses[i];
            END IF;
        END LOOP;
    RETURN statuses[1]; -- Mặc định nếu không có gì khớp
END;
$$ LANGUAGE plpgsql;

-- Hàm tạo giới tính ngẫu nhiên
CREATE OR REPLACE FUNCTION random_gender() RETURNS VARCHAR AS $$
DECLARE
    genders VARCHAR[] := ARRAY['Nam', 'Nữ', 'Khác'];
    weights NUMERIC[] := ARRAY[0.5, 0.48, 0.02];
    r NUMERIC;
    cumulative NUMERIC := 0;
    i INTEGER;
BEGIN
    r := random();
    FOR i IN 1..array_length(genders, 1) LOOP
            cumulative := cumulative + weights[i];
            IF r <= cumulative THEN
                RETURN genders[i];
            END IF;
        END LOOP;
    RETURN genders[1]; -- Mặc định nếu không có gì khớp
END;
$$ LANGUAGE plpgsql;

-- Tạo thêm 1000 nhân viên mới
DO $$
    DECLARE
        i INTEGER;
        new_employee_id INTEGER;
        first_name_var VARCHAR;
        last_name_var VARCHAR;
        birth_date_var DATE;
        gender_var VARCHAR;
        id_number_var TEXT;
        permanent_address_var TEXT;
        temporary_address_var TEXT;
        personal_email_var VARCHAR;
        company_email_var VARCHAR;
        phone_number_var VARCHAR;
        marital_status_var VARCHAR;
        bank_account_var VARCHAR;
        bank_name_var VARCHAR;
        tax_code_var VARCHAR;
        employee_code_var TEXT;
        department_id_var INTEGER;
        position_id_var INTEGER;
        contract_type_var VARCHAR;
        contract_code_var VARCHAR;
        start_date_var DATE;
        end_date_var DATE;
        base_salary_var DECIMAL;
        sign_date_var DATE;
        username_var TEXT;
        password_hash_var TEXT;
        contract_types VARCHAR[] := ARRAY['Thử việc', '1 năm', '2 năm', '3 năm', '5 năm'];
        contract_durations INTEGER[] := ARRAY[3, 12, 24, 36, 60];
    BEGIN
        FOR i IN 1..1000 LOOP
                -- Tạo thông tin nhân viên cơ bản
                first_name_var := random_first_name();
                last_name_var := random_last_name();
                birth_date_var := random_birth_date();
                gender_var := random_gender();
                id_number_var := random_id_number();
                permanent_address_var := random_address();
                temporary_address_var := random_address();
                personal_email_var := random_personal_email(first_name_var, last_name_var);
                company_email_var := random_company_email(first_name_var, last_name_var);
                phone_number_var := random_phone();
                marital_status_var := random_marital_status();
                bank_account_var := random_bank_account();
                bank_name_var := random_bank_name();
                tax_code_var := random_tax_code();
                employee_code_var := 'NV' || (i + 1000)::text;

                -- Thêm nhân viên mới
                INSERT INTO employees (
                    employee_code, first_name, last_name, birth_date, gender,
                    id_number, permanent_address, temporary_address,
                    personal_email, company_email, phone_number,
                    marital_status, bank_account, bank_name, tax_code
                ) VALUES (
                             employee_code_var, first_name_var, last_name_var, birth_date_var, gender_var,
                             id_number_var, permanent_address_var, temporary_address_var,
                             personal_email_var, company_email_var, phone_number_var,
                             marital_status_var, bank_account_var, bank_name_var, tax_code_var
                         ) RETURNING employee_id INTO new_employee_id;

                -- Tạo tài khoản người dùng
                username_var := lower(unaccent(last_name_var)) || lower(unaccent(first_name_var)) || (i + 1000)::text;
                password_hash_var := '$2a$10$cUtg8nvDC7PnZLe5eZGVDO4fwD5yvRhemp/vFXDG67OxDMSv7fHbm'; -- Mật khẩu mặc định

                INSERT INTO users (employee_id, username, password_hash)
                VALUES (new_employee_id, username_var, password_hash_var);

                -- Chọn phòng ban ngẫu nhiên
                department_id_var := floor(random() * 15) + 1;

                -- Chọn vị trí ngẫu nhiên trong phòng ban
                SELECT position_id INTO position_id_var
                FROM positions
                WHERE department_id = department_id_var
                ORDER BY random()
                LIMIT 1;

                -- Thêm lịch sử công tác
                INSERT INTO employment_history (
                    employee_id, department_id, position_id, start_date, is_current,
                    transfer_reason, decision_number
                ) VALUES (
                             new_employee_id, department_id_var, position_id_var,
                             CURRENT_DATE - (floor(random() * 1095) + 30)::integer * INTERVAL '1 day',
                             true, 'Tuyển dụng mới', 'QD' || (i + 1000)::text || '/' || extract(year from CURRENT_DATE)::text
                         );

                -- Tạo hợp đồng
                contract_type_var := contract_types[floor(random() * array_length(contract_types, 1)) + 1];
                start_date_var := CURRENT_DATE - (floor(random() * 1095) + 30)::integer * INTERVAL '1 day';

                -- Xác định thời hạn hợp đồng dựa trên loại
                CASE contract_type_var
                    WHEN 'Thử việc' THEN end_date_var := start_date_var + INTERVAL '3 month';
                    WHEN '1 năm' THEN end_date_var := start_date_var + INTERVAL '1 year';
                    WHEN '2 năm' THEN end_date_var := start_date_var + INTERVAL '2 year';
                    WHEN '3 năm' THEN end_date_var := start_date_var + INTERVAL '3 year';
                    WHEN '5 năm' THEN end_date_var := start_date_var + INTERVAL '5 year';
                    ELSE end_date_var := start_date_var + INTERVAL '1 year';
                    END CASE;

                -- Xác định mức lương dựa trên vị trí
                SELECT
                    CASE
                        WHEN p.level = 1 THEN 8000000 + floor(random() * 4000000)
                        WHEN p.level = 2 THEN 12000000 + floor(random() * 6000000)
                        WHEN p.level = 3 THEN 18000000 + floor(random() * 12000000)
                        WHEN p.level = 4 THEN 30000000 + floor(random() * 20000000)
                        WHEN p.level = 5 THEN 50000000 + floor(random() * 30000000)
                        ELSE 10000000
                        END INTO base_salary_var
                FROM positions p
                WHERE p.position_id = position_id_var;

                sign_date_var := start_date_var - (floor(random() * 15) + 1)::integer * INTERVAL '1 day';
                contract_code_var := 'HD' || (i + 1000)::text || '/' || extract(year from start_date_var)::text;

                INSERT INTO contracts (
                    employee_id, contract_type, contract_code,
                    start_date, end_date, base_salary,
                    sign_date, is_present
                ) VALUES (
                             new_employee_id, contract_type_var, contract_code_var,
                             start_date_var, end_date_var, base_salary_var,
                             sign_date_var, CURRENT_DATE BETWEEN start_date_var AND end_date_var
                         );

                -- Thêm phụ cấp cơ bản
                INSERT INTO allowances (
                    employee_id, allowance_type, amount, start_date, description
                ) VALUES (
                             new_employee_id, 'Phụ cấp ăn trưa', 800000, start_date_var, 'Phụ cấp ăn trưa hàng tháng'
                         );

                -- Thêm phụ cấp điện thoại cho một số nhân viên
                IF random() < 0.7 THEN
                    INSERT INTO allowances (
                        employee_id, allowance_type, amount, start_date, description
                    ) VALUES (
                                 new_employee_id, 'Phụ cấp điện thoại',
                                 CASE
                                     WHEN (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) >= 3 THEN 500000
                                     ELSE 300000
                                     END,
                                 start_date_var, 'Phụ cấp điện thoại hàng tháng'
                             );
                END IF;

                -- Thêm phụ cấp nhiên liệu cho một số nhân viên
                IF random() < 0.4 THEN
                    INSERT INTO allowances (
                        employee_id, allowance_type, amount, start_date, description
                    ) VALUES (
                                 new_employee_id, 'Phụ cấp nhiên liệu',
                                 CASE
                                     WHEN (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) >= 3 THEN 1500000
                                     ELSE 1000000
                                     END,
                                 start_date_var, 'Phụ cấp xăng xe hàng tháng'
                             );
                END IF;

                -- Thêm phụ cấp chức vụ cho trưởng phòng và cấp cao hơn
                IF (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) >= 3 THEN
                    INSERT INTO allowances (
                        employee_id, allowance_type, amount, start_date, description
                    ) VALUES (
                                 new_employee_id, 'Phụ cấp chức vụ',
                                 CASE
                                     WHEN (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) = 5 THEN 10000000
                                     WHEN (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) = 4 THEN 8000000
                                     WHEN (SELECT p.level FROM positions p WHERE p.position_id = position_id_var) = 3 THEN 5000000
                                     ELSE 0
                                     END,
                                 start_date_var, 'Phụ cấp cho vị trí quản lý'
                             );
                END IF;

                -- Thêm người phụ thuộc cho nhân viên đã kết hôn
                IF marital_status_var = 'Đã kết hôn' THEN
                    -- Thêm vợ/chồng
                    INSERT INTO dependents (
                        employee_id, full_name, relationship, birth_date,
                        id_number, is_tax_dependent
                    ) VALUES (
                                 new_employee_id,
                                 (CASE WHEN gender_var = 'Nam' THEN random_first_name() || ' Thị ' || random_last_name() ELSE random_first_name() || ' Văn ' || random_last_name() END),
                                 (CASE WHEN gender_var = 'Nam' THEN 'Vợ' ELSE 'Chồng' END),
                                 birth_date_var - (floor(random() * 1825) - 1825)::integer * INTERVAL '1 day',
                                 random_id_number(),
                                 true
                             );

                    -- Thêm con (0-3 người con)
                    FOR j IN 1..floor(random() * 4)::integer LOOP
                            INSERT INTO dependents (
                                employee_id, full_name, relationship, birth_date,
                                id_number, is_tax_dependent
                            ) VALUES (
                                         new_employee_id,
                                         random_first_name() || ' ' || (CASE WHEN random() < 0.5 THEN 'Minh ' ELSE 'Thị ' END) || random_last_name(),
                                         'Con',
                                         CURRENT_DATE - (floor(random() * 7300))::integer * INTERVAL '1 day',
                                         (CASE WHEN random() < 0.3 THEN random_id_number() ELSE NULL END),
                                         true
                                     );
                        END LOOP;
                END IF;

                -- Thêm dữ liệu lương cho vài tháng gần đây
                FOR j IN 1..3 LOOP
                        -- Chỉ thêm dữ liệu lương cho nhân viên đã làm việc trong những tháng đó
                        IF start_date_var <= (CURRENT_DATE - (j * INTERVAL '1 month')) THEN
                            -- Tính tổng phụ cấp
                            INSERT INTO salary_records (
                                employee_id, month, year, base_salary, total_allowance,
                                overtime_pay, deductions, insurance_deduction, tax_amount, net_salary, payment_status
                            ) VALUES (
                                         new_employee_id,
                                         EXTRACT(MONTH FROM CURRENT_DATE - (j * INTERVAL '1 month')),
                                         EXTRACT(YEAR FROM CURRENT_DATE - (j * INTERVAL '1 month')),
                                         base_salary_var,
                                         (SELECT COALESCE(SUM(amount), 0) FROM allowances WHERE employee_id = new_employee_id),
                                         floor(random() * 2000000),
                                         floor(random() * 500000),
                                         base_salary_var * 0.105, -- 10.5% BHXH + BHYT + BHTN
                                         GREATEST(0, (base_salary_var - 11000000) * 0.1), -- Thuế thu nhập cá nhân đơn giản
                                         base_salary_var +
                                         (SELECT COALESCE(SUM(amount), 0) FROM allowances WHERE employee_id = new_employee_id) +
                                         floor(random() * 2000000) -
                                         floor(random() * 500000) -
                                         (base_salary_var * 0.105) -
                                         GREATEST(0, (base_salary_var - 11000000) * 0.1),
                                         'Paid'
                                     );
                        END IF;
                    END LOOP;

                -- Thêm dữ liệu chấm công ngẫu nhiên cho tháng hiện tại
                FOR j IN 1..22 LOOP -- 22 ngày làm việc trong tháng
                -- Chỉ thêm dữ liệu chấm công cho nhân viên đã làm việc
                        IF start_date_var <= (date_trunc('month', CURRENT_DATE) + ((j-1) || ' days')::interval) THEN
                            -- Bỏ qua ngày cuối tuần
                            CONTINUE WHEN EXTRACT(DOW FROM (date_trunc('month', CURRENT_DATE) + ((j-1) || ' days')::interval)) IN (0, 6);

                            INSERT INTO attendance (
                                employee_id, date, check_in, check_out, status, overtime_hours
                            ) VALUES (
                                         new_employee_id,
                                         date_trunc('month', CURRENT_DATE) + ((j-1) || ' days')::interval,
                                         (CASE
                                              WHEN random() < 0.8 THEN '08:00:00'::time + (floor(random() * 30) || ' minutes')::interval
                                              WHEN random() < 0.95 THEN '08:30:00'::time + (floor(random() * 30) || ' minutes')::interval
                                              ELSE NULL
                                             END),
                                         (CASE
                                              WHEN random() < 0.9 THEN '17:30:00'::time + (floor(random() * 90) || ' minutes')::interval
                                              WHEN random() < 0.95 THEN '17:00:00'::time + (floor(random() * 30) || ' minutes')::interval
                                              ELSE NULL
                                             END),
                                         (CASE
                                              WHEN random() < 0.85 THEN 'On time'
                                              WHEN random() < 0.95 THEN 'Late'
                                              ELSE 'Absent'
                                             END),
                                         (CASE WHEN random() < 0.3 THEN round((random() * 3)::numeric, 1) ELSE 0 END)
                                     );
                        END IF;
                    END LOOP;

                -- Thêm dữ liệu nghỉ phép ngẫu nhiên
                IF random() < 0.2 AND start_date_var <= (CURRENT_DATE - INTERVAL '3 month') THEN
                    INSERT INTO leaves (
                        employee_id, leave_type, start_date, end_date,
                        total_days, status, reason, leave_allowed_day, leave_policy_id,
                        approved_by
                    ) VALUES (
                                 new_employee_id,
                                 (CASE
                                      WHEN random() < 0.6 THEN 'annual leave'
                                      WHEN random() < 0.8 THEN 'sick leave'
                                      WHEN random() < 0.9 THEN 'wedding leave'
                                      ELSE 'no pay leave'
                                     END),
                                 CURRENT_DATE - (floor(random() * 60) + 5)::integer * INTERVAL '1 day',
                                 CURRENT_DATE - (floor(random() * 5))::integer * INTERVAL '1 day',
                                 floor(random() * 5) + 1,
                                 (CASE
                                      WHEN random() < 0.9 THEN 'Approved'
                                      ELSE 'Pending'
                                     END),
                                 (CASE
                                      WHEN random() < 0.6 THEN 'Nghỉ phép năm'
                                      WHEN random() < 0.8 THEN 'Nghỉ ốm'
                                      WHEN random() < 0.9 THEN 'Kết hôn'
                                      ELSE 'Việc cá nhân'
                                     END),
                                 12, -- Số ngày phép được phép
                                 (CASE
                                      WHEN random() < 0.6 THEN 1 -- annual leave
                                      WHEN random() < 0.8 THEN 2 -- sick leave
                                      WHEN random() < 0.9 THEN 4 -- wedding leave
                                      ELSE 6 -- no pay leave
                                     END),
                                 (SELECT employee_id FROM employees WHERE employee_id IN (1, 2, 3, 4, 5, 6, 7) ORDER BY random() LIMIT 1)
                             );
                END IF;
            END LOOP;
    END $$;

-- Tiếp tục phần cập nhật manager cho phòng ban
UPDATE departments
SET manager_id = (
    SELECT employee_id
    FROM employment_history eh
             JOIN positions p ON eh.position_id = p.position_id
    WHERE eh.department_id = departments.department_id
      AND p.level >= 3
      AND eh.is_current = true
      AND eh.employee_id > 15 -- Không cập nhật các manager đã được thiết lập trước đó
    ORDER BY random()
    LIMIT 1
)
WHERE department_id > 15 -- Chỉ cập nhật các phòng ban mới nếu có
   OR manager_id IS NULL;

-- Thêm một số nhật ký hệ thống cho nhân viên mới
INSERT INTO audit_logs (user_id, action_type, action_info, action_level)
SELECT
    u.user_id,
    CASE
        WHEN random() < 0.4 THEN 'Authentication'
        WHEN random() < 0.6 THEN 'View'
        WHEN random() < 0.8 THEN 'Update'
        ELSE 'Create'
        END,
    CASE
        WHEN random() < 0.4 THEN 'Đăng nhập hệ thống'
        WHEN random() < 0.6 THEN 'Xem thông tin nhân viên'
        WHEN random() < 0.8 THEN 'Cập nhật thông tin cá nhân'
        ELSE 'Tạo yêu cầu nghỉ phép'
        END,
    CASE
        WHEN random() < 0.9 THEN 'INFO'
        ELSE 'WARNING'
        END
FROM users u
         JOIN employees e ON u.employee_id = e.employee_id
WHERE e.employee_id > 15 -- Chỉ thêm nhật ký cho nhân viên mới
  AND random() < 0.3 -- Chỉ thêm cho khoảng 30% nhân viên mới
LIMIT 500; -- Giới hạn số lượng nhật ký

-- Thêm một số yêu cầu ngẫu nhiên
INSERT INTO requests (requester_id, user_id, request_type, reason, status, is_process,
                      Created_at, start_date, end_date, total_days, note)
SELECT
    e.employee_id,
    u.user_id,
    CASE
        WHEN random() < 0.5 THEN 'Nghỉ phép'
        WHEN random() < 0.7 THEN 'Tăng lương'
        WHEN random() < 0.9 THEN 'Bổ sung phụ cấp'
        ELSE 'Khác'
        END,
    CASE
        WHEN random() < 0.5 THEN 'Nghỉ phép năm'
        WHEN random() < 0.7 THEN 'Xin tăng lương do thành tích tốt'
        WHEN random() < 0.9 THEN 'Xin bổ sung phụ cấp đi lại'
        ELSE 'Yêu cầu khác'
        END,
    CASE
        WHEN random() < 0.6 THEN 'Approved'
        WHEN random() < 0.8 THEN 'Pending'
        ELSE 'Rejected'
        END,
    CASE
        WHEN random() < 0.7 THEN true
        ELSE false
        END,
    CURRENT_TIMESTAMP - (floor(random() * 90) || ' days')::interval,
    CURRENT_DATE - (floor(random() * 30) || ' days')::interval,
    CURRENT_DATE - (floor(random() * 15) || ' days')::interval,
    floor(random() * 5) + 1,
    CASE
        WHEN random() < 0.3 THEN 'Đã xử lý'
        WHEN random() < 0.6 THEN 'Đang chờ phê duyệt'
        ELSE NULL
        END
FROM employees e
         JOIN users u ON e.employee_id = u.employee_id
WHERE e.employee_id > 15 -- Chỉ thêm yêu cầu cho nhân viên mới
  AND random() < 0.2 -- Chỉ thêm cho khoảng 20% nhân viên mới
LIMIT 300;

-- Cập nhật approval_id cho các yêu cầu đã được phê duyệt hoặc từ chối
UPDATE requests
SET approval_id = (
    SELECT user_id
    FROM users
    WHERE employee_id IN (
        SELECT manager_id
        FROM departments
        WHERE department_id = (
            SELECT department_id
            FROM employment_history
            WHERE employee_id = requests.requester_id
              AND is_current = true
        )
    )
    LIMIT 1
)
WHERE status IN ('Approved', 'Rejected')
  AND approval_id IS NULL;

-- Thêm một số tin nhắn vào kênh chung
INSERT INTO messages (channel_id, user_id, message_content, created_at)
SELECT
    1, -- Channel General
    u.user_id,
    CASE
        WHEN random() < 0.2 THEN 'Xin chào mọi người!'
        WHEN random() < 0.4 THEN 'Chúc mọi người một ngày làm việc hiệu quả!'
        WHEN random() < 0.6 THEN 'Có ai biết thông tin về dự án mới không?'
        WHEN random() < 0.8 THEN 'Mình cần hỗ trợ về vấn đề kỹ thuật, có ai giúp được không?'
        ELSE 'Chúc mừng sinh nhật công ty!'
        END,
    CURRENT_TIMESTAMP - (floor(random() * 30) || ' days')::interval - (floor(random() * 24) || ' hours')::interval
FROM users u
         JOIN employees e ON u.employee_id = e.employee_id
WHERE e.employee_id > 15 -- Chỉ thêm tin nhắn từ nhân viên mới
  AND random() < 0.1 -- Chỉ thêm cho khoảng 10% nhân viên mới
ORDER BY random()
LIMIT 200;

-- Thêm một số tin nhắn vào kênh hệ thống từ tài khoản hệ thống
INSERT INTO messages (channel_id, user_id, message_content, created_at)
SELECT
    2, -- Channel System
    (SELECT user_id FROM users WHERE username = 'system'),
    CASE
        WHEN random() < 0.2 THEN 'Hệ thống sẽ bảo trì vào cuối tuần này.'
        WHEN random() < 0.4 THEN 'Vui lòng cập nhật thông tin cá nhân trước ngày 15 tháng này.'
        WHEN random() < 0.6 THEN 'Đã phát hiện lỗi hệ thống và đang khắc phục.'
        WHEN random() < 0.8 THEN 'Hệ thống đã được nâng cấp lên phiên bản mới.'
        ELSE 'Vui lòng đổi mật khẩu định kỳ để đảm bảo an toàn thông tin.'
        END,
    CURRENT_TIMESTAMP - (floor(random() * 60) || ' days')::interval
FROM generate_series(1, 20);

-- Đảm bảo giữ nguyên các dữ liệu đặc biệt
-- Đảm bảo tài khoản System Administrator không bị thay đổi
UPDATE users
SET role = 'SYSTEM',
    status = 'Active'
WHERE username = 'system';

-- Đảm bảo một số tài khoản quản lý được phân quyền cao hơn
UPDATE users
SET role = 'ADMIN'
WHERE employee_id IN (1, 2, 3, 4, 5, 6, 7)
  AND role = 'USER';

-- Đảm bảo tất cả nhân viên đều có dữ liệu chấm công đầy đủ cho tháng hiện tại
WITH missing_attendance AS (
    SELECT e.employee_id, d.date
    FROM employees e
             CROSS JOIN generate_series(
            date_trunc('month', CURRENT_DATE)::date,
            (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month - 1 day')::date,
            INTERVAL '1 day'
                        ) AS d(date)
             LEFT JOIN attendance a ON e.employee_id = a.employee_id AND a.date = d.date
    WHERE a.attendance_id IS NULL
      AND EXTRACT(DOW FROM d.date) BETWEEN 1 AND 5 -- Chỉ ngày làm việc (thứ 2 đến thứ 6)
      AND e.is_deleted = false
      AND d.date <= CURRENT_DATE -- Chỉ đến ngày hiện tại
)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, overtime_hours)
SELECT
    employee_id,
    date,
    CASE
        WHEN random() < 0.8 THEN '08:00:00'::time + (floor(random() * 30) || ' minutes')::interval
        WHEN random() < 0.95 THEN '08:30:00'::time + (floor(random() * 30) || ' minutes')::interval
        ELSE NULL
        END,
    CASE
        WHEN random() < 0.9 THEN '17:30:00'::time + (floor(random() * 90) || ' minutes')::interval
        WHEN random() < 0.95 THEN '17:00:00'::time + (floor(random() * 30) || ' minutes')::interval
        ELSE NULL
        END,
    CASE
        WHEN random() < 0.85 THEN 'On time'
        WHEN random() < 0.95 THEN 'Late'
        ELSE 'Absent'
        END,
    CASE WHEN random() < 0.3 THEN round((random() * 3)::numeric, 1) ELSE 0 END
FROM missing_attendance;

-- Thêm dữ liệu lương cho tháng hiện tại cho nhân viên chưa có
INSERT INTO salary_records (employee_id, month, year, base_salary, total_allowance,
                            overtime_pay, deductions, insurance_deduction, tax_amount, net_salary, payment_status)
SELECT
    e.employee_id,
    EXTRACT(MONTH FROM CURRENT_DATE),
    EXTRACT(YEAR FROM CURRENT_DATE),
    c.base_salary,
    COALESCE((SELECT SUM(amount) FROM allowances WHERE employee_id = e.employee_id), 0),
    (SELECT COALESCE(SUM(overtime_hours) * 1.5 * (c.base_salary / 26 / 8), 0)
     FROM attendance
     WHERE employee_id = e.employee_id
       AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)
     group by c.base_salary),
    (SELECT COUNT(*) * 200000
     FROM attendance
     WHERE employee_id = e.employee_id
       AND status = 'Late'
       AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)),
    c.base_salary * 0.105, -- 10.5% BHXH + BHYT + BHTN
    GREATEST(0, (c.base_salary - 11000000) * 0.1), -- Thuế thu nhập cá nhân đơn giản
    c.base_salary +
    COALESCE((SELECT SUM(amount) FROM allowances WHERE employee_id = e.employee_id), 0) +
    (SELECT COALESCE(SUM(overtime_hours) * 1.5 * (c.base_salary / 26 / 8), 0)
     FROM attendance
     WHERE employee_id = e.employee_id
       AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)
     group by c.base_salary) -
    (SELECT COUNT(*) * 200000
     FROM attendance
     WHERE employee_id = e.employee_id
       AND status = 'Late'
       AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
       AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)) -
    (c.base_salary * 0.105) -
    GREATEST(0, (c.base_salary - 11000000) * 0.1),
    CASE
        WHEN CURRENT_DATE > (date_trunc('month', CURRENT_DATE) + INTERVAL '25 days') THEN 'Paid'
        ELSE 'Pending'
        END
FROM employees e
         JOIN contracts c ON e.employee_id = c.employee_id
         LEFT JOIN salary_records sr ON e.employee_id = sr.employee_id
    AND sr.month = EXTRACT(MONTH FROM CURRENT_DATE)
    AND sr.year = EXTRACT(YEAR FROM CURRENT_DATE)
WHERE sr.salary_id IS NULL -- Chỉ thêm cho nhân viên chưa có dữ liệu lương tháng hiện tại
  AND c.is_present = true -- Chỉ thêm cho hợp đồng hiện tại
  AND e.is_deleted = false;

-- Cập nhật thông tin thêm cho người phụ thuộc
UPDATE dependents
SET is_tax_dependent = true
WHERE is_tax_dependent IS NULL;

-- Đảm bảo các nhân viên có đủ dữ liệu phép năm
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, total_days, status, reason, leave_allowed_day, leave_policy_id, approved_by)
SELECT
    e.employee_id,
    'annual leave',
    CURRENT_DATE - (floor(random() * 180) + 1)::integer * INTERVAL '1 day',
    CURRENT_DATE - (floor(random() * 180) + 1)::integer * INTERVAL '1 day' + (floor(random() * 3) + 1)::integer * INTERVAL '1 day',
    floor(random() * 3) + 1,
    'Approved',
    'Nghỉ phép năm',
    12,
    1,
    (SELECT employee_id FROM employees WHERE employee_id IN (1, 2, 3, 4, 5, 6, 7) ORDER BY random() LIMIT 1)
FROM employees e
         LEFT JOIN leaves l ON e.employee_id = l.employee_id
WHERE l.leave_id IS NULL -- Chỉ thêm cho nhân viên chưa có dữ liệu nghỉ phép
  AND e.employee_id > 15 -- Chỉ thêm cho nhân viên mới
  AND e.is_deleted = false
  AND random() < 0.7 -- Chỉ thêm cho khoảng 70% nhân viên mới
LIMIT 500;

-- Thêm một số dữ liệu audit log cho hoạt động hệ thống
INSERT INTO audit_logs (user_id, action_type, action_info, action_level)
VALUES
    ((SELECT user_id FROM users WHERE username = 'system'), 'SYSTEM', 'Hệ thống tự động sao lưu dữ liệu', 'INFO'),
    ((SELECT user_id FROM users WHERE username = 'system'), 'SYSTEM', 'Hệ thống tự động tính lương tháng', 'INFO'),
    ((SELECT user_id FROM users WHERE username = 'system'), 'SYSTEM', 'Hệ thống tự động gửi email nhắc nhở', 'INFO'),
    ((SELECT user_id FROM users WHERE username = 'system'), 'SYSTEM', 'Hệ thống phát hiện đăng nhập bất thường', 'WARNING'),
    ((SELECT user_id FROM users WHERE username = 'system'), 'SYSTEM', 'Hệ thống cập nhật phiên bản mới', 'INFO');

-- Xóa các hàm tạm thời đã sử dụng
DROP FUNCTION IF EXISTS random_first_name();
DROP FUNCTION IF EXISTS random_last_name();
DROP FUNCTION IF EXISTS random_birth_date();
DROP FUNCTION IF EXISTS random_phone();
DROP FUNCTION IF EXISTS random_personal_email(VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS random_company_email(VARCHAR, VARCHAR);
DROP FUNCTION IF EXISTS random_address();
DROP FUNCTION IF EXISTS random_id_number();
DROP FUNCTION IF EXISTS random_tax_code();
DROP FUNCTION IF EXISTS random_bank_account();
DROP FUNCTION IF EXISTS random_bank_name();
DROP FUNCTION IF EXISTS random_marital_status();
DROP FUNCTION IF EXISTS random_gender();
