CREATE TABLE account (
    account_id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(100) UNIQUE DEFAULT NULL,
    password VARCHAR(255) DEFAULT NULL,
    is_active INT DEFAULT NULL,
    failed_attempt INT DEFAULT 0,
    locked_until DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id)
);

CREATE TABLE role (
    role_id INT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    role_name VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (role_id)
);

CREATE TABLE account_role (
    account_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (account_id, role_id)
);

CREATE TABLE expertise (
    expertise_id INT NOT NULL AUTO_INCREMENT,
    expertise_name VARCHAR(100) NOT NULL,
    icon_url VARCHAR(255) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (expertise_id)
);

CREATE TABLE staff (
    staff_id INT NOT NULL AUTO_INCREMENT,
    account_id INT UNIQUE DEFAULT NULL,
    expertise_id INT DEFAULT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    staff_type ENUM NOT NULL,
    experience VARCHAR(100) DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT NULL,
    is_featured TINYINT(1) DEFAULT 0,
    featured_priority INT DEFAULT 0,
    is_deleted INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    avg_rating DECIMAL(2,1) DEFAULT 0.0,
    total_reviews INT DEFAULT 0,
    specialty_treatment VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (staff_id)
);

CREATE TABLE patient (
    patient_id INT NOT NULL AUTO_INCREMENT,
    account_id INT UNIQUE DEFAULT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender VARCHAR(10) DEFAULT NULL,
    date_of_birth DATE DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    address VARCHAR(255) DEFAULT NULL,
    avatar_url VARCHAR(255) DEFAULT NULL,
    is_deleted INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    booking_locked TINYINT(1) DEFAULT 0,
    cancel_spam_count INT DEFAULT 0,
    PRIMARY KEY (patient_id)
);

CREATE TABLE patient_vital_profile (
    patient_id INT NOT NULL,
    height INT DEFAULT NULL,
    weight DECIMAL(5,2) DEFAULT NULL,
    blood_pressure VARCHAR(20) DEFAULT NULL,
    pulse INT DEFAULT NULL,
    blood_type VARCHAR(5) DEFAULT NULL,
    allergies TEXT DEFAULT NULL,
    chronic_diseases TEXT DEFAULT NULL,
    medical_history TEXT DEFAULT NULL,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (patient_id)
);

CREATE TABLE service (
    service_id INT NOT NULL AUTO_INCREMENT,
    service_name VARCHAR(100) NOT NULL,
    service_type ENUM NOT NULL,
    estimated_duration INT DEFAULT 15,
    original_price DECIMAL(10,2) NOT NULL,
    is_deleted INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    image_url VARCHAR(255) DEFAULT NULL,
    description TEXT DEFAULT NULL,
    is_featured TINYINT(1) DEFAULT 0,
    featured_priority INT DEFAULT 0,
    updated_at DATETIME(6) DEFAULT NULL,
    discount_amount DECIMAL(10,2) DEFAULT NULL,
    PRIMARY KEY (service_id)
);

