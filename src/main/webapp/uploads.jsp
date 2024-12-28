<%@ page import="model.FileRecord" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Uploads</title>
  <link rel="icon" href="${pageContext.request.contextPath}/static/images/logo_3.png" type="image/x-icon">
  <link rel="stylesheet" href="styles.css">
  <style>
    body {
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      background-color: #f9f9f9;
      margin: 0;
      padding: 0;
    }

    .container {
      padding: 20px;
      max-width: 1200px;
      margin: auto;
    }

    h2 {
      text-align: center;
      margin-bottom: 20px;
      color: #333;
    }

    /* Upload Form Styling */
    .upload-form {
      background-color: #fff;
      padding: 20px;
      border-radius: 8px;
      border: 1px solid #ddd;
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
      margin-bottom: 40px;
    }

    .upload-form label {
      display: block;
      margin-bottom: 20px; /* Adjust the space as needed */
    }


    .upload-form input[type="file"] {
      width: 100%;
      padding: 12px;
      border: 1px solid #ccc;
      border-radius: 5px;
      margin-bottom: 10px;
    }

    .upload-form button {
      width: 100%;
      padding: 12px;
      background-color: #4CAF50;
      color: white;
      border: none;
      border-radius: 5px;
      cursor: pointer;
      font-size: 1rem;
      transition: background-color 0.3s ease;
    }

    .upload-form button:hover {
      background-color: #45a049;
    }

    /* File List Table */
    .file-list {
      width: 100%;
      border-collapse: collapse;
      margin-top: 20px;
    }

    .file-list th, .file-list td {
      padding: 10px;
      text-align: left;
      border-bottom: 1px solid #ddd;
    }

    .file-list th {
      background-color: #4CAF50;
      color: white;
    }

    .file-list td {
      background-color: #fff;
    }

    .file-list td a {
      color: #4CAF50;
      text-decoration: none;
    }

    .file-list td a:hover {
      text-decoration: underline;
    }

    /* Center align error/success messages */
    .message {
      text-align: center;
      padding: 10px;
      margin-bottom: 20px;
      font-weight: bold;
    }

    .error {
      color: red;
      background-color: #ffe6e6;
      border: 1px solid #ff9999;
    }

    .success {
      color: green;
      background-color: #e6ffe6;
      border: 1px solid #99ff99;
    }

  </style>
</head>
<body>

<div class="container">
  <h2>Upload Your Files</h2>

  <!-- Display Success or Error Message -->
  <%
    String errorMessage = (String) session.getAttribute("errorMessage");
    String successMessage = (String) session.getAttribute("successMessage");
    session.removeAttribute("errorMessage");
    session.removeAttribute("successMessage");
    if (errorMessage != null) {
  %>
  <div class="message error"><%= errorMessage %></div>
  <% } %>
  <% if (successMessage != null) { %>
  <div class="message success"><%= successMessage %></div>
  <% } %>

  <!-- File Upload Form -->
  <div class="upload-form">
    <form action="/uploads" method="POST" enctype="multipart/form-data">
      <input type="hidden" name="action" value="upload">
      <label for="file">Choose a file to upload:</label>
      <input type="file" name="file" id="file" required>
      <button type="submit">Upload</button>
    </form>
  </div>

  <!-- File List Table -->
  <h3>Uploaded Files</h3>
  <table class="file-list">
    <thead>
    <tr>
      <th>File Name</th>
      <th>Uploader</th>
      <th>Timestamp</th>
      <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <%
      List<FileRecord> files = (List<FileRecord>) request.getAttribute("files");
      if (files != null && !files.isEmpty()) {
        for (FileRecord file : files) {
    %>
    <tr>
      <td><%= file.getFileName() %></td>
      <td><%= file.getUploader() %></td>
      <td><%= new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.getTimestamp()) %></td>
      <td>
        <!-- Remove button -->
        <form action="/uploads" method="POST" style="display:inline;">
          <input type="hidden" name="action" value="remove">
          <input type="hidden" name="fileName" value="<%= file.getFileName() %>">
          <button type="submit" style="background-color: red; color: white; border: none; padding: 5px 10px; cursor: pointer;">Remove</button>
        </form>
        <!-- Download link -->
        <a href="/uploads/download?file=<%= file.getFileName() %>">Download</a>
      </td>
    </tr>
    <% } %>
    <% } else { %>
    <tr>
      <td colspan="4" style="text-align:center;">No files uploaded yet.</td>
    </tr>
    <% } %>
    </tbody>
  </table>

  <!-- Back to Dashboard Button -->
  <div style="text-align:center; margin-top: 20px;">
    <a href="chat" style="text-decoration: none;">
      <button style="padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer;">Back to Dashboard</button>
    </a>
  </div>

</div>

</body>
</html>
