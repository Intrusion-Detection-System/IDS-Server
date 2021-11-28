<%@page import="org.apache.commons.collections4.bag.SynchronizedSortedBag"%>
<%@page import="project.ids.UnregisteredDevice"%>
<%@page import="project.ids.Device"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="project.ids.ArduinoCommunicationServer" %>
<%@ page import="java.util.Vector" %>
<%@ page import="project.ids.*" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
   <%
      ArduinoCommunicationServer server = ArduinoCommunicationServer.getInstance();
      Vector<Device> deviceList = server.getDeviceList();
      
      if(deviceList.size() > 0) {
    	  for(int i=0; i<deviceList.size(); i++) {
    		  System.out.println("mac: " + deviceList.get(i).Mac);
    	  }
      }
   %>
</body>
</html>