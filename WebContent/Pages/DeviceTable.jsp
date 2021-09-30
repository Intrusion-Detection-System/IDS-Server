<%@page import="project.ids.*"%>
<%@page import="java.sql.Timestamp"%>
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Vector" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%
	DatabaseConnection dbConnection = DatabaseConnection.getInstance();
	Vector<Device> aliveDeviceList = ArduinoCommunicationServer.getDeviceList();
%>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css"/>
	<script type="text/javascript" src="JQuery/jquery-3.5.1.min.js"></script>
	<script type="text/javascript">
	</script>
</head>

<body>
	<table id="deviceTable" style="margin-bottom: 100px">
		<tr id="head">
			<td width="200">디바이스ID</td>
			<td width="200">위치</td>
			<td width="200">상태정보</td>
			<td width="200">측정시간</td>
			<td width="200">환경설정</td>
			<td width="200">디바이스 제거</td>
		</tr>
		<%	
		ArrayList<DeviceTableDTO> registeredDeviceList = new ArrayList<>();
		registeredDeviceList = dbConnection.selectRegisteredDevice();
			
		// DeviceList(tcList)의 사이즈가 0이며
		// 하나의 디바이스만 등록되었다가 DeviceList에서 삭제된 경우
		// 해당 디바이스 컬럼의 배경화면을 흑백으로한다.
		if(aliveDeviceList.size() == 0) {
			for(int i=0; i<registeredDeviceList.size(); i++) {
				int deviceID = registeredDeviceList.get(i).getDeviceID();
				String position = registeredDeviceList.get(i).getPosition();
				String state = registeredDeviceList.get(i).getAction();
				Timestamp time = registeredDeviceList.get(i).getMeasurementTime();
		%>
				<tr id="disabledDevice">
					<td width="200"><%=deviceID%></td>
					<td id="position<%=deviceID%>" width="200"><%=position%></td>
					<%if (state.equals("열림")) {%>
						<td width="200" style="color: red"><%=state%><br>
							<form action="Signal.jsp" target="_blank" method="post">
								<input type="text" value=<%=deviceID%> name="deviceID" style="display: none;" readonly>
								<input type="submit" value="경고신호" disabled>
							</form>
						</td>
					<%} else if(state.equals("닫힘")) {%>
						<td width="200" style="color: green"><%=state%></td>
					<%} %>
					<td width="200"><%=time%></td>
					<td width="200">
						<form action="DeviceLog.jsp" target="_blank" method="post">
							<input type="text" value="<%=deviceID%>" name="deviceID" style="display: none;" readonly>
							<button type="button" id=<%=deviceID%> onclick="revise(this.deviceID)">수정</button>
							<input type="submit" value="로그확인"> 
						</form>
					</td>
					<td width="200">
						<form action="Disconnect.jsp" target="_blank" method="post">
							<input type="text" value="<%=deviceID%>" name="deviceID" style="display: none;" readonly>
							<input type="submit" value="제거" disabled>
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
				int deviceID = registeredDeviceList.get(i).getDeviceID();
				String position = registeredDeviceList.get(i).getPosition();
				String state = registeredDeviceList.get(i).getAction();
				Timestamp time = registeredDeviceList.get(i).getMeasurementTime();
				boolean isConnected = false;
				
				for(int j=0; j<aliveDeviceList.size(); j++) {
					if(aliveDeviceList.get(j).getDeviceID() == deviceID)
						isConnected = true; // 연결되어있는 디바이스
				}%>
		
				<%if(isConnected) {%> 
					<tr id="connectedDevice">
						<td width="200"><%=deviceID%></td>
						<td id="position<%=deviceID%>"width="200"><%=position%></td>
						<% if (state.equals("열림")) {%>
							<td width="200" style="color: red"><%=state%><br>
								<form action="Signal.jsp" target="_blank" method="post">
									<input type="text" value=<%=deviceID%> name="deviceID" style="display: none;" readonly>
									<input type="submit" value="경고신호">	
								</form>
							</td>
						<%} else if (state.equals("닫힘")) {%>
							<td width="200" style="color: green"><%=state%></td>
						<%} %>
						<td width="200"><%=time%></td>
						<td width="200">
							<form action="DeviceLog.jsp" target="_blank" method="post">
								<input type="text" value="<%=deviceID%>" name="deviceID" style="display: none;" readonly>
								<button type="button" id=<%=deviceID%> onclick="revise(this.deviceID)">수정</button>
								<input type="submit" value="로그확인">
							</form>
						</td>	
						<td width="200">
							<form action="Disconnect.jsp" target="_blank" method="post">
								<input type="text" value="<%=deviceID%> name="deviceID" style="display: none;" readonly>
								<input type="submit" value="제거" >
							</form>
						</td>
					</tr>
				<%}
				
				else { %>	
					<tr id="disabledDevice">
						<td width="200"><%=deviceID%></td>
						<td id="position<%=deviceID%>"width="200"><%=position%></td>
						<% if (state.equals("열림")) {%>
							<td width="200" style="color: red"><%=state%><br>
								<form action="Signal.jsp" target="_blank" method="post">
									<input type="text" value=<%=deviceID%> name="deviceID" style="display: none;" readonly>
									<input type="submit" value="경고신호" disabled>	
								</form>
							</td>
						<%} if (state.equals("닫힘")) {%>
							<td width="200" style="color: green"><%=state%></td>
						<%} %>
						<td width="200"><%=time%></td>
						<td width="200">
							<form action="DeviceLog.jsp" target="_blank" method="post">
								<input type="text" value="<%=deviceID%>" name="deviceID" style="display: none;" readonly>
								<button type="button" id=<%=deviceID%> onclick="revise(this.deviceID)">수정</button>
								<input type="submit" value="로그확인">
							</form>
						</td>
						<td width="200">
							<form action="Disconnect.jsp" target="_blank" method="post">
								<input type="text" value="<%=deviceID%>" name="deviceID" style="display: none;" readonly>
								<input type="submit" value="제거" disabled>
							</form>
						</td>	
					</tr>
			<%}
			}
		}%>
	</table>
</body>
</html>