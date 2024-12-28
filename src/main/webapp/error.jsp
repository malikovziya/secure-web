<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            text-align: center;
            margin-top: 50px;
        }
        .error-container {
            padding: 20px;
            border: 1px solid #f00;
            background-color: #ffeeee;
            display: inline-block;
        }
        .error-container h1 {
            color: #f00;
        }
    </style>
</head>
<body>
<div class="error-container">
    <h1>Error</h1>
    <p><%= request.getAttribute("errorMessage") %></p>
    <p><a href="index.html">Go to Login Page</a></p>
</div>
</body>
</html>
