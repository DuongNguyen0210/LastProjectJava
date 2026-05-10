-- Database Setup Script for LastProjectJava
-- Run this script in SQL Server Management Studio or sqlcmd
-- Make sure you have appropriate permissions to create databases

-- Create the database
CREATE DATABASE LastProjectJava;
GO

-- Use the database
USE LastProjectJava;
GO

-- Create target_account table
CREATE TABLE target_account (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(255) NOT NULL,
    platform NVARCHAR(50) NOT NULL,
    UNIQUE(username, platform)
);
GO

-- Create submission table
CREATE TABLE submission (
    id INT IDENTITY(1,1) PRIMARY KEY,
    submit_id NVARCHAR(50) NOT NULL UNIQUE,
    account_id INT NOT NULL,
    language NVARCHAR(100) NOT NULL,
    source_code NVARCHAR(MAX) NOT NULL,
    submitted_at DATETIME2 NOT NULL,
    FOREIGN KEY (account_id) REFERENCES target_account(id)
);
GO

-- Create indexes for better performance
CREATE INDEX idx_submission_account_id ON submission(account_id);
CREATE INDEX idx_submission_submitted_at ON submission(submitted_at);
CREATE INDEX idx_target_account_platform ON target_account(platform);
GO

-- Optional: Create a view for easier querying
CREATE VIEW v_submissions_with_accounts AS
SELECT
    s.id,
    s.submit_id,
    ta.username,
    ta.platform,
    s.language,
    s.source_code,
    s.submitted_at
FROM submission s
JOIN target_account ta ON s.account_id = ta.id;
GO

PRINT 'Database setup completed successfully!';
PRINT 'Database: LastProjectJava';
PRINT 'Tables created: target_account, submission';
PRINT 'Indexes created for optimal performance';
PRINT 'View created: v_submissions_with_accounts';
