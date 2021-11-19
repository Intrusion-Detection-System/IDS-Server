<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
    
<%request.setCharacterEncoding("utf-8");%>
<%
byte sensorID = Byte.parseByte(request.getParameter("sensorID"));
byte groupID = Byte.parseByte(request.getParameter("groupID"));
String mac = request.getParameter("mac");
// TODO: device_id 자동 지정해주는 로직 구현 필요
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
</head>
<body>

</body>
</html>