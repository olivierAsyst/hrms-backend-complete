-- =====================================================
-- HRMS Backend - Attendance and Payroll Schema
-- PostgreSQL Migration Script
-- =====================================================

-- =====================================================
-- ATTENDANCE MANAGEMENT
-- =====================================================

-- Table: attendances
CREATE TABLE IF NOT EXISTS attendances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    working_hours DECIMAL(5,2),
    overtime_hours DECIMAL(5,2) DEFAULT 0,
    late_minutes INTEGER DEFAULT 0,
    early_departure_minutes INTEGER DEFAULT 0,
    absence_id BIGINT,
    notes TEXT,
    location VARCHAR(200),
    ip_address VARCHAR(45),
    device_info VARCHAR(200),
    metadata JSONB,
    verified BOOLEAN NOT NULL DEFAULT false,
    verified_by BIGINT,
    verified_at TIMESTAMP,
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_attendance_verified_by FOREIGN KEY (verified_by) REFERENCES users(id),
    CONSTRAINT chk_attendance_times CHECK (check_out_time IS NULL OR check_out_time >= check_in_time)
);

-- Indexes for attendances
CREATE INDEX idx_attendance_employee ON attendances(employee_id) WHERE deleted = false;
CREATE INDEX idx_attendance_date ON attendances(attendance_date) WHERE deleted = false;
CREATE INDEX idx_attendance_status ON attendances(status) WHERE deleted = false;
CREATE INDEX idx_attendance_employee_date ON attendances(employee_id, attendance_date) WHERE deleted = false;
CREATE INDEX idx_attendance_verified ON attendances(verified) WHERE deleted = false;

COMMENT ON TABLE attendances IS 'Daily attendance records with check-in/out times and overtime tracking';

-- =====================================================
-- LEAVE/ABSENCE MANAGEMENT
-- =====================================================

-- Table: absences
CREATE TABLE IF NOT EXISTS absences (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INTEGER NOT NULL,
    is_half_day BOOLEAN NOT NULL DEFAULT false,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT,
    supporting_document_url VARCHAR(500),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    rejection_reason TEXT,
    is_paid BOOLEAN NOT NULL DEFAULT true,
    deducted_from_balance BOOLEAN NOT NULL DEFAULT true,
    balance_before INTEGER,
    balance_after INTEGER,
    is_emergency BOOLEAN NOT NULL DEFAULT false,
    emergency_contact VARCHAR(200),
    substitute_employee_id BIGINT,
    substitute_notes TEXT,
    notes TEXT,
    metadata JSONB,
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_absence_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_absence_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_absence_substitute FOREIGN KEY (substitute_employee_id) REFERENCES employees(id),
    CONSTRAINT chk_absence_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_absence_total_days CHECK (total_days > 0)
);

-- Indexes for absences
CREATE INDEX idx_absence_employee ON absences(employee_id) WHERE deleted = false;
CREATE INDEX idx_absence_type ON absences(type) WHERE deleted = false;
CREATE INDEX idx_absence_status ON absences(status) WHERE deleted = false;
CREATE INDEX idx_absence_dates ON absences(start_date, end_date) WHERE deleted = false;
CREATE INDEX idx_absence_employee_status ON absences(employee_id, status) WHERE deleted = false;

COMMENT ON TABLE absences IS 'Employee leave requests and absence records';

-- Table: leave_balances
CREATE TABLE IF NOT EXISTS leave_balances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    leave_type VARCHAR(30) NOT NULL,
    total_allocated INTEGER NOT NULL DEFAULT 0,
    used INTEGER NOT NULL DEFAULT 0,
    pending INTEGER NOT NULL DEFAULT 0,
    available INTEGER NOT NULL DEFAULT 0,
    carried_forward INTEGER DEFAULT 0,
    carry_forward_limit INTEGER,
    expires_on DATE,
    notes TEXT,
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT uk_employee_year_type UNIQUE (employee_id, year, leave_type),
    CONSTRAINT chk_leave_balance_positive CHECK (total_allocated >= 0 AND used >= 0 AND pending >= 0)
);

-- Indexes for leave_balances
CREATE INDEX idx_leave_balance_employee ON leave_balances(employee_id) WHERE deleted = false;
CREATE INDEX idx_leave_balance_year ON leave_balances(year) WHERE deleted = false;
CREATE INDEX idx_leave_balance_type ON leave_balances(leave_type) WHERE deleted = false;
CREATE INDEX idx_leave_balance_employee_year ON leave_balances(employee_id, year) WHERE deleted = false;

