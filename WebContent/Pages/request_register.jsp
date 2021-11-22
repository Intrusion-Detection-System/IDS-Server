<%@page import="java.net.Socket"%>
<%@page import="project.ids.UnregisteredDevice"%>
<%@page import="java.util.Vector"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
<%@ page import="project.ids.ArduinoCommunicationServer"%>
    
<%request.setCharacterEncoding("utf-8");%>
<%
	byte sensorID = Byte.parseByte(request.getParameter("sensorID"));
	byte groupID = Byte.parseByte(request.getParameter("groupID"));
	String mac = request.getParameter("mac");
	String location = request.getParameter("location");

	// 데이터베이스에 등록
	DatabaseConnection dbConnection = new DatabaseConnection();
	byte deviceID = dbConnection.countDeviceID();
	dbConnection.insertDevice(sensorID, groupID, deviceID, mac, location);
	dbConnection.insertLogTable(sensorID, groupID, deviceID, "닫힘", 0);
	
	// unregistered_devices 테이블에서 등록된 디바이스 제거
	//dbConnection.deleteRegisteredDevice(mac);
	
	// 서버에 등록 요청
	ArduinoCommunicationServer server = ArduinoCommunicationServer.getInstance();
	Vector<UnregisteredDevice> unregisteredDevices = server.getUnregisteredDevices();
	
	if(unregisteredDevices.size() > 0) {
		for(int i=0; i<unregisteredDevices.size(); i++) {
			unregisteredDevices.get(i).setSensorID(sensorID);
			unregisteredDevices.get(i).setGroupID(groupID);
			unregisteredDevices.get(i).setDeviceID(deviceID);
			
			server.requestRegister(unregisteredDevices.get(i));
		}
	}
	
	// TEST
	if(unregisteredDevices.size() > 0) {
		System.out.println(unregisteredDevices.get(0).getMac());
	}
	else {
		System.out.println("디바이스 정보 없음");
	}
	
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<script type="text/javascript">
		window.self.close();
	</script>
</head>
<body>
</body>
</html>