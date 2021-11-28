<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.*" %>
<%request.setCharacterEncoding("utf-8");%>

<%
	String id = request.getParameter("deviceID");
	int sensorID = id.charAt(0) - '0';
	int groupID = Integer.parseInt(String.format("%d%d", id.charAt(1)-'0', id.charAt(2)-'0'));
	int deviceID = Integer.parseInt(String.format("%d%d", id.charAt(3)-'0', id.charAt(4)-'0'));
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<script type="text/javascript">
		window.self.close();
	</script>
</head>
<body>
</body>
</html>