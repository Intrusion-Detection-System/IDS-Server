<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="project.ids.*"%>   
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Vector" %>
<!DOCTYPE html>

<%
	ArduinoCommunicationServer arduinoServer = ArduinoCommunicationServer.getInstance();
%>

<html>
<head>
<meta charset="UTF-8">

</head>
<body>
	<%
	Vector<UnregisteredDevice> unregisteredDeviceList = arduinoServer.getUnregisteredDevices();
	
	
	if(unregisteredDeviceList.size() > 0) {
		unregisteredDeviceList.get(0).toString();
	}
	else 
	%>
	<b>error</b>
</body>
</html>