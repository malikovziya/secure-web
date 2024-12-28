<%@ page import="java.util.*, java.sql.Timestamp" %>
<%@ page import="model.ChatMessage, java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect("index.html");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard</title>
    <link rel="icon" href="${pageContext.request.contextPath}/static/images/logo_main.jpg" type="image/x-icon">
    <link rel="stylesheet" href="styles.css">
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background-color: #f1f1f1;
            margin: 0;
            display: flex; /* Ensure proper layout alignment */
            min-height: 100vh; /* Full height view */
        }

        /* Sidebar Styling */
        .sidebar {
            width: 250px;
            background-color: #4CAF50;
            color: white;
            display: flex;
            flex-direction: column;
            padding: 20px;
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
        }

        .sidebar h2 {
            color: white;
            text-align: center;
            margin-bottom: 20px;
        }

        .sidebar a {
            color: white;
            text-decoration: none;
            padding: 10px 20px;
            margin: 5px 0;
            font-size: 1rem;
            border-radius: 5px;
            transition: background-color 0.3s ease;
            display: block;
            text-align: center;
        }

        .sidebar a:hover {
            background-color: #3e8e41;
        }

        /* Main Content Area Styling */
        .container {
            margin-left: 250px;
            padding: 20px;
            width: calc(100% - 250px);
        }

        .chat-container {
            background-color: #fff;
            border-radius: 8px;
            border: 1px solid #ddd;
            padding: 20px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            max-height: 400px;
            overflow-y: auto;
            margin-bottom: 20px;
            position: relative; /* Keep chat container relative for layout */
        }

        .chat-message {
            background-color: #f9f9f9;
            border-radius: 5px;
            margin-bottom: 15px;
            padding: 10px;
            text-align: left;
            word-wrap: break-word;
            white-space: normal;
            width: auto;
            max-width: 100%;
        }

        .chat-message .username {
            font-weight: bold;
            color: #5b5b5b;
        }

        .chat-message .timestamp {
            font-size: 0.8em;
            color: #888;
        }

        .chat-input, .search-input {
            width: 100%;
            padding: 12px;
            margin-top: 10px;
            border-radius: 5px;
            border: 1px solid #ccc;
            font-size: 1rem;
            box-sizing: border-box;
        }

        .chat-input {
            height: 80px;
        }

        .chat-form, .search-form {
            margin-top: 20px;
        }

        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            transition: background-color 0.3s ease;
            margin: 10px 0;
        }

        button:hover {
            background-color: #45a049;
        }

        .error {
            color: red;
            font-weight: bold;
            text-align: center;
            margin-bottom: 20px; /* Add margin below the error message */
            padding: 15px;
            background-color: #ffe6e6;
            border: 1px solid #ff9999;
            border-radius: 5px;
            width: 100%; /* Make error message width responsive */
            margin-left: auto;
            margin-right: auto;
        }

    </style>
</head>
<body>

<!-- Sidebar -->
<div class="sidebar">
    <h2>Menu</h2>
    <a href="profile">My Profile</a>
    <a href="uploads">Uploads</a>
    <a href="auth?logout=true">Logout</a>
</div>

<!-- Main Content -->
<div class="container">
    <h2>Welcome to the Dashboard</h2>

    <!-- Search Form -->
    <form action="chat" method="GET" class="search-form">
        <input type="text" name="keyword" class="search-input" placeholder="Search messages..." />
        <button type="submit">Search</button>
    </form>

    <!-- Display chat messages -->
    <div class="chat-container">
        <%
            String errorMessage = (String) session.getAttribute("error");
            if (errorMessage != null) {
        %>
        <!-- Display error message if it exists -->
        <div class="error"><%= errorMessage %></div>
        <%
                session.removeAttribute("error");
            }
        %>

        <%
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  // Define desired format
            List<ChatMessage> messages = (List<ChatMessage>) request.getAttribute("messages");
            if (messages != null && !messages.isEmpty()) {
                for (ChatMessage msg : messages) {
                    String formattedTimestamp = sdf.format(msg.getTimestamp());  // Format timestamp
        %>
        <div class="chat-message">
            <p class="username"><%= msg.getUsername() %></p>
            <p><%= msg.getMessage() %></p>
            <p class="timestamp"><%= formattedTimestamp %></p> <!-- Display formatted timestamp -->
        </div>
        <%
            }
        } else {
        %>
        <p>No messages available. Start the conversation!</p>
        <%
            }
        %>
    </div>

    <!-- Form to send a new chat message -->
    <form action="chat" method="POST" class="chat-form">
        <textarea name="message" class="chat-input" placeholder="Type your message..."></textarea>
        <button type="submit">Send Message</button>
    </form>

    <!-- Download Messages Button -->
    <form action="downloadMessages" method="GET">
        <button type="submit">Download All Messages</button>
    </form>

</div>

</body>
</html>