COMMENT ON TABLE leave_balances IS 'Annual leave balance tracking per employee and leave type';

-- =====================================================
-- PAYROLL MANAGEMENT
-- =====================================================

-- Table: payrolls
CREATE TABLE IF NOT EXISTS payrolls (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    pay_period VARCHAR(7) NOT NULL, -- YYYY-MM format
    payment_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    -- Earnings
    basic_salary DECIMAL(15,2) NOT NULL,
    housing_allowance DECIMAL(15,2) DEFAULT 0,
    transport_allowance DECIMAL(15,2) DEFAULT 0,
    meal_allowance DECIMAL(15,2) DEFAULT 0,
    bonus DECIMAL(15,2) DEFAULT 0,
    commission DECIMAL(15,2) DEFAULT 0,
    overtime_pay DECIMAL(15,2) DEFAULT 0,
    other_earnings DECIMAL(15,2) DEFAULT 0,
    -- Deductions
    tax_deduction DECIMAL(15,2) DEFAULT 0,
    social_security DECIMAL(15,2) DEFAULT 0,
    health_insurance DECIMAL(15,2) DEFAULT 0,
    pension_contribution DECIMAL(15,2) DEFAULT 0,
    loan_repayment DECIMAL(15,2) DEFAULT 0,
    salary_advance_deduction DECIMAL(15,2) DEFAULT 0,
    absence_deduction DECIMAL(15,2) DEFAULT 0,
    late_deduction DECIMAL(15,2) DEFAULT 0,
    other_deductions DECIMAL(15,2) DEFAULT 0,
    -- Calculated totals
    gross_salary DECIMAL(15,2) NOT NULL,
    total_deductions DECIMAL(15,2) NOT NULL,
    net_salary DECIMAL(15,2) NOT NULL,
    -- Attendance details
    working_days INTEGER NOT NULL,
    present_days INTEGER NOT NULL,
    absent_days INTEGER NOT NULL DEFAULT 0,
    paid_leave_days INTEGER NOT NULL DEFAULT 0,
    unpaid_leave_days INTEGER NOT NULL DEFAULT 0,
    overtime_hours DECIMAL(5,2) DEFAULT 0,
    late_hours DECIMAL(5,2) DEFAULT 0,
    -- Additional info
    notes TEXT,
    payment_method VARCHAR(50),
    bank_account_number VARCHAR(50),
    transaction_reference VARCHAR(100),
    -- Approval
    approved_by BIGINT,
    approved_at TIMESTAMP,
    paid_by BIGINT,
    paid_at TIMESTAMP,
    -- JSONB fields
    earnings_breakdown JSONB,
    deductions_breakdown JSONB,
    payslip_url VARCHAR(500),
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_payroll_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_payroll_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_payroll_paid_by FOREIGN KEY (paid_by) REFERENCES users(id),
    CONSTRAINT uk_employee_period UNIQUE (employee_id, pay_period),
    CONSTRAINT chk_payroll_amounts CHECK (basic_salary >= 0 AND gross_salary >= 0 AND net_salary >= 0)
);

-- Indexes for payrolls
CREATE INDEX idx_payroll_employee ON payrolls(employee_id) WHERE deleted = false;
CREATE INDEX idx_payroll_period ON payrolls(pay_period) WHERE deleted = false;
CREATE INDEX idx_payroll_status ON payrolls(status) WHERE deleted = false;
CREATE INDEX idx_payroll_employee_period ON payrolls(employee_id, pay_period) WHERE deleted = false;
CREATE INDEX idx_payroll_payment_date ON payrolls(payment_date) WHERE deleted = false;

COMMENT ON TABLE payrolls IS 'Monthly payroll records with detailed earnings and deductions';

-- =====================================================
-- SALARY ADVANCES
-- =====================================================