CREATE TABLE doctor_service_price (
    id INT NOT NULL AUTO_INCREMENT,
    staff_id INT NOT NULL UNIQUE,
    original_price DECIMAL(38,2) NOT NULL,
    discount_amount DECIMAL(38,2) DEFAULT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE appointment (
    appointment_id INT NOT NULL AUTO_INCREMENT,
    patient_id INT NOT NULL,
    main_doctor_id INT DEFAULT NULL,
    service_id INT DEFAULT NULL,
    expertise_id INT DEFAULT NULL,
    suggested_expertise_id INT DEFAULT NULL,
    appointment_date DATE NOT NULL,
    time_start TIME DEFAULT NULL,
    time_end TIME DEFAULT NULL,
    appointment_type ENUM NOT NULL,
    status ENUM DEFAULT NULL,
    created_by ENUM DEFAULT NULL,
    booking_mode ENUM DEFAULT NULL,
    is_ai_suggested TINYINT(1) DEFAULT 0,
    checkin_time DATETIME DEFAULT NULL,
    queue_number INT DEFAULT NULL,
    cancelled_by ENUM DEFAULT NULL,
    cancel_reason TEXT DEFAULT NULL,
    is_deleted INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    note TEXT DEFAULT NULL,
    checkout_time DATETIME DEFAULT NULL,
    reschedule_count INT DEFAULT 0,
    reschedule_reason VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (appointment_id)
);

CREATE TABLE staff_schedule (
    schedule_id INT NOT NULL AUTO_INCREMENT,
    staff_id INT NOT NULL,
    working_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM DEFAULT NULL,
    note VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (schedule_id)
);

CREATE TABLE leave_request (
    leave_id INT NOT NULL AUTO_INCREMENT,
    staff_id INT NOT NULL,
    leave_type ENUM DEFAULT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    reason TEXT DEFAULT NULL,
    status ENUM DEFAULT NULL,
    approved_by INT DEFAULT NULL,
    rejection_reason TEXT DEFAULT NULL,
    reviewed_at DATETIME DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (leave_id)
);

CREATE TABLE medical_record (
    record_id INT NOT NULL AUTO_INCREMENT,
    patient_id INT NOT NULL,
    appointment_id INT DEFAULT NULL,
    main_doctor_id INT NOT NULL,
    diagnosis TEXT DEFAULT NULL,
    treatment TEXT DEFAULT NULL,
    note TEXT DEFAULT NULL,
    status ENUM NOT NULL,
    updated_by_doctor_id INT DEFAULT NULL,
    edit_reason TEXT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    vitals_taken BIT(1) DEFAULT NULL,
    consultation_discount DECIMAL(38,2) DEFAULT NULL,
    consultation_final_fee DECIMAL(38,2) DEFAULT NULL,
    consultation_original_fee DECIMAL(38,2) DEFAULT NULL,
    PRIMARY KEY (record_id)
);

CREATE TABLE medical_record_vital (
    record_id INT NOT NULL,
    weight DECIMAL(5,2) DEFAULT NULL,
    blood_pressure VARCHAR(20) DEFAULT NULL,
    pulse INT DEFAULT NULL,
    recorded_by INT DEFAULT NULL,
    status VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (record_id)
);

CREATE TABLE service_order (
    order_id INT NOT NULL AUTO_INCREMENT,
    record_id INT NOT NULL,
    service_id INT NOT NULL,
    custom_service_name VARCHAR(255) DEFAULT NULL,
    doctor_note TEXT DEFAULT NULL,
    ordered_by INT NOT NULL,
    status ENUM DEFAULT NULL,
    rejection_reason TEXT DEFAULT NULL,
    sample_collected_at DATETIME DEFAULT NULL,
    sample_collected_by INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    is_deleted TINYINT DEFAULT 0,
    service_discount DECIMAL(10,2) DEFAULT NULL,
    service_final_fee DECIMAL(10,2) NOT NULL,
    service_original_fee DECIMAL(10,2) DEFAULT NULL,
    PRIMARY KEY (order_id)
);

CREATE TABLE service_result (
    result_id INT NOT NULL AUTO_INCREMENT,
    order_id INT NOT NULL UNIQUE,
    result_data TEXT DEFAULT NULL,
    conclusion TEXT DEFAULT NULL,
    attachment_urls JSON DEFAULT NULL,
    entered_by INT NOT NULL,
    entered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (result_id)
);

CREATE TABLE medicine (
    medicine_id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    active_element VARCHAR(255) DEFAULT NULL,
    packing_standard VARCHAR(100) DEFAULT NULL,
    base_unit VARCHAR(50) DEFAULT NULL,
    usage_note VARCHAR(255) DEFAULT NULL,
    is_deleted INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (medicine_id)
);

CREATE TABLE prescription (
    prescription_id INT NOT NULL AUTO_INCREMENT,
    record_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING',
    is_deleted TINYINT DEFAULT 0,
    PRIMARY KEY (prescription_id)
);

CREATE TABLE prescription_item (
    prescription_item_id INT NOT NULL AUTO_INCREMENT,
    prescription_id INT NOT NULL,
    medicine_id INT DEFAULT NULL,
    medicine_name VARCHAR(255) DEFAULT NULL,
    unit VARCHAR(50) NOT NULL,
    quantity DECIMAL(8,2) NOT NULL,
    dosage VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (prescription_item_id)
);

CREATE TABLE invoice (
    invoice_id INT NOT NULL AUTO_INCREMENT,
    record_id INT NOT NULL,
    patient_id INT NOT NULL,
    total_price DECIMAL(38,2) NOT NULL,
    payment_method ENUM DEFAULT NULL,
    status ENUM DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (invoice_id)
);

CREATE TABLE invoice_item (
    item_id INT NOT NULL AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    item_type ENUM NOT NULL,
    reference_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    price_at_time DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (item_id)
);

CREATE TABLE device_token (
    token_id INT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    device_type VARCHAR(50) DEFAULT NULL,
    token VARCHAR(255) NOT NULL,
    account_id INT NOT NULL,
    PRIMARY KEY (token_id)
);

CREATE TABLE doctor_review (
    review_id INT NOT NULL AUTO_INCREMENT,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    rating INT NOT NULL,
    comment VARCHAR(255) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reply VARCHAR(255) DEFAULT NULL,
    replied_at DATETIME DEFAULT NULL,
    replied_by INT DEFAULT NULL,
    is_anonymous BIT(1) DEFAULT NULL,
    appointment_id INT DEFAULT NULL,
    ai_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ai_moderation_note VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (review_id),
    UNIQUE KEY unique_review (doctor_id, patient_id)
);

CREATE TABLE feedback (
    feedback_id INT NOT NULL AUTO_INCREMENT,
    record_id INT NOT NULL,
    rating INT DEFAULT NULL,
    comment VARCHAR(255) DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reply VARCHAR(255) DEFAULT NULL,
    replied_at DATETIME DEFAULT NULL,
    replied_by INT DEFAULT NULL,
    is_anonymous BIT(1) DEFAULT NULL,
    ai_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ai_moderation_note VARCHAR(500) DEFAULT NULL,
    PRIMARY KEY (feedback_id)
);

CREATE TABLE contact_message (
    message_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) DEFAULT NULL,
    subject VARCHAR(100) DEFAULT 'Khac',
    content TEXT NOT NULL,
    status ENUM DEFAULT NULL,
    replied_at DATETIME DEFAULT NULL,
    reply_content TEXT DEFAULT NULL,
    replied_by INT DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id)
);

