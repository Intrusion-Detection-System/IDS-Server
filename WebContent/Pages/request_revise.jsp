<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
<%request.setCharacterEncoding("utf-8");%>
<%
	int deviceID = Integer.parseInt(request.getParameter("deviceID"));
	String position = request.getParameter("position");
	
	DatabaseConnection dbConnection = DatabaseConnection.getInstance();
	dbConnection.updateDevicePosition(deviceID, position);
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<script type="text/javascript">
		window.self.close();
		opener.parent.location.reload();
	</script>
</head>
<body>
</body>
</html>