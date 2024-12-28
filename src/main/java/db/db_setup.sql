-- you need to run this commands after connecting your database

CREATE TABLE users (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) NOT NULL UNIQUE,
       password VARCHAR(255) NOT NULL,
       profile_photo VARCHAR(255) DEFAULT NULL,
       role ENUM('Admin', 'Moderator', 'User') NOT NULL DEFAULT 'User',
       email VARCHAR(255) default NULL
);

CREATE TABLE chat_messages (
       id INT AUTO_INCREMENT PRIMARY KEY,
       username VARCHAR(255) NOT NULL,
       message TEXT NOT NULL,
       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE file_uploads (
       id INT AUTO_INCREMENT PRIMARY KEY,
       file_name VARCHAR(255) NOT NULL,
       uploader VARCHAR(255) NOT NULL,
       timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       file_path VARCHAR(255) NOT NULL
);

