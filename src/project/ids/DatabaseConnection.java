package project.ids;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.*;
import java.util.ArrayList;

public class DatabaseConnection {
	private DataSource dataSource;
	private PreparedStatement pstmt;
	private ResultSet resultSet;

	private static DatabaseConnection instance = null;
	public static DatabaseConnection getInstance() throws SQLException, NamingException {
		if (instance == null) {
			instance = new DatabaseConnection();
		}
		return instance;
	}
	
	/* 테스트용 커넥션
	public DatabaseConnection() {
		String jdbcUrl = "jdbc:mysql://localhost:3306/iotDB?serverTimezone=Asia/Seoul";
		String dbId = "ks";
		String dbPass = "ks";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			connection = DriverManager.getConnection(jdbcUrl, dbId, dbPass);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	*/
	
	/* 운용 커넥션(커넥션풀) */
	private DatabaseConnection() throws NamingException, SQLException {
		Context context = new InitialContext();
		dataSource = (DataSource) context.lookup("java:comp/env/jdbc/idsDB");
	}
	
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	// 등록된 디바이스 조회
	// 여러 개의 검색결과가 있을 수 있으므로 ArrayList로 반환
	public ArrayList<DeviceTableDTO> selectRegisteredDevice() throws NamingException, SQLException {
		Connection connection = getConnection();
		ArrayList<DeviceTableDTO> deviceList = new ArrayList<>();
		
		// 조인된 결과는 별도의 테이블이 필요
		String query = 
				"SELECT device.id, device.position, status.action, status.time " +
				"FROM device, status " +
				"WHERE device.id = status.device_id " +
				"AND status.id in (SELECT max(status.id) FROM status GROUP BY status.device_id)";
		
		pstmt = connection.prepareStatement(query);
		resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			DeviceTableDTO deviceTableDTO = new DeviceTableDTO();
			deviceTableDTO.setDeviceID(resultSet.getInt("device.id"));
			deviceTableDTO.setPosition(resultSet.getString("device.position"));
			deviceTableDTO.setAction(resultSet.getString("status.action"));
			deviceTableDTO.setMeasurementTime(resultSet.getTimestamp("time"));
			
			deviceList.add(deviceTableDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return deviceList;
	}
	
	// 전체 로그 조회
	public ArrayList<LogTableDTO> selectLogTable() throws NamingException, SQLException {
		Connection connection = getConnection();
		ArrayList<LogTableDTO> logList = new ArrayList<>();
		
		String query = 
				"SELECT status.time, device.position, status.action, status.sensor_data " +
				"FROM device, status " +
				"WHERE device.id = status.device_id " +
				"ORDER BY status.time asc";
		
		pstmt = connection.prepareStatement(query);
		resultSet = pstmt.executeQuery();
		
		while(resultSet.next()) {
			LogTableDTO logTableDTO = new LogTableDTO();
			logTableDTO.setMeasurementTime(resultSet.getTimestamp("status.time"));
			logTableDTO.setPosition(resultSet.getString("device.position"));
			logTableDTO.setAction(resultSet.getString("status.action"));
			logTableDTO.setSensorData(resultSet.getInt("status.sensor_data"));
			
			logList.add(logTableDTO);
		}
		
		if(resultSet != null) resultSet.close();
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
		
		return logList;
	}
	
	// 디바이스 제거 (완전 삭제)
	public void removeDevice(int deviceID) throws SQLException { // 지우고자하는 디바이스ID
		Connection connection = getConnection();
		/* ThreadController에서 제거
		ArrayList<ThreadController> tcList = IoT_Server.getTcList();
		tcList.get(i).getTco().setTco(10);
		tcList.remove(i);
		*/
		
		String statusDeleteQuery = "DELETE FROM status WHERE device_id=?";
		String deviceDeleteQuery = "DELETE FROM device WHERE id=?";
		
		pstmt = connection.prepareStatement(statusDeleteQuery);
		pstmt.setInt(1, deviceID);
		pstmt.executeUpdate();
		
		pstmt.setInt(1, deviceID);
		pstmt.executeUpdate();
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
	
	// 디바이스 위치 업데이트
	public void updateDevicePosition(int deviceID, String position) throws SQLException { // 바꾸고자하는 디바이스ID, 위치값
		Connection connection = getConnection();
		String query = "UPDATE device SET position=? WHERE id=?";
		
		pstmt = connection.prepareStatement(query);
		pstmt.setString(1, position);
		pstmt.setInt(2, deviceID);
		pstmt.executeUpdate(); 
		
		if(pstmt != null) pstmt.close();
		if(connection != null) connection.close();
	}
}
