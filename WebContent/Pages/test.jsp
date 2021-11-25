<%@page import="project.ids.UnregisteredDevice"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.ArduinoCommunicationServer" %>
<%@ page import="java.util.Vector" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		ArduinoCommunicationServer server = ArduinoCommunicationServer.getInstance();
		Vector<String> list = server.getTestList();
		
		if(list.size() > 0) {
			System.out.println(list.get(0));
		}
		
		else {
			System.out.println("데이터 없음");
		}
	%>
</body>
</html>