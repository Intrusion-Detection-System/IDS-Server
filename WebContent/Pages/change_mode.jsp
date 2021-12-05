<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.*" %>
<% request.setCharacterEncoding("utf-8");%>
<%
	int id = Integer.parseInt(request.getParameter("deviceID"));
	String value = request.getParameter("mode");
	if(value.equals("비방범모드")) { // 비방범모드로 변경
		ArduinoCommunicationServer.setNonAutoSecureMode(id);
	}
	else if(value.equals("방범모드")) { // 방범모드로 변경
		ArduinoCommunicationServer.setAutoSecureMode(id);
	}
%>
<!DOCTYPE html>
<html>
	<head>
	<meta charset="UTF-8">
	<script type="text/javascript">
	window.self.close()
	</script>
</head>
<body>

</body>
</html>