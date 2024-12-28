<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile Photo Upload Status</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            text-align: center;
            margin-top: 50px;
        }
        .message {
            font-size: 20px;
            padding: 20px;
            margin: 20px;
            border-radius: 5px;
        }
        .success {
            background-color: #4CAF50;
            color: white;
        }
        .error {
            background-color: #f44336;
            color: white;
        }
        .info {
            background-color: #2196F3;
            color: white;
        }
        .back-link {
            margin-top: 20px;
            font-size: 1.1rem;
            color: #4CAF50;
            text-decoration: none;
        }
        .back-link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

<%
    // Retrieve the status message from the request
    String statusMessage = (String) request.getAttribute("statusMessage");
    String messageType = (String) request.getAttribute("messageType");
%>

<% if (statusMessage != null) { %>
<div class="message <%= messageType %>">
    <%= statusMessage %>
</div>
<% } %>

<p class="back-link"><a href="profile">Go back to your profile</a></p>

</body>
</html>
