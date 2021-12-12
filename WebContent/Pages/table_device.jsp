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
			
		// DeviceList(tcList)의 사이즈가 0이며
		// 하나의 디바이스만 등록되었다가 DeviceList에서 삭제된 경우
		// 해당 디바이스 컬럼의 배경화면을 흑백으로한다.
		if(aliveDeviceList.size() == 0) {
			for(int i=0; i<registeredDeviceList.size(); i++) {
				byte sensorID = registeredDeviceList.get(i).getSensorID();
				byte groupID = registeredDeviceList.get(i).getGroupID();
				byte deviceID = registeredDeviceList.get(i).getDeviceID();
				String location = registeredDeviceList.get(i).getLocation();
				String state = registeredDeviceList.get(i).getAction();
				Timestamp time = registeredDeviceList.get(i).getMeasurementTime();
		
				int id = Integer.parseInt(String.format("%d%02d%02d", sensorID, groupID, deviceID));
		%>
				<tr id="disabledDevice">
					<td width="200"><%=id%></td> <!-- 디바이스id  -->
					<td id="location<%=id%>" width="200"><%=location%></td> <!-- 위치 -->
					<%if (state.equals("열림")) {%>
						<td width="200" style="color: red"><%=state%><br>
						</td>
					<%} else if(state.equals("닫힘")) {%>
						<td width="200" style="color: green"><%=state%></td>
					<%} %>
					<td width="200"><%=time%></td>
					<td width="200">
						<form action="device_log.jsp" target="_blank" method="post">
							<input type="text" value="<%=id%>" name="deviceID" style="display: none;" readonly>
							<input type="submit" value="로그확인"> 
						</form>
					</td>
					<td width="300">
						<form action="" target="_blank" method="post">
							<input type="submit" value="제거" disabled>
						</form>
						<form action="" target="_blank" method="post">
							<input type="submit" value="초기화" disabled>
						</form>
					</td>
					<td width="300">
						<form action="" target="_blank" method="post">
							<input type="submit" value="경고신호" disabled>
						</form>
						<form action="" target="_blank" method="post">
							<input type="button" id="mode" value="방범모드" disabled>
						</form>	
					</td> 
				</tr>
		<%
			}
		}
		// DeviceList를 탐색하여 연결이 끊어진 경우
		// 해당 디바이스 컬럼의 배경색을 흑백으로한다.
		// 연결이 되어있는 디바이스는 정상표시한다.
		else {
			for(int i=0; i<registeredDeviceList.size(); i++) {
				byte sensorID = registeredDeviceList.get(i).getSensorID();
				byte groupID = registeredDeviceList.get(i).getGroupID();
				byte deviceID = registeredDeviceList.get(i).getDeviceID();
				String location = registeredDeviceList.get(i).getLocation();
				String state = registeredDeviceList.get(i).getAction();
				Timestamp time = registeredDeviceList.get(i).getMeasurementTime();
				boolean isConnected = false;
				String mode = "";
				String setMode = "";
				
				int id = Integer.parseInt(String.format("%d%02d%02d", sensorID, groupID, deviceID));
				
				for(int j=0; j<aliveDeviceList.size(); j++) {
					if(aliveDeviceList.get(j).id == id) {
						isConnected = true; // 연결되어있는 디바이스
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
		
				<%if(isConnected) {%> 
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
				<%}
				
				else { %>	
					<tr id="disabledDevice">
						<td width="200"><%=id%></td>
						<td id="location<%=id%>"width="200"><%=location%></td>
						<% if (state.equals("열림")) {%>
							<td width="200" style="color: red"><%=state%><br></td>
						<%} if (state.equals("닫힘")) {%>
							<td width="200" style="color: green"><%=state%></td>
						<%} %>
						<td width="200"><%=time%></td>
						<td width="200">
							<form action="device_log.jsp" target="_blank" method="post">
								<input type="text" value="<%=id%>" name="deviceID" style="display: none;" readonly>
								<input type="submit" value="로그확인">
							</form>
						</td>
						<td width="300">
							<form action="" target="_blank" method="post">
								<input type="submit" value="제거" disabled>
							</form>
							<form action="" target="_blank" method="post">
								<input type="submit" value="초기화" disabled>
							</form>
						</td>
						<td width="300">
							<form action="" target="_blank" method="post">
								<input type="submit" value="경고신호" disabled>
							</form>
							<form action="change_mode.jsp" target="_blank" method="post">
								<input type="button" id="mode" value="방범모드" disabled>
							</form>	
						</td> 
					</tr>
			<%}
			}
		}%>
	</table>
</body>
</html>