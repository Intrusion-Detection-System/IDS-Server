<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.DatabaseConnection" %>
<%@ page import="project.ids.LocationDTO" %>
<%@ page import="java.util.ArrayList" %>
<%request.setCharacterEncoding("utf-8");%>

<%
byte sensorID = Byte.parseByte(request.getParameter("sensorID"));
String mac = request.getParameter("mac");
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<link rel="stylesheet" type="text/css" href="CSS/web-css.css" />
	<script type="text/javascript" src="JQuery/jquery-3.5.1.min.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// AJAX
			$("#locations").load("table_location")
		})
	</script>
</head>
<body>
	<!-- group_id, position, 라디오 박스 -->
	<form action="request_register" method="post">
		<%
		DatabaseConnection dbConnection = DatabaseConnection.getInstance(); 
		ArrayList<LocationDTO> locationList = dbConnection.selectLocationsTable();
		for(int i=0; i<locationList.size(); i++) {
			byte groupID = locationList.get(i).getGroupID();
			String location = locationList.get(i).getLocation();
		%>
		<input type="text" id="sensorID" value=<%=sensorID %> style="display: none">
		<input type="text" id="mac" value=<%=mac %> style="display: none">
		<input type="text" id="groupID" value=<%=groupID%>>
		<input type="text" id="location" value=<%=location %>>
		<!-- group_id는 DB로, location은 웹 페이지 table_device로 -->
		<input type="radio" name="button" value=<%=groupID %>>
		<%	
		}
		%>
		<input type="submit" value="등록">
	</form>
</body>
</html>