CREATE TABLE chat_session (
    session_id INT NOT NULL AUTO_INCREMENT,
    patient_id INT DEFAULT NULL,
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    rating INT DEFAULT NULL,
    feedback_comment VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (session_id)
);

CREATE TABLE chat_message (
    message_id INT NOT NULL AUTO_INCREMENT,
    session_id INT NOT NULL,
    sender_type ENUM NOT NULL,
    message_content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (message_id)
);

CREATE TABLE notification (
    notification_id INT NOT NULL AUTO_INCREMENT,
    account_id INT DEFAULT NULL,
    type ENUM DEFAULT NULL,
    content VARCHAR(255) DEFAULT NULL,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notification_id)
);

CREATE TABLE follow_up (
    follow_up_id INT NOT NULL AUTO_INCREMENT,
    record_id INT NOT NULL,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    scheduled_datetime DATETIME NOT NULL,
    note VARCHAR(255) DEFAULT NULL,
    status ENUM DEFAULT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME(6) DEFAULT NULL,
    appointment_id INT DEFAULT NULL,
    confirmed_at DATETIME DEFAULT NULL,
    reminder_sent_at DATETIME DEFAULT NULL,
    cancel_reason VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (follow_up_id)
);

ALTER TABLE account_role ADD CONSTRAINT fk_ac_role_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE;
ALTER TABLE account_role ADD CONSTRAINT fk_ac_role_role FOREIGN KEY (role_id) REFERENCES role(role_id);

ALTER TABLE staff ADD CONSTRAINT fk_staff_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE CASCADE;
ALTER TABLE staff ADD CONSTRAINT fk_staff_expertise FOREIGN KEY (expertise_id) REFERENCES expertise(expertise_id);
ALTER TABLE patient ADD CONSTRAINT fk_patient_account FOREIGN KEY (account_id) REFERENCES account(account_id) ON DELETE SET NULL;
ALTER TABLE patient_vital_profile ADD CONSTRAINT fk_vital_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE;

ALTER TABLE doctor_service_price ADD CONSTRAINT fk_price_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id);

ALTER TABLE staff_schedule ADD CONSTRAINT fk_sched_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id);
ALTER TABLE leave_request ADD CONSTRAINT fk_leave_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id);
ALTER TABLE leave_request ADD CONSTRAINT fk_leave_approver FOREIGN KEY (approved_by) REFERENCES staff(staff_id);

ALTER TABLE appointment ADD CONSTRAINT fk_app_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE;
ALTER TABLE appointment ADD CONSTRAINT fk_app_doctor FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id);
ALTER TABLE appointment ADD CONSTRAINT fk_app_service FOREIGN KEY (service_id) REFERENCES service(service_id);
ALTER TABLE appointment ADD CONSTRAINT fk_app_exp FOREIGN KEY (expertise_id) REFERENCES expertise(expertise_id);
ALTER TABLE appointment ADD CONSTRAINT fk_app_sug_exp FOREIGN KEY (suggested_expertise_id) REFERENCES expertise(expertise_id);