-- Table: salary_advances
CREATE TABLE IF NOT EXISTS salary_advances (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    request_date DATE NOT NULL,
    amount_requested DECIMAL(15,2) NOT NULL,
    amount_approved DECIMAL(15,2),
    amount_disbursed DECIMAL(15,2),
    amount_repaid DECIMAL(15,2) DEFAULT 0,
    remaining_balance DECIMAL(15,2),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reason TEXT NOT NULL,
    repayment_months INTEGER NOT NULL,
    monthly_repayment DECIMAL(15,2),
    repayment_start_date DATE,
    repayment_end_date DATE,
    fully_repaid BOOLEAN NOT NULL DEFAULT false,
    repayment_completed_date DATE,
    -- Approval
    approved_by BIGINT,
    approved_at TIMESTAMP,
    approval_notes TEXT,
    -- Rejection
    rejection_reason TEXT,
    rejected_by BIGINT,
    rejected_at TIMESTAMP,
    -- Disbursement
    disbursement_date DATE,
    disbursement_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    disbursed_by BIGINT,
    disbursed_at TIMESTAMP,
    notes TEXT,
    -- JSONB fields
    repayment_schedule JSONB,
    metadata JSONB,
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_salary_advance_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_salary_advance_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_salary_advance_rejected_by FOREIGN KEY (rejected_by) REFERENCES users(id),
    CONSTRAINT fk_salary_advance_disbursed_by FOREIGN KEY (disbursed_by) REFERENCES users(id),
    CONSTRAINT chk_salary_advance_amounts CHECK (amount_requested > 0 AND repayment_months > 0)
);

-- Indexes for salary_advances
CREATE INDEX idx_salary_advance_employee ON salary_advances(employee_id) WHERE deleted = false;
CREATE INDEX idx_salary_advance_status ON salary_advances(status) WHERE deleted = false;
CREATE INDEX idx_salary_advance_request_date ON salary_advances(request_date) WHERE deleted = false;
CREATE INDEX idx_salary_advance_repayment ON salary_advances(fully_repaid) WHERE deleted = false;

COMMENT ON TABLE salary_advances IS 'Employee salary advance requests and repayment tracking';

-- =====================================================
-- SALARY TRANSACTIONS
-- =====================================================

-- Table: salary_transactions
CREATE TABLE IF NOT EXISTS salary_transactions (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    transaction_type VARCHAR(30) NOT NULL,
    transaction_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    is_credit BOOLEAN NOT NULL,
    description TEXT,
    reference_number VARCHAR(100),
    payroll_id BIGINT,
    salary_advance_id BIGINT,
    payment_method VARCHAR(50),
    bank_account_number VARCHAR(50),
    transaction_reference VARCHAR(100),
    processed_by BIGINT,
    processed_at TIMESTAMP,
    notes TEXT,
    metadata JSONB,
    receipt_url VARCHAR(500),
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    version BIGINT DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT false,
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(100),
    -- Constraints
    CONSTRAINT fk_salary_transaction_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
    CONSTRAINT fk_salary_transaction_payroll FOREIGN KEY (payroll_id) REFERENCES payrolls(id),
    CONSTRAINT fk_salary_transaction_advance FOREIGN KEY (salary_advance_id) REFERENCES salary_advances(id),
    CONSTRAINT fk_salary_transaction_processed_by FOREIGN KEY (processed_by) REFERENCES users(id),
    CONSTRAINT chk_salary_transaction_amount CHECK (amount > 0)
);

-- Indexes for salary_transactions
CREATE INDEX idx_salary_transaction_employee ON salary_transactions(employee_id) WHERE deleted = false;
CREATE INDEX idx_salary_transaction_type ON salary_transactions(transaction_type) WHERE deleted = false;
CREATE INDEX idx_salary_transaction_date ON salary_transactions(transaction_date) WHERE deleted = false;
CREATE INDEX idx_salary_transaction_payroll ON salary_transactions(payroll_id) WHERE deleted = false;

COMMENT ON TABLE salary_transactions IS 'Complete transaction history for salary payments and deductions';

-- =====================================================
-- ADD FOREIGN KEY FOR ATTENDANCE -> ABSENCE
-- =====================================================

ALTER TABLE attendances 
ADD CONSTRAINT fk_attendance_absence 
FOREIGN KEY (absence_id) REFERENCES absences(id);

-- =====================================================
-- FUNCTIONS AND TRIGGERS
-- =====================================================

-- Trigger to update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to all new tables
CREATE TRIGGER update_attendances_updated_at BEFORE UPDATE ON attendances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_absences_updated_at BEFORE UPDATE ON absences
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_leave_balances_updated_at BEFORE UPDATE ON leave_balances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payrolls_updated_at BEFORE UPDATE ON payrolls
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_salary_advances_updated_at BEFORE UPDATE ON salary_advances
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_salary_transactions_updated_at BEFORE UPDATE ON salary_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Leave balance initialization for existing employees
-- INSERT INTO leave_balances (employee_id, year, leave_type, total_allocated, available)
-- SELECT id, EXTRACT(YEAR FROM CURRENT_DATE), 'ANNUAL_LEAVE', 21, 21
-- FROM employees WHERE deleted = false;

COMMENT ON DATABASE hrms_db IS 'Complete HRMS with Attendance, Leave and Payroll Management';
