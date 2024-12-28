<%@ page import="model.UserProfile" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    UserProfile userProfile = (UserProfile) request.getAttribute("userProfile");
    String profilePhoto = "static/images/img.png"; // Default profile image
    String username = "Guest"; // Fallback username
    if (userProfile != null) {
        username = userProfile.getUsername();
        if (userProfile.getProfilePhoto() != null && !userProfile.getProfilePhoto().isEmpty()) {
            profilePhoto = userProfile.getProfilePhoto();
        }
    }
    String errorMessage = (String) request.getAttribute("errorMessage");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Profile</title>
    <link rel="icon" href="${pageContext.request.contextPath}/static/images/logo_2.png" type="image/x-icon">
    <link rel="stylesheet" href="styles.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            color: #333;
        }

        h2 {
            text-align: center;
            color: #4CAF50;
            margin-top: 30px;
        }

        .profile-container {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            margin-top: 30px;
            padding: 20px;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 80%;
            margin-left: auto;
            margin-right: auto;
        }

        .profile-photo {
            width: 180px; /* Increased size */
            height: 180px; /* Increased size */
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid #4CAF50;
            margin-bottom: 20px; /* Adjusted margin to move username below photo */
        }

        .profile-details {
            font-size: 24px; /* Increased size */
            color: #555;
            text-align: center; /* Centered username */
        }

        .profile-details p {
            margin: 5px 0;
        }

        .profile-details strong {
            color: #4CAF50;
        }

        .profile-container a {
            display: block;
            text-align: center;
            color: #4CAF50;
            margin-top: 20px;
            font-size: 1.1rem;
            text-decoration: none;
        }

        .profile-container a:hover {
            text-decoration: underline;
        }

        form {
            background-color: #fff;
            padding: 20px;
            margin-top: 30px;
            border-radius: 8px;
            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
            width: 80%;
            margin-left: auto;
            margin-right: auto;
        }

        label {
            font-size: 16px;
            margin-bottom: 10px;
            display: block;
        }

        input[type="file"] {
            padding: 10px;
            font-size: 14px;
            width: 100%;
            border-radius: 5px;
            border: 1px solid #ccc;
            margin-bottom: 20px;
        }

        button {
            background-color: #4CAF50;
            color: white;
            padding: 12px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            transition: background-color 0.3s ease;
            width: 100%;
        }

        button:hover {
            background-color: #45a049;
        }

        /* Additional styling */
        .logout-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            color: #4CAF50;
            font-size: 1.1rem;
        }

        .logout-link a {
            text-decoration: none;
            color: inherit;
        }

        .logout-link a:hover {
            text-decoration: underline;
        }

        /* Styling for the checkbox container */
        .checkbox-container {
            display: flex;
            align-items: center;
            margin-bottom: 20px; /* Space below the checkbox */
        }

        /* Label for checkbox */
        .checkbox-label {
            font-size: 16px;
            margin-right: 10px;  /* Space between label and checkbox */
            color: #555;
            cursor: pointer;
        }

        /* Custom checkbox appearance */
        .checkbox {
            width: 20px;
            height: 20px;
            border: 2px solid #4CAF50;
            border-radius: 5px;
            background-color: white;
            position: relative;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        /* When the checkbox is checked */
        .checkbox:checked {
            background-color: #4CAF50;
            border-color: #4CAF50;
        }

        /* Add a checkmark when checked */
        .checkbox:checked::after {
            content: "";
            position: absolute;
            top: 4px;
            left: 4px;
            width: 10px;
            height: 10px;
            background-color: white;
            border-radius: 2px;
        }

        /* Hover effect for checkbox */
        .checkbox:hover {
            border-color: #45a049;
        }
    </style>
</head>
<body>

<div class="profile-container">
    <img src="<%= profilePhoto %>" class="profile-photo" alt="Profile Photo">
    <div class="profile-details">
        <p><strong>Username:</strong> <%= username %></p>
    </div>
</div>

<% if (errorMessage != null) { %>
<div class="error-message">
    <p style="color: red;"><%= errorMessage %></p>
</div>
<% } %>

<!-- Back to Dashboard Button -->
<div style="text-align:center; margin-top: 20px;">
    <a href="chat" style="text-decoration: none;">
        <button style="padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer;">Back to Dashboard</button>
    </a>
</div>

<form action="uploadProfilePhoto" method="POST" enctype="multipart/form-data">
    <label for="profilePhoto">Choose a new profile photo:</label>
    <input type="file" name="profilePhoto" id="profilePhoto" accept="image/*" required>

    <!-- Malware scan checkbox -->
    <div class="checkbox-container">
        <label for="enableMalwareScan" class="checkbox-label">Enable Malware Scan</label>
        <input type="checkbox" id="enableMalwareScan" name="enableMalwareScan" class="checkbox">
    </div>

    <button type="submit">Change Profile</button>
</form>

</body>
</html>
