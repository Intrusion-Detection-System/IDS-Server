<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css" />
	<script type="text/javascript" src="JQuery/jquery-3.5.1.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// AJAX
			$("#deviceTable").load("table_device.jsp")
			$("#logTable").load("table_log.jsp")
			$("#unregisteredDeviceTable").load("require_register.jsp")
			
			function updateTables() {
				$("#deviceTable").load("table_device.jsp")
				$("#logTable").load("table_log.jsp")
				$("#unregisteredDeviceTable").load("require_register.jsp")
			}
			setInterval(updateTables, 2000);
		})
	</script>
	
	<title>침입 탐지 시스템</title>
</head>
<body>
	<p style="font-size: 20px; margin-top: 30px">관리자 인터페이스</p>
	<div id="deviceTable"></div>
	
	<p style="font-size: 18px;">전체 로그 정보</p>
	<div id="logTable"></div>
	
	<p style="font-size: 20px; margin-top: 80px">디바이스 연결요청</p>
	<div id="unregisteredDeviceTable"></div>
</body>
</html>