<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>디바이스 설정</title>
	<style type="text/css">
		html, body {
			text-align: center;
		}
	</style>
	<script type="text/javascript" src="JQuery/jquery-3.5.1.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#ok").click(function() {
				// 디바이스 위치정보 수정
				var deviceID = opener.parent.getDeviceID()
				var parentID = "position" + deviceID
				var text = $("#position").val()
				
				$("#deviceID").val(deviceID)
				opener.document.getElementById(parentID).innerHTML = text
			})
			
			$("#cancel").click(function() {
				window.self.close()
			})
		})
	</script>
</head>
<body>
	<p style="color: gray;">위치 정보 수정</p>
	<form action="request_revise.jsp" method="post">
		<input text="text" id="position" name="position" placeholder="위치를 입력하세요.">
		<!-- request_revise 파라미터 전달용 -->
		<input text="text" id="deviceID" name="deviceID" style="display: none">
		<br><br>
		<button id="ok">확인</button>
		<button type="button" id="cancel">취소</button>
		<br><br><br><br><br>
		<p style="color: gray;">디바이스 초기화</p>
		<button id="reset">초기화</button>
	</form>
</body>
</html>