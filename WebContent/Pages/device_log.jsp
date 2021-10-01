<%@page import="project.ids.DatabaseConnection"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.sql.Timestamp" %>
<%@ page import="project.ids.LogTableDTO"%>
<%@ page import="project.ids.ArduinoCommunicationServer"%>
<%request.setCharacterEncoding("utf-8"); %>
<%
	DatabaseConnection dbConnection = DatabaseConnection.getInstance();
	int deviceID = Integer.parseInt(request.getParameter("deviceID"));
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<style type="text/css">
	#logBox {
		text-align: center;
		line-height: 2.5em;
		border-collapse: collapse;
		width: 60%;
		margin: auto;
		border-bottom: 2px solid #d8d8d8;
	}
	
	#head {
	background-color: #c4ddfe;
	font-weight: bold;
}
</style>
</head>
<body>
	<table id="logBox">
		<tr id="head">
			<td width="350">측정시간</td>
			<td width="350">위치</td>
			<td width="200">상태정보</td>
			<td width="200">측정값</td>
		</tr>
		<%
		ArrayList<LogTableDTO> deviceLogList = dbConnection.selectDeviceLogList(deviceID);
		for(int i=0; i<deviceLogList.size(); i++) {
			Timestamp time = deviceLogList.get(i).getMeasurementTime();
			String position = deviceLogList.get(i).getPosition();
			String state = deviceLogList.get(i).getAction();
			int sensorData = deviceLogList.get(i).getSensorData();
		%>
			<tr>
			<%if(state.equals("열림")) {%>
				<td width="350" style="color: red"><%=time %></td>
				<td width="350" style="color: red"><%=position %></td>
				<td width="200" style="color: red"><%=state %></td>
				<td width="200" style="color: red"><%=sensorData %></td>
			<%} else if(state.equals("닫힘")) {%>
				<td width="350"><%=time %></td>
				<td width="350"><%=position %></td>
				<td width="200"><%=state %></td>
				<td width="200"><%=sensorData %></td>
			<%} %>
			</tr>
		<% }%>
	</table>
</body>
</html>