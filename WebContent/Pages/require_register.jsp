<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="project.ids.*"%>   
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Vector" %>
<!DOCTYPE html>

<html>
<head>
<meta charset="UTF-8">

</head>
<body>
	<table id="unregisteredDevice" style="margin-bottom: 100px">
		<tr id="head">
			<td width="200">센서ID</td>
			<td width="200">MAC주소</td>
			<td width="50"></td>
		</tr>
	
	<%
	DatabaseConnection dbConnection = new DatabaseConnection();
	
	ArrayList<UnregisteredDevice> unregisteredDevices = dbConnection.selectUnregisteredDevices();
	if(unregisteredDevices.size() > 0) {
		for(int i=0; i<unregisteredDevices.size(); i++) {
			byte sensorID = unregisteredDevices.get(i).getSensorID();
			String mac = unregisteredDevices.get(i).getMac();
	%>		
			<tr id="body">
				<td width="200"><%=sensorID %></td>
				<td width="200"><%=mac %></td>
				<td width="50">
					<form action="register_device.jsp" target="_blank" method="post">
					<input type="submit" value="선택">
					<input type="text" value="<%=sensorID %>" name="sensorID" style="display: none;" readonly>
					<input type="text" value="<%=mac %>" name="mac" style="display: none;" readonly>
					</form>
				</td>
			</tr>		
	<%  }
	}
	%>
	</table>
</body>
</html>