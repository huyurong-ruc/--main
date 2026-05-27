-- Align student_portrait with edu.ruc.platform.student.domain.StudentPortrait

ALTER TABLE student_portrait
    ADD COLUMN IF NOT EXISTS gender VARCHAR(16),
    ADD COLUMN IF NOT EXISTS ethnicity VARCHAR(32),
    ADD COLUMN IF NOT EXISTS scholarships VARCHAR(255),
    ADD COLUMN IF NOT EXISTS volunteer_service VARCHAR(255),
    ADD COLUMN IF NOT EXISTS leadership_roles VARCHAR(500),
    ADD COLUMN IF NOT EXISTS gpa DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS credits_earned INTEGER,
    ADD COLUMN IF NOT EXISTS career_orientation VARCHAR(128),
    ADD COLUMN IF NOT EXISTS remarks VARCHAR(255);
