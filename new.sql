-- Xoá toàn bộ schema và tất cả các đối tượng bên trong
DROP SCHEMA public CASCADE;
-- Tạo lại schema public sau khi đã xoá
Create SCHEMA public;

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
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
Create TABLE leaves
(
    leave_id    SERIAL PRIMARY KEY,
    employee_id INTEGER REFERENCES employees (employee_id),
    leave_type  VARCHAR(50) NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    total_days  INTEGER     NOT NULL,
    status      VARCHAR(20) NOT NULL,
    reason      TEXT,
    approved_by INTEGER REFERENCES employees (employee_id),
    Created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    Created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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
    user_id       SERIAL PRIMARY KEY,
    employee_id   INTEGER REFERENCES employees (employee_id),
    username      text NOT NULL,
    password_hash text NOT NULL,
    email         text,
    role          text      default 'ADMIN',
    status        text default 'Active',
    last_Authentication    TIMESTAMP DEFAULT NULL,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng nhật ký hệ thống
Create TABLE audit_logs
(
    log_id      SERIAL PRIMARY KEY,
    user_id     INTEGER REFERENCES users (user_id),
    action_type text,
    action_info text,
    action_level text,
    Created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
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

-- Thêm dữ liệu nhân viên
INSERT INTO employees (employee_code, first_name, last_name, birth_date, gender,
                       id_number, permanent_address, temporary_address,
                       personal_email, company_email, phone_number,
                       marital_status, bank_account, bank_name, tax_code)
VALUES
    -- Ban lãnh đạo
    ('GD001', 'Nguyễn', 'Văn An', '1975-05-15', 'Nam',
     '001075123456', '123 Lê Lợi, Q.1, TP.HCM', '123 Lê Lợi, Q.1, TP.HCM',
     'nguyenvanan@gmail.com', 'an.nguyen@company.com', '0901234567',
     'Đã kết hôn', '1234567890', 'Vietcombank', '0123456789'),

    ('PGD001', 'Trần', 'Bảo Hùng', '1978-11-10', 'Nam',
     '002078123456', '456 Nguyễn Thị Minh Khai, Q.3, TP.HCM', '456 Nguyễn Thị Minh Khai, Q.3, TP.HCM',
     'tranbaohung@gmail.com', 'hung.tran@company.com', '0902345678',
     'Đã kết hôn', '2345678901', 'Vietcombank', '0234567890'),

    -- Trưởng phòng
    ('TP001', 'Lê', 'Thị Hương', '1982-03-25', 'Nữ',
     '003082123456', '789 Nguyễn Huệ, Q.1, TP.HCM', '789 Nguyễn Huệ, Q.1, TP.HCM',
     'lethihuong@gmail.com', 'huong.le@company.com', '0903456789',
     'Độc thân', '3456789012', 'Techcombank', '0345678901'),

    ('TP002', 'Phạm', 'Minh Đức', '1980-07-12', 'Nam',
     '004080123456', '101 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM', '101 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM',
     'phamminhduc@gmail.com', 'duc.pham@company.com', '0904567890',
     'Đã kết hôn', '4567890123', 'BIDV', '0456789012'),

    ('TP003', 'Võ', 'Thanh Tùng', '1981-11-05', 'Nam',
     '005081123456', '202 Võ Văn Ngân, Q.Thủ Đức, TP.HCM', '202 Võ Văn Ngân, Q.Thủ Đức, TP.HCM',
     'vothanhtung@gmail.com', 'tung.vo@company.com', '0905678901',
     'Độc thân', '5678901234', 'ACB', '0567890123'),

    ('TP004', 'Nguyễn', 'Thị Mai', '1983-09-18', 'Nữ',
     '006083123456', '303 Cách Mạng Tháng 8, Q.10, TP.HCM', '303 Cách Mạng Tháng 8, Q.10, TP.HCM',
     'nguyenthimai@gmail.com', 'mai.nguyen@company.com', '0906789012',
     'Đã kết hôn', '6789012345', 'Sacombank', '0678901234'),

    ('TP005', 'Trần', 'Văn Hiếu', '1979-04-30', 'Nam',
     '007079123456', '404 Nguyễn Văn Linh, Q.7, TP.HCM', '404 Nguyễn Văn Linh, Q.7, TP.HCM',
     'tranvanhieu@gmail.com', 'hieu.tran@company.com', '0907890123',
     'Đã kết hôn', '7890123456', 'VPBank', '0789012345'),

    -- Nhân viên
    ('NV001', 'Đỗ', 'Thị Hà', '1990-02-14', 'Nữ',
     '008090123456', '505 Lý Tự Trọng, Q.1, TP.HCM', '505 Lý Tự Trọng, Q.1, TP.HCM',
     'dothiha@gmail.com', 'ha.do@company.com', '0908901234',
     'Độc thân', '8901234567', 'MB Bank', '0890123456'),

    ('NV002', 'Hoàng', 'Minh Tuấn', '1988-12-01', 'Nam',
     '009088123456', '606 Lê Duẩn, Q.1, TP.HCM', '606 Lê Duẩn, Q.1, TP.HCM',
     'hoangminhtuan@gmail.com', 'tuan.hoang@company.com', '0909012345',
     'Đã kết hôn', '9012345678', 'OCB', '0901234567'),

    ('NV003', 'Lý', 'Thanh Thảo', '1991-06-20', 'Nữ',
     '010091123456', '707 Trần Hưng Đạo, Q.5, TP.HCM', '707 Trần Hưng Đạo, Q.5, TP.HCM',
     'lythanhthao@gmail.com', 'thao.ly@company.com', '0910123456',
     'Độc thân', '0123456789', 'TPBank', '1012345678'),

    ('NV004', 'Nguyễn', 'Văn Bình', '1992-08-25', 'Nam',
     '011092123456', '808 Nguyễn Trãi, Q.5, TP.HCM', '808 Nguyễn Trãi, Q.5, TP.HCM',
     'nguyenvanbinh@gmail.com', 'binh.nguyen@company.com', '0911234567',
     'Độc thân', '1234567891', 'Vietinbank', '1123456789'),

    ('NV005', 'Trần', 'Thị Lan', '1993-03-15', 'Nữ',
     '012093123456', '909 Lê Hồng Phong, Q.10, TP.HCM', '909 Lê Hồng Phong, Q.10, TP.HCM',
     'tranthilan@gmail.com', 'lan.tran@company.com', '0912345678',
     'Đã kết hôn', '2345678912', 'BIDV', '1234567890'),

    ('NV006', 'Lê', 'Văn Cường', '1989-09-05', 'Nam',
     '013089123456', '101 Nguyễn Đình Chiểu, Q.3, TP.HCM', '101 Nguyễn Đình Chiểu, Q.3, TP.HCM',
     'levancuong@gmail.com', 'cuong.le@company.com', '0913456789',
     'Đã kết hôn', '3456789123', 'ACB', '1345678901'),

    ('NV007', 'Phạm', 'Thị Hồng', '1994-11-20', 'Nữ',
     '014094123456', '202 Điện Biên Phủ, Q.1, TP.HCM', '202 Điện Biên Phủ, Q.1, TP.HCM',
     'phamthihong@gmail.com', 'hong.pham@company.com', '0914567890',
     'Độc thân', '4567891234', 'Sacombank', '1456789012'),

    ('NV008', 'Vũ', 'Đình Trung', '1987-07-30', 'Nam',
     '015087123456', '303 Nguyễn Công Trứ, Q.1, TP.HCM', '303 Nguyễn Công Trứ, Q.1, TP.HCM',
     'vudinhtrung@gmail.com', 'trung.vu@company.com', '0915678901',
     'Đã kết hôn', '5678912345', 'VPBank', '1567890123');

-- Thêm tài khoản người dùng
INSERT INTO users (employee_id, username, password_hash)
VALUES
    (1, 'annguyen', '1'),
    (2, 'hungtran', '1'),
    (3, 'huongle', '1'),
    (4, 'ducpham', '1'),
    (5, 'tungvo', '1'),
    (6, 'mainguyen', '1'),
    (7, 'hieutran', '1'),
    (8, 'hado', '1'),
    (9, 'tuanhoang', '1'),
    (10, 'thaoly', '1'),
    (11, 'binhnguyen', '1'),
    (12, 'lantran', '1'),
    (13, 'cuongle', '1'),
    (14, 'hongpham', '1'),
    (15, 'trungvu', '1');

-- Thêm phòng ban (với manager_id)
INSERT INTO departments (department_name, department_code, description, manager_id)
VALUES
    ('Ban Giám đốc', 'BGD', 'Ban lãnh đạo công ty', 1),

    ('Phòng Phát triển Phần mềm', 'DEV', 'Phòng phát triển ứng dụng và phần mềm', 3),
    ('Phòng An ninh Thông tin', 'SEC', 'Phòng đảm bảo bảo mật cho hệ thống và dữ liệu', 5),
    ('Phòng Kiểm thử và Đảm bảo Chất lượng', 'QA', 'Phòng kiểm thử phần mềm và đảm bảo chất lượng', 7),
    ('Phòng Quản trị Hệ thống', 'SYS', 'Quản lý cơ sở hạ tầng công nghệ thông tin', 4),
    ('Phòng Phân tích Dữ liệu', 'DATA', 'Phòng phân tích dữ liệu và hỗ trợ quyết định', 6),

    ('Phòng Kinh doanh', 'SLS', 'Phòng tiếp cận và chăm sóc khách hàng', 2),
    ('Phòng Marketing', 'MKT', 'Phòng xây dựng chiến lược marketing và truyền thông', 3),
    ('Phòng Hỗ trợ Khách hàng', 'SUP', 'Phòng hỗ trợ và giải quyết khiếu nại khách hàng', 6),

    ('Phòng Nhân sự', 'HRM', 'Quản lý tuyển dụng và phát triển nhân sự', 4),
    ('Phòng Hành chính', 'ADM', 'Quản lý các công việc hành chính văn phòng', 7),

    ('Phòng Kế toán', 'ACC', 'Quản lý kế toán và báo cáo tài chính', 5),
    ('Phòng Phân tích Tài chính', 'FIN-ANAL', 'Phân tích tài chính và lập báo cáo tài chính', 4),

    ('Phòng Nghiên cứu', 'RES', 'Nghiên cứu công nghệ và xu hướng mới', 3),
    ('Phòng Phát triển Sản phẩm', 'PROD', 'Phát triển sản phẩm và chuyển giao từ nghiên cứu', 2);

-- Thêm chức vụ
INSERT INTO positions (position_name, department_id, position_code, level, description)
VALUES
    -- Ban Giám đốc
    ('Giám đốc', 1, 'GD_POS001', 5, 'Quản lý cao nhất của công ty'),
    ('Phó Giám đốc', 1, 'GD_POS002', 4, 'Hỗ trợ Giám đốc trong quản lý công ty'),

    -- Khối Công nghệ
    ('Trưởng phòng Phát triển', 2, 'DEV_POS001', 3, 'Quản lý phòng phát triển phần mềm'),
    ('Lập trình viên Senior', 2, 'DEV_POS002', 2, 'Lập trình viên cấp cao'),
    ('Lập trình viên Junior', 2, 'DEV_POS003', 1, 'Lập trình viên cấp thấp'),

    ('Trưởng phòng An ninh', 3, 'SEC_POS001', 3, 'Quản lý phòng an ninh thông tin'),
    ('Chuyên viên An ninh', 3, 'SEC_POS002', 2, 'Chuyên viên bảo mật'),

    ('Trưởng phòng QA', 4, 'QA_POS001', 3, 'Quản lý phòng kiểm thử'),
    ('Tester Senior', 4, 'QA_POS002', 2, 'Kiểm thử viên cấp cao'),
    ('Tester Junior', 4, 'QA_POS003', 1, 'Kiểm thử viên cấp thấp'),

    ('Trưởng phòng Hệ thống', 5, 'SYS_POS001', 3, 'Quản lý phòng hệ thống'),
    ('Quản trị viên Hệ thống', 5, 'SYS_POS002', 2, 'Quản trị hệ thống'),

    ('Trưởng phòng Dữ liệu', 6, 'DATA_POS001', 3, 'Quản lý phòng phân tích dữ liệu'),
    ('Chuyên viên Phân tích Dữ liệu', 6, 'DATA_POS002', 2, 'Phân tích dữ liệu'),

    -- Khối Kinh doanh
    ('Trưởng phòng Kinh doanh', 7, 'SLS_POS001', 3, 'Quản lý phòng kinh doanh'),
    ('Nhân viên Kinh doanh Senior', 7, 'SLS_POS002', 2, 'Nhân viên kinh doanh cấp cao'),
    ('Nhân viên Kinh doanh Junior', 7, 'SLS_POS003', 1, 'Nhân viên kinh doanh cấp thấp'),

    ('Trưởng phòng Marketing', 8, 'MKT_POS001', 3, 'Quản lý phòng marketing'),
    ('Chuyên viên Marketing', 8, 'MKT_POS002', 2, 'Chuyên viên marketing'),

    ('Trưởng phòng CSKH', 9, 'SUP_POS001', 3, 'Quản lý phòng hỗ trợ khách hàng'),
    ('Nhân viên CSKH', 9, 'SUP_POS002', 1, 'Nhân viên hỗ trợ khách hàng'),

    -- Khối Hành chính - Nhân sự
    ('Trưởng phòng Nhân sự', 10, 'HRM_POS001', 3, 'Quản lý phòng nhân sự'),
    ('Chuyên viên Nhân sự', 10, 'HRM_POS002', 2, 'Chuyên viên nhân sự'),

    ('Trưởng phòng Hành chính', 11, 'ADM_POS001', 3, 'Quản lý phòng hành chính'),
    ('Nhân viên Hành chính', 11, 'ADM_POS002', 1, 'Nhân viên hành chính'),

    -- Khối Tài chính
    ('Trưởng phòng Kế toán', 12, 'ACC_POS001', 3, 'Quản lý phòng kế toán'),
    ('Kế toán viên', 12, 'ACC_POS002', 2, 'Kế toán viên'),

    ('Trưởng phòng Tài chính', 13, 'FIN_POS001', 3, 'Quản lý phòng tài chính'),
    ('Chuyên viên Phân tích Tài chính', 13, 'FIN_POS002', 2, 'Chuyên viên phân tích tài chính'),

    -- Khối Nghiên cứu và Phát triển
    ('Trưởng phòng Nghiên cứu', 14, 'RES_POS001', 3, 'Quản lý phòng nghiên cứu'),
    ('Nghiên cứu viên', 14, 'RES_POS002', 2, 'Nghiên cứu viên'),

    ('Trưởng phòng Phát triển Sản phẩm', 15, 'PROD_POS001', 3, 'Quản lý phòng phát triển sản phẩm'),
    ('Chuyên viên Phát triển Sản phẩm', 15, 'PROD_POS002', 2, 'Chuyên viên phát triển sản phẩm');


INSERT INTO contracts (employee_id, contract_type, contract_code,
                       start_date, end_date, base_salary,
                       sign_date, is_present)
VALUES
    -- Ban lãnh đạo
    (1, '5 năm', 'HD001/2020', '2020-01-01', '2025-01-01', 50000000, '2019-12-15', true),
    (2, '5 năm', 'HD002/2020', '2020-01-01', '2025-01-01', 40000000, '2019-12-15', true),

    -- Trưởng phòng
    (3, '3 năm', 'HD003/2021', '2021-01-01', '2024-01-01', 30000000, '2020-12-15', true),
    (4, '3 năm', 'HD004/2021', '2021-01-01', '2024-01-01', 30000000, '2020-12-15', true),
    (5, '3 năm', 'HD005/2021', '2021-01-01', '2024-01-01', 30000000, '2020-12-15', true),
    (6, '3 năm', 'HD006/2021', '2021-01-01', '2024-01-01', 30000000, '2020-12-15', true),
    (7, '3 năm', 'HD007/2021', '2021-01-01', '2024-01-01', 30000000, '2020-12-15', true),

    -- Nhân viên
    (8, 'Thử việc', 'HD008/2022', '2022-01-01', '2022-04-01', 10000000, '2021-12-20', false),
    (8, '1 năm', 'HD009/2022', '2022-04-01', '2023-04-01', 12000000, '2022-03-25', false),
    (8, '2 năm', 'HD010/2023', '2023-04-01', '2025-04-01', 15000000, '2023-03-20', true),

    (9, '2 năm', 'HD011/2022', '2022-01-01', '2024-01-01', 18000000, '2021-12-15', true),

    (10, 'Thử việc', 'HD012/2022', '2022-06-01', '2022-09-01', 9000000, '2022-05-25', false),
    (10, '1 năm', 'HD013/2022', '2022-09-01', '2023-09-01', 11500000, '2022-08-20', false),
    (10, '2 năm', 'HD014/2023', '2023-09-01', '2025-09-01', 14000000, '2023-08-25', true),

    (11, '2 năm', 'HD015/2022', '2022-03-01', '2024-03-01', 16000000, '2022-02-15', true),

    (12, '2 năm', 'HD016/2022', '2022-05-01', '2024-05-01', 15000000, '2022-04-15', true),

    (13, '2 năm', 'HD017/2022', '2022-04-01', '2024-04-01', 16500000, '2022-03-15', true),

    (14, 'Thử việc', 'HD018/2023', '2023-01-01', '2023-04-01', 9500000, '2022-12-20', false),
    (14, '1 năm', 'HD019/2023', '2023-04-01', '2024-04-01', 12500000, '2023-03-25', true),

    (15, '2 năm', 'HD020/2022', '2022-07-01', '2024-07-01', 17000000, '2022-06-15', true);

-- Thêm lịch sử công tác
INSERT INTO employment_history (employee_id, department_id, position_id, start_date, end_date, is_current,
                                transfer_reason, decision_number)
VALUES
    -- Ban lãnh đạo
    (1, 1, 1, '2020-01-01', NULL, true, 'Bổ nhiệm', 'QD001/2020'),
    (2, 1, 2, '2020-01-01', NULL, true, 'Bổ nhiệm', 'QD002/2020'),

    -- Trưởng phòng
    (3, 2, 3, '2021-01-01', NULL, true, 'Bổ nhiệm', 'QD003/2021'),
    (4, 5, 11, '2021-01-01', NULL, true, 'Bổ nhiệm', 'QD004/2021'),
    (5, 3, 6, '2021-01-01', NULL, true, 'Bổ nhiệm', 'QD005/2021'),
    (6, 6, 13, '2021-01-01', NULL, true, 'Bổ nhiệm', 'QD006/2021'),
    (7, 4, 8, '2021-01-01', NULL, true, 'Bổ nhiệm', 'QD007/2021'),

    -- Nhân viên
    (8, 10, 23, '2022-01-01', NULL, true, 'Tuyển dụng mới', 'QD008/2022'),

    (9, 7, 16, '2022-01-01', NULL, true, 'Tuyển dụng mới', 'QD009/2022'),

    (10, 4, 9, '2022-06-01', NULL, true, 'Tuyển dụng mới', 'QD010/2022'),

    (11, 2, 4, '2022-03-01', NULL, true, 'Tuyển dụng mới', 'QD011/2022'),

    (12, 6, 14, '2022-05-01', NULL, true, 'Tuyển dụng mới', 'QD012/2022'),

    (13, 7, 17, '2022-04-01', NULL, true, 'Tuyển dụng mới', 'QD013/2022'),

    (14, 12, 28, '2023-01-01', NULL, true, 'Tuyển dụng mới', 'QD014/2023'),

    (15, 3, 7, '2022-07-01', NULL, true, 'Tuyển dụng mới', 'QD015/2022');

-- Thêm người phụ thuộc
INSERT INTO dependents (employee_id, full_name, relationship,
                        birth_date, id_number, is_tax_dependent)
VALUES
    -- Giám đốc
    (1, 'Nguyễn Thị Hà', 'Vợ', '1980-08-20', '001080123456', true),
    (1, 'Nguyễn Minh Khôi', 'Con', '2005-03-10', NULL, true),
    (1, 'Nguyễn Minh Anh', 'Con', '2010-11-15', NULL, true),

    -- Phó Giám đốc
    (2, 'Lê Thị Lan', 'Vợ', '1982-05-15', '002082123456', true),
    (2, 'Trần Bảo Nam', 'Con', '2008-09-20', NULL, true),

    -- Trưởng phòng
    (4, 'Nguyễn Thị Hồng', 'Vợ', '1983-07-25', '004083123456', true),
    (4, 'Phạm Minh Quân', 'Con', '2012-09-12', NULL, true),

    (5, 'Lê Thị Ngọc', 'Vợ', '1984-10-30', '005084123456', true),
    (5, 'Võ Minh Trí', 'Con', '2013-04-22', NULL, true),

    (6, 'Trần Văn Nam', 'Chồng', '1980-12-05', '006080123456', true),
    (6, 'Nguyễn Thị Linh', 'Con', '2015-03-18', NULL, true),

    (7, 'Phạm Thị Lan', 'Vợ', '1982-05-15', '007082123456', true),
    (7, 'Trần Minh Khôi', 'Con', '2014-08-20', NULL, true),

    -- Nhân viên
    (9, 'Vũ Thị Ngọc', 'Vợ', '1989-10-30', '009089123456', true),
    (9, 'Hoàng Minh Trí', 'Con', '2016-04-22', NULL, true),

    (11, 'Lê Thị Hương', 'Vợ', '1992-08-20', '011092123456', true),
    (11, 'Nguyễn Văn Cường', 'Con', '2018-03-10', NULL, true),

    (12, 'Nguyễn Văn Hùng', 'Chồng', '1990-12-05', '012090123456', true),

    (13, 'Trần Thị Mai', 'Vợ', '1990-07-25', '013090123456', true),
    (13, 'Lê Minh Quân', 'Con', '2017-09-12', NULL, true),

    (15, 'Nguyễn Thị Hoa', 'Vợ', '1989-10-30', '015089123456', true);

-- Thêm phụ cấp
INSERT INTO allowances (employee_id, allowance_type, amount, start_date, end_date, description)
VALUES
    -- Ban lãnh đạo
    (1, 'Phụ cấp chức vụ', 10000000, '2020-01-01', NULL, 'Phụ cấp cho vị trí Giám đốc'),
    (1, 'Phụ cấp nhiên liệu', 3000000, '2020-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (1, 'Phụ cấp điện thoại', 1000000, '2020-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (2, 'Phụ cấp chức vụ', 8000000, '2020-01-01', NULL, 'Phụ cấp cho vị trí Phó Giám đốc'),
    (2, 'Phụ cấp nhiên liệu', 2500000, '2020-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (2, 'Phụ cấp điện thoại', 800000, '2020-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    -- Trưởng phòng
    (3, 'Phụ cấp chức vụ', 5000000, '2021-01-01', NULL, 'Phụ cấp cho vị trí Trưởng phòng'),
    (3, 'Phụ cấp nhiên liệu', 1500000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (3, 'Phụ cấp điện thoại', 500000, '2021-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (4, 'Phụ cấp chức vụ', 5000000, '2021-01-01', NULL, 'Phụ cấp cho vị trí Trưởng phòng'),
    (4, 'Phụ cấp nhiên liệu', 1500000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (4, 'Phụ cấp điện thoại', 500000, '2021-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (5, 'Phụ cấp chức vụ', 5000000, '2021-01-01', NULL, 'Phụ cấp cho vị trí Trưởng phòng'),
    (5, 'Phụ cấp nhiên liệu', 1500000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (5, 'Phụ cấp điện thoại', 500000, '2021-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (6, 'Phụ cấp chức vụ', 5000000, '2021-01-01', NULL, 'Phụ cấp cho vị trí Trưởng phòng'),
    (6, 'Phụ cấp nhiên liệu', 1500000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (6, 'Phụ cấp điện thoại', 500000, '2021-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (7, 'Phụ cấp chức vụ', 5000000, '2021-01-01', NULL, 'Phụ cấp cho vị trí Trưởng phòng'),
    (7, 'Phụ cấp nhiên liệu', 1500000, '2021-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (7, 'Phụ cấp điện thoại', 500000, '2021-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    -- Nhân viên
    (8, 'Phụ cấp ăn trưa', 800000, '2022-01-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (8, 'Phụ cấp điện thoại', 300000, '2022-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (9, 'Phụ cấp ăn trưa', 800000, '2022-01-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (9, 'Phụ cấp nhiên liệu', 1000000, '2022-01-01', NULL, 'Phụ cấp xăng xe hàng tháng'),
    (9, 'Phụ cấp điện thoại', 300000, '2022-01-01', NULL, 'Phụ cấp điện thoại hàng tháng'),

    (10, 'Phụ cấp ăn trưa', 800000, '2022-06-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (10, 'Phụ cấp dự án', 2000000, '2023-01-01', '2023-06-30', 'Phụ cấp cho dự án đặc biệt'),

    (11, 'Phụ cấp ăn trưa', 800000, '2022-03-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (11, 'Phụ cấp chuyên môn', 1500000, '2022-03-01', NULL, 'Phụ cấp cho kỹ năng lập trình'),

    (12, 'Phụ cấp ăn trưa', 800000, '2022-05-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (12, 'Phụ cấp chuyên môn', 1200000, '2022-05-01', NULL, 'Phụ cấp cho kỹ năng phân tích dữ liệu'),

    (13, 'Phụ cấp ăn trưa', 800000, '2022-04-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (13, 'Phụ cấp nhiên liệu', 1000000, '2022-04-01', NULL, 'Phụ cấp xăng xe hàng tháng'),

    (14, 'Phụ cấp ăn trưa', 800000, '2023-01-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),

    (15, 'Phụ cấp ăn trưa', 800000, '2022-07-01', NULL, 'Phụ cấp ăn trưa hàng tháng'),
    (15, 'Phụ cấp chuyên môn', 1300000, '2022-07-01', NULL, 'Phụ cấp cho kỹ năng bảo mật');

-- Thêm nghỉ phép
INSERT INTO leaves (employee_id, leave_type, start_date, end_date, total_days, status, reason, approved_by)
VALUES
    (1, 'Nghỉ phép năm', '2023-07-10', '2023-07-14', 5, 'Đã duyệt', 'Nghỉ mát cùng gia đình', 2),

    (3, 'Nghỉ phép năm', '2023-05-15', '2023-05-19', 5, 'Đã duyệt', 'Nghỉ du lịch', 1),
    (3, 'Nghỉ ốm', '2023-08-10', '2023-08-11', 2, 'Đã duyệt', 'Bị cảm cúm', 1),

    (5, 'Nghỉ phép năm', '2023-07-03', '2023-07-07', 5, 'Đã duyệt', 'Nghỉ mát cùng gia đình', 1),

    (6, 'Nghỉ không lương', '2023-09-18', '2023-09-22', 5, 'Đã duyệt', 'Việc gia đình', 1),

    (7, 'Nghỉ phép năm', '2023-04-24', '2023-04-28', 5, 'Đã duyệt', 'Nghỉ du lịch', 1),

    (8, 'Nghỉ phép năm', '2023-06-19', '2023-06-23', 5, 'Đã duyệt', 'Nghỉ du lịch', 3),

    (10, 'Nghỉ ốm', '2023-10-05', '2023-10-06', 2, 'Đã duyệt', 'Bị sốt', 7),

    (11, 'Nghỉ phép năm', '2023-08-21', '2023-08-25', 5, 'Đã duyệt', 'Nghỉ du lịch', 3),

    (13, 'Nghỉ không lương', '2023-11-13', '2023-11-17', 5, 'Đã duyệt', 'Việc gia đình', 2),

    (14, 'Nghỉ ốm', '2023-09-07', '2023-09-08', 2, 'Đã duyệt', 'Bị cảm cúm', 4),

    (15, 'Nghỉ phép năm', '2023-10-16', '2023-10-20', 5, 'Đã duyệt', 'Nghỉ du lịch', 5);
-- Generate attendance records for all employees (20 days each)
WITH date_range AS (
    SELECT generate_series(
                   (date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Ho_Chi_Minh'))::date), -- First day of month
                   (date_trunc('month', (CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Ho_Chi_Minh'))::date + INTERVAL '60 days'), -- +60 days
                   INTERVAL '1 day'
           )::date AS work_date
)
INSERT INTO attendance (employee_id, date, check_in, check_out, status, overtime_hours)
SELECT
    e.employee_id,
    d.work_date,
    CASE
        WHEN random() < 0.5 THEN '08:00:00'::time
        ELSE '08:30:00'::time
        END AS check_in,
    CASE
        WHEN random() < 0.5 THEN '18:00:00'::time
        ELSE '18:30:00'::time
        END AS check_out,
    CASE
        WHEN random() < 0.9 THEN 'Đúng giờ'
        ELSE 'Đi muộn ' || (5 + (random() * 25)::integer)::text || 'p'
        END AS status,
    CASE
        WHEN random() < 0.3 THEN round((random() * 2)::numeric, 1)
        ELSE 0
        END AS overtime_hours
FROM employees e
         CROSS JOIN date_range d
WHERE EXTRACT(DOW FROM d.work_date) BETWEEN 1 AND 5;  -- Weekdays only


-- Tiếp tục thêm dữ liệu lương
INSERT INTO salary_records (employee_id, month, year, base_salary, total_allowance,
                            overtime_pay, deductions, insurance_deduction, tax_amount, net_salary, payment_status)
VALUES
    -- Tiếp tục từ nhân viên 15
    (15, 1, 2023, 17000000, 2100000, 1700000, 0, 1700000, 2100000, 17000000, 'Đã thanh toán'),
    (15, 2, 2023, 17000000, 2100000, 1500000, 0, 1700000, 2000000, 16900000, 'Đã thanh toán'),
    (14, 1, 2023, 12500000, 2100000, 12500000, 0, 12500000, 2100000, 12500000, 'Đã thanh toán'),
    (13, 1, 2023, 16500000, 2100000, 16500000, 0, 16500000, 2100000, 16500000, 'Đã thanh toán'),
    (12, 1, 2023, 15000000, 2100000, 15000000, 0, 15000000, 2100000, 15000000, 'Đã thanh toán'),
    (11, 1, 2023, 16000000, 2100000, 16000000, 0, 16000000, 2100000, 16000000, 'Đã thanh toán'),
    (10, 1, 2023, 14000000, 2100000, 14000000, 0, 14000000, 2100000, 14000000, 'Đã thanh toán'),
    (9, 1, 2023, 18000000, 2100000, 18000000, 0, 18000000, 2100000, 18000000, 'Đã thanh toán'),
    (8, 1, 2023, 15000000, 2100000, 15000000, 0, 15000000, 2100000, 15000000, 'Đã thanh toán'),
    (7, 1, 2023, 30000000, 2100000, 30000000, 0, 30000000, 2100000, 30000000, 'Đã thanh toán'),
    (6, 1, 2023, 30000000, 2100000, 30000000, 0, 30000000, 2100000, 30000000, 'Đã thanh toán'),
    (5, 1, 2023, 30000000, 2100000, 30000000, 0, 30000000, 2100000, 30000000, 'Đã thanh toán'),
    (4, 1, 2023, 30000000, 2100000, 30000000, 0, 30000000, 2100000, 30000000, 'Đã thanh toán'),
    (3, 1, 2023, 30000000, 2100000, 30000000, 0, 30000000, 2100000, 30000000, 'Đã thanh toán'),
    (2, 1, 2023, 40000000, 2100000, 40000000, 0, 40000000, 2100000, 40000000, 'Đã thanh toán'),
    (1, 1, 2023, 50000000, 2100000, 50000000, 0, 50000000, 2100000, 50000000, 'Đã thanh toán'),

;

-- Thêm nhật ký hệ thống
INSERT INTO audit_logs (user_id, action_type, action_info, action_level)
VALUES
    (1, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (1, 'Create', 'Tạo yêu cầu kết toán lương tháng 3/2023', 'INFO'),
    (2, 'APPROVE', 'Phê duyệt yêu cầu kết toán lương tháng 3/2023', 'INFO'),
    (1, 'Create', 'Tạo yêu cầu kết toán lương tháng 4/2023', 'INFO'),
    (2, 'APPROVE', 'Phê duyệt yêu cầu kết toán lương tháng 4/2023', 'INFO'),

    (3, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (3, 'Create', 'Tạo yêu cầu nghỉ phép', 'INFO'),
    (1, 'APPROVE', 'Phê duyệt yêu cầu nghỉ phép của Lê Thị Hương', 'INFO'),

    (5, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (5, 'Create', 'Tạo yêu cầu nghỉ phép', 'INFO'),
    (1, 'APPROVE', 'Phê duyệt yêu cầu nghỉ phép của Võ Thanh Tùng', 'INFO'),

    (6, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (6, 'Create', 'Tạo yêu cầu nghỉ không lương', 'INFO'),
    (1, 'APPROVE', 'Phê duyệt yêu cầu nghỉ không lương của Nguyễn Thị Mai', 'INFO'),

    (8, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (8, 'Create', 'Tạo yêu cầu nghỉ phép', 'INFO'),
    (3, 'APPROVE', 'Phê duyệt yêu cầu nghỉ phép của Đỗ Thị Hà', 'INFO'),

    (9, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (9, 'Create', 'Tạo yêu cầu tăng lương', 'INFO'),

    (10, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (10, 'Create', 'Tạo yêu cầu nghỉ ốm', 'INFO'),
    (7, 'APPROVE', 'Phê duyệt yêu cầu nghỉ ốm của Lý Thanh Thảo', 'INFO'),

    (11, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (11, 'Create', 'Tạo yêu cầu tăng lương', 'INFO'),
    (3, 'APPROVE', 'Phê duyệt yêu cầu tăng lương của Nguyễn Văn Bình', 'INFO'),

    (12, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (12, 'Create', 'Tạo yêu cầu bổ sung phụ cấp', 'INFO'),
    (4, 'APPROVE', 'Phê duyệt yêu cầu bổ sung phụ cấp của Trần Thị Lan', 'INFO'),

    (14, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (14, 'Create', 'Tạo yêu cầu bổ sung phụ cấp', 'INFO'),
    (4, 'REJECT', 'Từ chối yêu cầu bổ sung phụ cấp của Phạm Thị Hồng', 'INFO'),

    (2, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (2, 'Update', 'Chỉnh sửa thông tin nhân viên Đỗ Thị Hà', 'INFO'),

    (4, 'Authentication', 'Đăng nhập hệ thống', 'INFO'),
    (4, 'Update', 'Chỉnh sửa thông tin hợp đồng của Lý Thanh Thảo', 'INFO'),

    (1, 'View', 'Sao lưu dữ liệu hệ thống', 'INFO'),
    (2, 'View', 'Khôi phục dữ liệu hệ thống', 'WARNING');