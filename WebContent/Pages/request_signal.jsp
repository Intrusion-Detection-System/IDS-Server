<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.*" %>
<%
	request.setCharacterEncoding("utf-8");
	int id = Integer.parseInt(request.getParameter("deviceID"));
	ArduinoCommunicationServer.sendSignal(id, data); // TODO data?
%>
<!DOCTYPE html>
<html>
<head>
	<script type="text/javascript">
		window.self.close();
	</script>
</head>
<body>

</body>
</html>