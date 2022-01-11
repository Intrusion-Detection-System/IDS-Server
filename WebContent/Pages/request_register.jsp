<%@page import="java.net.Socket"%>
<%@page import="project.ids.UnregisteredDevice"%>
<%@page import="java.util.Vector"%>
<%@page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
<%@ page import="project.ids.ArduinoCommunicationServer"%>
    
<%request.setCharacterEncoding("utf-8");%>
<%
	byte sensorID = Byte.parseByte(request.getParameter("sensorID"));
	byte groupID = Byte.parseByte(request.getParameter("groupID"));
	String mac = request.getParameter("mac");
	String location = request.getParameter("location");

	// 데이터베이스에 등록(table: devices, status)
	DatabaseConnection dbConnection = new DatabaseConnection();
	
	short id;
	short deviceID;
	// 센서 id가 같고 그룹 id가 같으면 해당 디바이스의 "device_id+1"로 지정
	if((id = dbConnection.checkDeviceID(sensorID, groupID)) != -1) {
		deviceID = id;
		deviceID++;
	}
	else {
		deviceID = dbConnection.countDeviceID();
	}
	dbConnection.insertDevice(sensorID, groupID, deviceID, mac, location);
	dbConnection.insertLogTable(sensorID, groupID, deviceID, "닫힘", 0, mac);
	
	// unregistered_devices 테이블에서 등록된 디바이스 제거
	dbConnection.deleteRegisteredDevice(mac);
	
	// 서버에 등록 요청
	ArduinoCommunicationServer server = ArduinoCommunicationServer.getInstance();
	Vector<UnregisteredDevice> unregisteredDevices = server.getUnregisteredDevices();
	
	if(unregisteredDevices.size() > 0) {
		for(int i=0; i<unregisteredDevices.size(); i++) {
			if(unregisteredDevices.get(i).getMac().equals(mac)) {
				unregisteredDevices.get(i).setSensorID(sensorID);
				unregisteredDevices.get(i).setGroupID(groupID);
				unregisteredDevices.get(i).setDeviceID(deviceID);
				
				// 서버에 등록 요청
				server.requestRegister(unregisteredDevices.get(i));
			}
		}
	}
	else {
		System.out.println("unregisteredDevice 없음");
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