-- Database Setup Script for LastProjectJava
-- Run this script in SQL Server Management Studio or sqlcmd
-- Make sure you have appropriate permissions to create databases

-- Create the database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'LastProjectJava')
BEGIN
    CREATE DATABASE LastProjectJava;
END
GO

-- Use the database
USE LastProjectJava;
GO

-- Create target_account table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'target_account')
BEGIN
    CREATE TABLE target_account (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(255) NOT NULL,
        platform NVARCHAR(50) NOT NULL,
        UNIQUE(username, platform)
    );
END
GO

-- Create submission table (Đã tích hợp đầy đủ các cột cho AI)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'submission')
BEGIN
    CREATE TABLE submission (
        id INT IDENTITY(1,1) PRIMARY KEY,
        submit_id NVARCHAR(50) NOT NULL UNIQUE,
        account_id INT NOT NULL,
        language NVARCHAR(100) NOT NULL,
        source_code NVARCHAR(MAX) NOT NULL,
        submitted_at DATETIME2 NOT NULL,
        
        -- Các cột phục vụ phân tích AI (Yêu cầu 1 & 2)
        data_structure NVARCHAR(MAX),
        algorithm NVARCHAR(MAX),
        ai_generated_probability FLOAT DEFAULT 0,
        ai_evaluation_note NVARCHAR(MAX),
        
        FOREIGN KEY (account_id) REFERENCES target_account(id)
    );
END
GO

-- Create indexes for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_submission_account_id')
    CREATE INDEX idx_submission_account_id ON submission(account_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_submission_submitted_at')
    CREATE INDEX idx_submission_submitted_at ON submission(submitted_at);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_target_account_platform')
    CREATE INDEX idx_target_account_platform ON target_account(platform);
GO

-- Cập nhật VIEW để hiển thị đầy đủ thông tin phân tích
IF EXISTS (SELECT * FROM sys.views WHERE name = 'v_submissions_with_accounts')
    DROP VIEW v_submissions_with_accounts;
GO

CREATE VIEW v_submissions_with_accounts AS
SELECT
    s.id,
    s.submit_id,
    ta.username,
    ta.platform,
    s.language,
    s.source_code,
    s.submitted_at,
    s.data_structure,
    s.algorithm,
    s.ai_generated_probability,
    s.ai_evaluation_note
FROM submission s
JOIN target_account ta ON s.account_id = ta.id;
GO

-- Giữ lại các Note thông báo cũ của nhóm
PRINT 'Database setup completed successfully!';
PRINT 'Database: LastProjectJava';
PRINT 'Tables created: target_account, submission';
PRINT 'Indexes created for optimal performance';
PRINT 'View created: v_submissions_with_accounts';
PRINT 'AI Analysis columns integrated: data_structure, algorithm, ai_generated_probability, ai_evaluation_note';