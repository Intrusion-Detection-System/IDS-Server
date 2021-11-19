<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.ArduinoCommunicationServer" %>
<%@ page import="project.ids.Device" %>
<%@ page import="java.util.Vector" %>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css" />
	<script type="text/javascript" src="JQuery/jquery-3.5.1.min.js"></script>
</head>
<body>
	<!-- sensor_id, mac_addr, group_id설정/등록 버튼-->
	<!-- group_id설정/등록 jsp에서 locations 테이블 표시 후 원하는 location 체크 후 등록 -->
	<table>
		<tr>
			<td width="350">센서ID</td>
			<td width="350">MAC주소</td>
			<td width="200">등록</td>
		</tr>
		<% 
		Vector<Device> deviceList = ArduinoCommunicationServer.getDeviceList(); 
		for(int i=0; i<deviceList.size(); i++) {
			byte sensorID = deviceList.get(i).getSensorID();
			String mac = deviceList.get(i).getMac();
		%>
		<tr>
			<td width="350"><%=sensorID %></td>
			<td width="350"><%=mac %></td>
			<td width="200">
				<form action="register_device.jsp" method="post">
					<input type="text" id="sensorID" value=<%=sensorID %> style="display: none">
					<input type="text" id="mac" value=<%=mac %> style="display: none">
					<button id="settings">그룹ID설정/디바이스등록</button>
				</form>
			</td>
		</tr>
		<% 
		}
		%>
	</table>
</body>
</html>