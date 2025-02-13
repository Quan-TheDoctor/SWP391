-- Xoá toàn bộ schema và tất cả các đối tượng bên trong
DROP SCHEMA public CASCADE;
-- Tạo lại schema public sau khi đã xoá
CREATE SCHEMA public;

-- Bảng thông tin nhân viên cơ bản
CREATE TABLE employees
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
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng phòng ban
CREATE TABLE departments
(
    department_id        SERIAL PRIMARY KEY,
    department_name      VARCHAR(100)       NOT NULL,
    department_code      VARCHAR(20) UNIQUE NOT NULL,
    parent_department_id INTEGER REFERENCES departments (department_id),
    description          TEXT,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng chức vụ
CREATE TABLE positions
(
    position_id   SERIAL PRIMARY KEY,
    position_name VARCHAR(100)       NOT NULL,
    position_code VARCHAR(20) UNIQUE NOT NULL,
    level         INTEGER            NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng quá trình công tác
CREATE TABLE employment_history
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
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng hợp đồng lao động
CREATE TABLE contracts
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
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng phụ cấp của nhân viên
CREATE TABLE allowances
(
    allowance_id   SERIAL PRIMARY KEY,
    employee_id    INTEGER REFERENCES employees (employee_id),
    allowance_type VARCHAR(50)    NOT NULL,
    amount         DECIMAL(15, 2) NOT NULL,
    start_date     DATE           NOT NULL,
    end_date       DATE,
    description    TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng chấm công
CREATE TABLE attendance
(
    attendance_id  SERIAL PRIMARY KEY,
    employee_id    INTEGER REFERENCES employees (employee_id),
    date           DATE NOT NULL,
    check_in       TIMESTAMP,
    check_out      TIMESTAMP,
    status         VARCHAR(20),
    overtime_hours DECIMAL(4, 2),
    note           TEXT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng nghỉ phép
CREATE TABLE leaves
(
    leave_id    SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees (employee_id),
    leave_type  VARCHAR(50)   NOT NULL,
    start_date  DATE          NOT NULL,
    end_date    DATE          NOT NULL,
    total_days  DECIMAL(4, 1) NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    reason      TEXT,
    approved_by INTEGER REFERENCES employees (employee_id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lương tháng
CREATE TABLE salary_records
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
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng đánh giá KPI
CREATE TABLE performance_reviews
(
    review_id     SERIAL PRIMARY KEY,
    employee_id   INTEGER REFERENCES employees (employee_id),
    reviewer_id   INTEGER REFERENCES employees (employee_id),
    review_period VARCHAR(20)   NOT NULL,
    review_year   INTEGER       NOT NULL,
    overall_score DECIMAL(3, 2) NOT NULL,
    strengths     TEXT,
    improvements  TEXT,
    comments      TEXT,
    status        VARCHAR(20)   NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng bằng cấp và chứng chỉ
CREATE TABLE qualifications
(
    qualification_id   SERIAL PRIMARY KEY,
    employee_id        INTEGER REFERENCES employees (employee_id),
    qualification_type VARCHAR(50)  NOT NULL,
    qualification_name VARCHAR(100) NOT NULL,
    institution        VARCHAR(100) NOT NULL,
    issue_date         DATE         NOT NULL,
    expiry_date        DATE,
    description        TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_employee_code ON employees (employee_code);
CREATE INDEX idx_department_code ON departments (department_code);
CREATE INDEX idx_position_code ON positions (position_code);
CREATE INDEX idx_contract_employee ON contracts (employee_id);
CREATE INDEX idx_attendance_employee_date ON attendance (employee_id, date);
CREATE INDEX idx_salary_employee_month_year ON salary_records (employee_id, month, year);

CREATE TABLE dependents
(
    dependent_id     SERIAL PRIMARY KEY,
    employee_id      INTEGER REFERENCES employees (employee_id),
    full_name        VARCHAR(100) NOT NULL,
    relationship     VARCHAR(50)  NOT NULL,
    birth_date       DATE         NOT NULL,
    id_number        VARCHAR(20),
    is_tax_dependent BOOLEAN   DEFAULT true,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table users
(
    user_id       SERIAL PRIMARY KEY,
    employee_id   INTEGER REFERENCES employees (employee_id),
    username      text NOT NULL,
    password_hash text NOT NULL,
    email         text,
    role          text      default 'USER',
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

create table audit_logs
(
    log_id      SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users (user_id),
    action_type text,
    action_info text,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE requests
(
    request_id serial primary key,
    requester_id INTEGER REFERENCES users (user_id),
    request_type text,
    request_id_list json,
    approval_id INTEGER REFERENCES users(user_id),
    status text,
    is_process bool default false,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO employees (employee_code, first_name, last_name, birth_date, gender,
                       id_number, permanent_address, temporary_address,
                       personal_email, company_email, phone_number,
                       marital_status, bank_account, bank_name, tax_code)
VALUES ('NV001', 'Nguyễn', 'Văn An', '1990-05-15', 'Nam',
        '001090123456', '123 Lê Lợi, Q.1, TP.HCM', '123 Lê Lợi, Q.1, TP.HCM',
        'nguyenvanan@gmail.com', 'an.nguyen@company.com', '0901234567',
        'Đã kết hôn', '1234567890', 'Vietcombank', '0123456789'),
       ('NV002', 'Trần', 'Bảo Hùng', '1988-11-10', 'Nam',
        '002090123456', '456 Nguyễn Thị Minh Khai, Q.3, TP.HCM', '456 Nguyễn Thị Minh Khai, Q.3, TP.HCM',
        'tranbaohung@gmail.com', 'hung.tran@company.com', '0902345678',
        'Đã kết hôn', '2345678901', 'Vietcombank', '0234567890');

INSERT INTO users (employee_id, username, password_hash)
VALUES (1, 'annguyen', '1'),
       (2, 'annguyen', '1');

INSERT INTO departments (department_name, department_code, description, parent_department_id)
VALUES ('Khối Công nghệ', 'TECH', 'Khối công nghệ thông tin', null),
       ('Khối Kinh doanh', 'SALES', 'Khối kinh doanh và marketing', null),
       ('Khối Hành chính - Nhân sự', 'HR', 'Khối quản lý nhân sự và hành chính', null),
       ('Khối Tài chính', 'FIN', 'Khối tài chính và kế toán', null),
       ('Khối Nghiên cứu và Phát triển', 'R&D', 'Khối nghiên cứu và phát triển sản phẩm công nghệ', null),
       ('Phòng Pháp lý', 'LEGAL', 'Phòng giải quyết các vấn đề pháp lý của công ty', null),
       ('Phòng Quản lý Dự án', 'PMO', 'Quản lý, giám sát và điều phối các dự án công nghệ', null),
       --  Khối công nghệ
       ('Phòng Phát triển Phần mềm', 'DEV', 'Phòng phát triển ứng dụng và phần mềm', 1),
       ('Phòng An ninh Thông tin', 'SEC', 'Phòng đảm bảo bảo mật cho hệ thống và dữ liệu', 1),
       ('Phòng Kiểm thử và Đảm bảo Chất lượng', 'QA', 'Phòng kiểm thử phần mềm và đảm bảo chất lượng', 1),
       ('Phòng Quản trị Hệ thống', 'SYS', 'Quản lý cơ sở hạ tầng công nghệ thông tin', 1),
       ('Phòng Phân tích Dữ liệu', 'DATA', 'Phòng phân tích dữ liệu và hỗ trợ quyết định', 1),
--  Khối kinh doanh
       ('Phòng Kinh doanh', 'SLS', 'Phòng tiếp cận và chăm sóc khách hàng', 2),
       ('Phòng Marketing', 'MKT', 'Phòng xây dựng chiến lược marketing và truyền thông', 2),
       ('Phòng Hỗ trợ Khách hàng', 'SUP', 'Phòng hỗ trợ và giải quyết khiếu nại khách hàng', 2),
--  Khối Hành chính - nhân sự
       ('Phòng Nhân sự', 'HRM', 'Quản lý tuyển dụng và phát triển nhân sự', 3),
       ('Phòng Hành chính', 'ADM', 'Quản lý các công việc hành chính văn phòng', 3),
--  Khối Tài chính
       ('Phòng Kế toán', 'ACC', 'Quản lý kế toán và báo cáo tài chính', 4),
       ('Phòng Phân tích Tài chính', 'FIN-ANAL', 'Phân tích tài chính và lập báo cáo tài chính', 4),
--  Khối Nghiên cứu và Phát triển
       ('Phòng Nghiên cứu', 'RES', 'Nghiên cứu công nghệ và xu hướng mới', 5),
       ('Phòng Phát triển Sản phẩm', 'PROD', 'Phát triển sản phẩm và chuyển giao từ nghiên cứu', 5);

-- Thêm chức vụ
INSERT INTO positions (position_name, position_code, level, description)
VALUES
    -- Khối Công nghệ
    ('Junior Software Developer', 'DEV_POS001', 1, 'Junior level software developer'),
    ('Mid-level Software Developer', 'DEV_POS002', 2, 'Mid-level software developer'),
    ('Senior Software Developer', 'DEV_POS003', 3, 'Senior level software developer'),
    ('Security Analyst', 'SEC_POS001', 2, 'Responsible for ensuring IT security'),
    ('QA Engineer', 'QA_POS001', 2, 'Quality assurance and testing of software'),
    ('System Administrator', 'SYS_POS001', 2, 'Responsible for maintaining IT infrastructure'),
    ('Data Analyst', 'DATA_POS001', 2, 'Analyze data to support decision-making'),
    -- Khối Kinh doanh
    ('Sales Representative', 'SLS_POS001', 1, 'Sales role focusing on customer acquisition'),
    ('Marketing Specialist', 'MKT_POS001', 1, 'Develop and execute marketing strategies'),
    ('Customer Support Representative', 'SUP_POS001', 1, 'Provide support and resolve customer issues'),

    -- Khối Hành chính - Nhân sự
    ('Human Resources Specialist', 'HRM_POS001', 1, 'Manage recruitment and employee relations'),
    ('Office Administrator', 'ADM_POS001', 1, 'Handle office administrative tasks'),

    -- Khối Tài chính
    ('Accountant', 'ACC_POS001', 1, 'Manage financial records and reports'),
    ('Financial Analyst', 'FIN-ANAL_POS001', 2, 'Analyze financial data and generate reports'),

    -- Khối Nghiên cứu và Phát triển
    ('Researcher', 'RES_POS001', 1, 'Conduct research to drive technological innovations'),
    ('Product Developer', 'PROD_POS001', 2, 'Develop products from research outcomes');


-- Thêm hợp đồng
INSERT INTO contracts (employee_id, contract_type, contract_code,
                       start_date, end_date, base_salary,
                       sign_date, is_present)
VALUES (1, '1 năm', 'HD001/2020',
        '2020-01-01', '2021-01-01', 9000000,
        '2019-12-25', false),
       (1, '2 năm', 'HD002/2021',
        '2021-01-01', '2023-01-01', 14000000,
        '2020-12-10', true),
       (2, '1 năm', 'HD001/2021',
        '2021-06-01', '2022-06-01', 12000000,
        '2021-05-20', true);

-- Thêm người phụ thuộc
INSERT INTO dependents (employee_id, full_name, relationship,
                        birth_date, id_number, is_tax_dependent)
VALUES (1, 'Nguyễn Thị Bình', 'Vợ', '1992-08-20', '001092123456', true),
       (1, 'Nguyễn Văn Cường', 'Con', '2018-03-10', NULL, true);

-- Generate attendance records for all employees (20 days each)
WITH date_range AS (SELECT generate_series(
                                   date_trunc('month', CURRENT_DATE) + '1 day'::interval,
                                   date_trunc('month', CURRENT_DATE) + '100 days'::interval,
                                   '1 day'::interval
                           )::date AS work_date)
INSERT
INTO attendance (employee_id, date, check_in, check_out, status, overtime_hours)
SELECT e.employee_id,
       d.work_date,
       d.work_date + '08:00:00'::interval + (random() * interval '30 minutes'),
       d.work_date + '18:00:00'::interval + (random() * interval '120 minutes'),
       CASE
           WHEN random() < 0.9 THEN 'Đúng giờ'
           ELSE 'Đi muộn'
           END,
       CASE
           WHEN random() < 0.1 THEN round((random() * 1)::numeric, 1)
           ELSE 0
           END
FROM employees e
         CROSS JOIN date_range d
WHERE extract(dow from d.work_date) BETWEEN 1 AND 5;

-- Thêm phụ cấp cho cả hai nhân viên
-- Allowances for employees (10 records)
INSERT INTO allowances (employee_id, allowance_type, amount, start_date, end_date, description)
VALUES (1, 'Phụ cấp nhiên liệu', 1500000, '2023-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
       (1, 'Phụ cấp chức vụ', 3000000, '2023-01-01', NULL, 'Phụ cấp cho vị trí quản lý'),
       (2, 'Phụ cấp chức vụ', 5000000, '2021-06-01', NULL, 'Phụ cấp cho chức vụ cao hơn');

-- Additional Employment History (10 records)
INSERT INTO employment_history (employee_id, department_id, position_id, start_date, end_date, is_current,
                                transfer_reason, decision_number)
VALUES (1, 8, 1, '2020-01-01', '2021-06-30', false, 'Thăng chức', 'QD001/2020'),
       (1, 8, 2, '2021-07-01', NULL, true, 'Thăng chức', 'QD002/2021'),
       (2, 8, 3, '2021-06-01', NULL, true, 'Thăng chức', 'QD001/2021'),
       (2, 8, 2, '2019-01-01', '2021-05-31', false, 'Thăng chức', 'QD002/2019');
-- Leaves (10 records)
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, total_days, status, reason, approved_by)
VALUES (1, 'Nghỉ phép năm', '2023-07-10', '2023-07-14', 5, 'Đã duyệt', 'Nghỉ mát cùng gia đình', 2),
       (1, 'Nghỉ không lương', '2023-10-01', '2023-10-05', 5, 'Từ chối', 'Việc cá nhân', 2);
-- Performance Reviews (10 records)
INSERT INTO performance_reviews (employee_id, reviewer_id, review_period, review_year, overall_score, strengths,
                                 improvements, comments, status)
VALUES (1, 2, 'Q2', 2020, 4.5, 'Kỹ năng lãnh đạo tốt, delivery đúng hạn', 'Cần cải thiện kỹ năng giao tiếp',
        'Hoàn thành xuất sắc các mục tiêu', 'Đã duyệt'),
       (1, 2, 'Q3', 2020, 4.3, 'Quản lý dự án hiệu quả', 'Cần cải thiện deadline management', 'Hoàn thành tốt',
        'Đã duyệt'),
       (2, 1, 'Q2', 2021, 4.7, 'Lãnh đạo tốt, hoàn thành xuất sắc công việc', 'Cải thiện quản lý thời gian',
        'Thực hiện rất tốt', 'Đã duyệt'),
       (2, 1, 'Q3', 2021, 4.5, 'Quản lý dự án hiệu quả', 'Cải thiện kỹ năng giao tiếp', 'Hoàn thành tốt',
        'Đã duyệt');
-- Qualifications (10 records)
INSERT INTO qualifications (employee_id, qualification_type, qualification_name, institution, issue_date, expiry_date,
                            description)
VALUES (1, 'Bằng đại học', 'Kỹ sư CNTT', 'Đại học Bách Khoa Hà Nội', '2015-06-15', NULL,
        'Chuyên ngành Công nghệ phần mềm'),
       (1, 'Chứng chỉ', 'AWS Solution Architect', 'Amazon', '2022-01-15', '2025-01-15',
        'Chứng chỉ AWS cấp độ Professional'),
       (2, 'Bằng đại học', 'Kỹ sư Phần mềm', 'Đại học FPT', '2014-06-20', NULL, 'Chuyên ngành Công nghệ phần mềm'),
       (2, 'Chứng chỉ', 'AWS Solution Architect', 'Amazon', '2021-03-12', '2024-03-12',
        'Chứng chỉ AWS cấp độ Professional');

-- Salary Records (10 records)
INSERT INTO salary_records (employee_id, month, year, base_salary, total_allowance,
                            overtime_pay, deductions, insurance_deduction, tax_amount, net_salary, payment_status)
VALUES (1, 6, 2020, 25000000, 4500000, 1500000, 500000, 2250000, 2800000, 25450000, 'Đã thanh toán'),
       (1, 7, 2020, 25000000, 4500000, 2000000, 500000, 2250000, 2900000, 25850000, 'Đã thanh toán'),
       (2, 6, 2021, 12000000, 5000000, 2000000, 600000, 2500000, 3200000, 16000000, 'Đã thanh toán'),
       (2, 7, 2021, 12000000, 5000000, 1800000, 600000, 2500000, 3100000, 15800000, 'Đã thanh toán');

-- Insert basic employee information
INSERT INTO employees (employee_id, employee_code, first_name, last_name, birth_date, gender,
                       id_number, permanent_address, temporary_address,
                       personal_email, company_email, phone_number,
                       marital_status, bank_account, bank_name, tax_code)
VALUES (3, 'NV003', 'Lê', 'Thị Mai', '1992-08-25', 'Nữ',
        '003092123456', '789 Điện Biên Phủ, Q.3, TP.HCM', '789 Điện Biên Phủ, Q.3, TP.HCM',
        'lethimai@gmail.com', 'mai.le@company.com', '0903456789',
        'Độc thân', '3456789012', 'Techcombank', '0345678901');

-- Insert user account
INSERT INTO users (employee_id, username, password_hash)
VALUES (3, 'maile', '1');

-- Insert employment history
INSERT INTO employment_history (employee_id, department_id, position_id, start_date, end_date,
                                is_current, transfer_reason, decision_number)
VALUES (3, 13, 9, '2021-07-01', NULL,
        true, NULL, 'QD001/2021'),
       (3, 14, 9, '2020-01-15', '2021-06-30',
        false, 'Chuyển đổi vị trí công tác', 'QD002/2020');

-- Insert contracts
INSERT INTO contracts (employee_id, contract_type, contract_code,
                       start_date, end_date, base_salary,
                       sign_date, is_present)
VALUES (3, 'Thử việc', 'HD001/2020-TV',
        '2020-01-15', '2020-04-15', 8000000,
        '2020-01-10', false),
       (3, '1 năm', 'HD002/2020',
        '2020-04-15', '2021-04-15', 10000000,
        '2020-04-10', false),
       (3, '2 năm', 'HD003/2021',
        '2021-04-15', '2023-04-15', 15000000,
        '2021-04-10', true);

-- Insert allowances
INSERT INTO allowances (employee_id, allowance_type, amount, start_date, end_date, description)
VALUES (3, 'Phụ cấp ăn trưa', 1000000, '2020-01-15', NULL, 'Phụ cấp ăn trưa hàng tháng'),
       (3, 'Phụ cấp điện thoại', 500000, '2020-01-15', NULL, 'Phụ cấp điện thoại hàng tháng'),
       (3, 'Phụ cấp xăng xe', 800000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng');

-- Insert attendance records for current month (similar to existing pattern)
WITH date_range AS (SELECT generate_series(
                                   date_trunc('month', CURRENT_DATE) + '1 day'::interval,
                                   date_trunc('month', CURRENT_DATE) + '365 days'::interval,
                                   '1 day'::interval
                           )::date AS work_date)
INSERT
INTO attendance (employee_id, date, check_in, check_out, status, overtime_hours)
SELECT 3,
       d.work_date,
       d.work_date + '08:00:00'::interval + (random() * interval '30 minutes'),
       d.work_date + '17:30:00'::interval + (random() * interval '90 minutes'),
       CASE
           WHEN random() < 0.9 THEN 'Đúng giờ'
           ELSE 'Đi muộn'
           END,
       CASE
           WHEN random() < 0.3 THEN round((random() * 2)::numeric, 1)
           ELSE 0
           END
FROM date_range d
WHERE extract(dow from d.work_date) BETWEEN 1 AND 5;

-- Insert leaves
INSERT INTO leaves (employee_id, leave_type, start_date, end_date,
                    total_days, status, reason, approved_by)
VALUES (3, 'Nghỉ phép năm', '2023-07-10', '2023-07-12',
        3, 'Đã duyệt', 'Về quê giải quyết việc gia đình', 1),
       (3, 'Nghỉ ốm', '2023-10-05', '2023-10-06',
        2, 'Đã duyệt', 'Nghỉ ốm có giấy bác sĩ', 1);

-- Insert salary records
INSERT INTO salary_records (employee_id, month, year, base_salary,
                            total_allowance, overtime_pay, deductions,
                            insurance_deduction, tax_amount, net_salary, payment_status)
VALUES (3, 1, 2024, 15000000,
        2300000, 450000, 0,
        1500000, 750000, 15500000, 'Đã thanh toán'),
       (3, 12, 2023, 15000000,
        2300000, 600000, 0,
        1500000, 750000, 15650000, 'Đã thanh toán');

-- Insert performance reviews
INSERT INTO performance_reviews (employee_id, reviewer_id, review_period,
                                 review_year, overall_score, strengths,
                                 improvements, comments, status)
VALUES (3, 1, 'Q4',
        2023, 4.2, 'Kỹ năng giao tiếp tốt, khả năng thích nghi cao',
        'Cần cải thiện kỹ năng quản lý thời gian', 'Nhân viên có tiềm năng phát triển', 'Hoàn thành');

-- Insert qualifications
INSERT INTO qualifications (employee_id, qualification_type, qualification_name,
                            institution, issue_date, expiry_date, description)
VALUES (3, 'Bằng đại học', 'Cử nhân Marketing',
        'Đại học Kinh tế TP.HCM', '2014-06-15', NULL, 'Tốt nghiệp loại Giỏi'),
       (3, 'Chứng chỉ', 'Digital Marketing Professional',
        'Google', '2020-03-20', '2023-03-20', 'Google Digital Marketing Certification');

-- Insert dependents
INSERT INTO dependents (employee_id, full_name, relationship,
                        birth_date, id_number, is_tax_dependent)
VALUES (3, 'Lê Văn Nam', 'Con',
        '2019-05-15', NULL, true);

select de
from employment_history de
where employee_id = 1
  and is_current = true