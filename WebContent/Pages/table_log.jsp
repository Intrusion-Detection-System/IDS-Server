<%@page import="java.sql.Timestamp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.*"%>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html>
<%
	DatabaseConnection dbConnection = new DatabaseConnection();
%>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css"/>
</head>
<body>
	<table id="logBox">
		<tr>
			<td width="350">측정시간</td>
			<td width="350">위치</td>
			<td width="200">상태정보</td>
			<td width="200">측정값</td>
		</tr>
		<%
		ArrayList<LogTableDTO> logList = dbConnection.selectLogList();
		for(int i=0; i<logList.size(); i++) {
			Timestamp time = logList.get(i).getMeasurementTime();
			String location = logList.get(i).getLocation();
			String state = logList.get(i).getAction();
			int sensorData = logList.get(i).getSensorData();
		%>
			<tr>
				<%if(state.equals("열림")) {%>
					<td width="350" style="color: red"><%=time%></td>
					<td width="350" style="color: red"><%=location%></td>
					<td width="200" style="color: red"><%=state%></td>
					<td width="200" style="color: red"><%=sensorData%></td>
				<%} else if(state.equals("닫힘")) {%>
					<td width="350"><%=time%></td>
					<td width="350"><%=location%></td>
					<td width="200"><%=state%></td>
					<td width="200"><%=sensorData%></td>
				<%} %>
			</tr>
		<%} %>
	</table>
</body>
</html>