# Secure Web Application

This project is a secure web application developed using Java servlets, JSP, and MySQL. It enables users to upload, download, and manage files, send messages to centralized dashboard securely while supporting user authentication and authorization with session management and multi-factor authentication (MFA).

## Features

### 1. User Authentication
- **Login**: Users log in using their credentials (username and password), followed by an MFA code sent to their registered email.
- **Registration**: New users can register by providing their username, email, and password.
- **Session Management**: Ensures secure tracking of logged-in users with options for "remember me" functionality.
- **Access Control**: Limits access to specific resources based on authentication status and roles.

### 2. Dashboard Management
- **Chat Management**: Users can view messages on the dashboard and filter messages by keywords.
- **Send Messages**: Authorized users can send new messages.
- **Download Chat History**: Admins can download a complete chat history.

### 3. File Management
- **File Upload**: Users can upload files with size and type validation. Files over 10 MB are rejected.
- **File List**: A detailed table lists all uploaded files with information like name, uploader, and timestamp.
- **File Download**: Users can download uploaded files.
- **File Removal**: Admins and Moderators can delete files.

### 4. Profile Management
- **Profile Page**: Displays the user's username and profile image.
- **Profile Image Update**: Users can upload new profile images (max size: 5 MB; formats: PNG, JPG).
- **Malware Scan**: Optionally scans uploaded files for malware using ClamAV.

### 5. Security Measures
- **MFA**: Additional security layer requiring a code sent to the user’s email.
- **Input Validation**: Ensures only valid files are uploaded.
- **Authorization Filters**: Restricts access to protected pages.
- **Rate Limiting**: Prevents excessive requests, with a limit of 100 requests for all endpoints.
- **Email Alerts**: Sends notifications and warning email if malicious input (e.g., XSS, SQL Injection) is detected.

## Technologies Used

### Frontend
- **JSP**: For rendering dynamic content.
- **HTML/CSS**: For user interface design and styling.

### Backend
- **Java Servlets**: Handles requests, business logic, and database interactions.
- **Session Management**: Manages user authentication securely.

### Database
- **MySQL**: Stores user credentials, file records, and chat data.

### Security Tools
- **ClamAV**: For optional malware scanning of uploaded files.

## Database Design

### Tables
1. **User Table**:
   - `id`, `username`, `password`, `profile_photo`, `role`, `email`
2. **FileUploads Table**:
   - `id`, `file_name`, `uploader`, `timestamp`, `file_path`
3. **ChatMessages Table**:
   - `id`, `username`, `message`, `timestamp`

## Setup Instructions

### Prerequisites
- Install Java, Tomcat, MySQL, and ClamWin (if using malware scanning).
- Install IntelliJ IDEA.

### Steps
1. Clone the repository.
2. Set up the MySQL database:
   - Create a new database using the provided script located at `src/main/java/db/db_setup.sql`.
   - Add the database connection in IntelliJ IDEA.
3. Configure environment variables in IntelliJ IDEA:
   - Set up environment variables for database username, password, and ClamWin path (if using malware scan).
4. Deploy the application:
   - Import the project into IntelliJ IDEA.
   - Configure Tomcat server in the IDE.
   - Build and deploy the application using the Tomcat server.
5. Access the application:
   - Open your browser and navigate to `http://localhost:8080/` to access the application.

### Malware Scanning Setup
If you plan to use the optional malware scanning feature:
1. Download and install [ClamWin](http://downloads.sourceforge.net/clamwin/clamwin-0.103.2.1-setup.exe).
2. Ensure the following files are located in `C:\Program Files (x86)\ClamWin\bin\database`:
   - `main.cvd`
   - `daily.cvd`
   - `bytecode.cvd`
3. Verify the ClamAV executable is accessible from the environment variables.

## Contributing
We welcome contributions to this project. To contribute:
1. Fork the repository.
2. Create a feature branch.
3. Commit your changes.
4. Push to your fork.
5. Open a pull request for review.

## License
This project is licensed under the [MIT License].

## Contact
For questions or support, feel free to contact [ziyamelikov04@gmail.com](mailto:ziyamelikov04@gmail.com).

---
Developed by: **Ziya Malikov**

