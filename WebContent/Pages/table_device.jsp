<%@page import="project.ids.*"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Vector" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
	DatabaseConnection dbConnection = new DatabaseConnection();
	
	//ArduinoCommunicationServer server = ArduinoCommunicationServer.getInstance(); // 일정시간마다 계속 호출 -> 오버헤드 증가
	Vector<Device> aliveDeviceList = ArduinoCommunicationServer.getDeviceList();
%>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css"/>
</head>

<body>
	<table id="deviceTable" style="margin-bottom: 100px">
		<tr id="head">
			<td width="200">디바이스ID</td>
			<td width="200">위치</td>
			<td width="200">상태정보</td>
			<td width="200">측정시간</td>
			<td width="200">환경설정</td>
			<td width="300">제거 및 초기화</td>
			<td width="300">신호전송 및 모드변경</td>
		</tr>
		<%	
		ArrayList<DeviceTableDTO> registeredDeviceList = dbConnection.selectRegisteredDevice();
			
		for(int i=0; i<registeredDeviceList.size(); i++) {
			byte sensorID = registeredDeviceList.get(i).getSensorID();
			byte groupID = registeredDeviceList.get(i).getGroupID();
			byte deviceID = registeredDeviceList.get(i).getDeviceID();
			String location = registeredDeviceList.get(i).getLocation();
			String state = registeredDeviceList.get(i).getAction();
			Timestamp time = registeredDeviceList.get(i).getMeasurementTime();
			String mode = "";
			String setMode = "";
			
			int id = Integer.parseInt(String.format("%d%02d%02d", sensorID, groupID, deviceID));
			
			for(int j=0; j<aliveDeviceList.size(); j++) {
				if(aliveDeviceList.get(j).id == id) {
					if(aliveDeviceList.get(j).auto == true) {
						mode = "(방범모드)";
						setMode = "비방범모드";
					}
					else {
						mode = "(비방범모드)";
						setMode = "방범모드";
					}
				}
			}%>
	
			
			<tr id="connectedDevice">
					<td width="200"><%=id%></td>
					<td id="location<%=id%>"width="200"><%=location%></td>
					<% if (state.equals("열림")) {%>
						<td width="200" style="color: red"><%=state%><br>
						<p style='color: #000000; font-size: 14px;'><%=mode %></p>
						</td>
					<%} else if (state.equals("닫힘")) {%>
						<td width="200" style="color: green"><%=state%>
						<p style='color: #000000; font-size: 14px;'><%=mode %></p>
						</td>
					<%} %>
					<td width="200"><%=time%></td>
					<td width="200">
						<form action="device_log.jsp" target="_blank" method="post">
							<input type="text" value="<%=id%>" name="deviceID" style="display: none;" readonly>
							<input type="submit" value="로그확인">
						</form>
					</td>	
					<td width="300">
						<form action="disconnect_device.jsp" target="_blank" method="post">
							<input type="text" value="<%=id%>" name="deviceID" style="display: none;" readonly>
							<input type="submit" value="제거" >
						</form>
						<form action="reset_device.jsp" target="_blank" method="post">
							<input type="text" value="<%=id%>" name="deviceID" style="display: none;" readonly>
							<input type="submit" value="초기화" >
						</form>
					</td>
					<td width="300">
						<form action="request_signal.jsp" target="_blank" method="post">
							<input type="text" value=<%=id%> name="deviceID" style="display: none;" readonly>
							<input type="submit" value="경고신호">
						</form>
						<form action="change_mode.jsp" target="_blank" method="post">
							<input type="text" value=<%=id%> name="deviceID" style="display: none;" readonly>
							<button type="submit" id=<%=id%> name="mode" value=<%=setMode%>><%=setMode%></button>
						</form>	
					</td> 
				</tr>
			
		<% }%>
	</table>
</body>
</html>