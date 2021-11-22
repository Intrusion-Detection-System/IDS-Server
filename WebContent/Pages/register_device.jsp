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
			//$("#locations").load("table_location")
		})
	</script>
</head>
<body>
	<!-- group_id, position, 라디오 박스 -->
	<table>
		<tr>
			<td width="200">그룹ID</td>
			<td width="200">위치</td>
			<td width="50"></td>
		</tr>
	
		<%
		DatabaseConnection dbConnection = DatabaseConnection.getInstance(); 
		ArrayList<LocationDTO> locationList = dbConnection.selectLocationsTable();
		for(int i=0; i<locationList.size(); i++) {
			byte groupID = locationList.get(i).getGroupID();
			String location = locationList.get(i).getLocation();
		%>
		<tr>
			<td width="200"><%=groupID%></td>
			<td width="200"><%=location %></td>
			<td width="50">
				<form action="request_register.jsp" method="post" >
					<input type="text" value="<%=sensorID%>" name="sensorID" style="display: none;" readonly>
					<input type="text" value="<%=groupID%>" name="groupID" style="display: none;" readonly>
					<input type="text" value="<%=mac%>" name="mac" style="display: none;" readonly>
					<input type="text" value="<%=location%>" name="location" style="display: none;" readonly>
					<button type="submit" value="등록"></button>
				</form>
			</td>
		</tr>
		
		<%	
		}
		%>
		
	
	</table>
</body>
</html>