ALTER TABLE medical_record ADD CONSTRAINT fk_rec_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id);
ALTER TABLE medical_record ADD CONSTRAINT fk_rec_app KEY (appointment_id) REFERENCES appointment(appointment_id);
ALTER TABLE medical_record ADD CONSTRAINT fk_rec_doctor FOREIGN KEY (main_doctor_id) REFERENCES staff(staff_id);
ALTER TABLE medical_record ADD CONSTRAINT fk_rec_editor FOREIGN KEY (updated_by_doctor_id) REFERENCES staff(staff_id);
ALTER TABLE medical_record_vital ADD CONSTRAINT fk_rec_vital_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id) ON DELETE CASCADE;
ALTER TABLE medical_record_vital ADD CONSTRAINT fk_rec_vital_staff FOREIGN KEY (recorded_by) REFERENCES staff(staff_id);

ALTER TABLE service_order ADD CONSTRAINT fk_ord_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id);
ALTER TABLE service_order ADD CONSTRAINT fk_ord_service FOREIGN KEY (service_id) REFERENCES service(service_id);
ALTER TABLE service_order ADD CONSTRAINT fk_ord_staff FOREIGN KEY (ordered_by) REFERENCES staff(staff_id);
ALTER TABLE service_order ADD CONSTRAINT fk_ord_collector FOREIGN KEY (sample_collected_by) REFERENCES staff(staff_id);
ALTER TABLE service_result ADD CONSTRAINT fk_res_order FOREIGN KEY (order_id) REFERENCES service_order(order_id);
ALTER TABLE service_result ADD CONSTRAINT fk_res_staff FOREIGN KEY (entered_by) REFERENCES staff(staff_id);

ALTER TABLE prescription ADD CONSTRAINT fk_pres_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id);
ALTER TABLE prescription_item ADD CONSTRAINT fk_item_pres FOREIGN KEY (prescription_id) REFERENCES prescription(prescription_id);
ALTER TABLE prescription_item ADD CONSTRAINT fk_item_med FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id);

ALTER TABLE invoice ADD CONSTRAINT fk_invoice_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id);
ALTER TABLE invoice ADD CONSTRAINT fk_invoice_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id);
ALTER TABLE invoice_item ADD CONSTRAINT fk_item_invoice FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id) ON DELETE CASCADE;

ALTER TABLE device_token ADD CONSTRAINT fk_device_account FOREIGN KEY (account_id) REFERENCES account(account_id);

ALTER TABLE doctor_review ADD CONSTRAINT fk_rev_doctor FOREIGN KEY (doctor_id) REFERENCES staff(staff_id) ON DELETE CASCADE;
ALTER TABLE doctor_review ADD CONSTRAINT fk_rev_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id) ON DELETE CASCADE;
ALTER TABLE doctor_review ADD CONSTRAINT fk_rev_replier FOREIGN KEY (replied_by) REFERENCES staff(staff_id);
ALTER TABLE doctor_review ADD CONSTRAINT fk_rev_app FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id);
ALTER TABLE feedback ADD CONSTRAINT fk_fb_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id);
ALTER TABLE feedback ADD CONSTRAINT fk_fb_replier FOREIGN KEY (replied_by) REFERENCES staff(staff_id);
ALTER TABLE contact_message ADD CONSTRAINT fk_msg_replier FOREIGN KEY (replied_by) REFERENCES staff(staff_id) ON DELETE SET NULL;

ALTER TABLE chat_session ADD CONSTRAINT fk_chat_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id);
ALTER TABLE chat_message ADD CONSTRAINT fk_chat_msg_session FOREIGN KEY (session_id) REFERENCES chat_session(session_id) ON DELETE CASCADE;
ALTER TABLE notification ADD CONSTRAINT fk_notif_account FOREIGN KEY (account_id) REFERENCES account(account_id);

ALTER TABLE follow_up ADD CONSTRAINT fk_fup_record FOREIGN KEY (record_id) REFERENCES medical_record(record_id);
ALTER TABLE follow_up ADD CONSTRAINT fk_fup_patient FOREIGN KEY (patient_id) REFERENCES patient(patient_id);
ALTER TABLE follow_up ADD CONSTRAINT fk_fup_doctor FOREIGN KEY (doctor_id) REFERENCES staff(staff_id);
ALTER TABLE follow_up ADD CONSTRAINT fk_fup_app FOREIGN KEY (appointment_id) REFERENCES appointment(appointment_id);