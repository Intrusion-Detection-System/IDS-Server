package project.ids;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

//import IoT_Project.IoT_Server;

public class ArduinoCommunicationServer {
	// private static List<Device> deviceList = new ArrayList<>();
	private static ArduinoCommunicationServer instance = null;
	private static Vector<Device> deviceList = new Vector<Device>();
	private static Vector<UnregisteredDevice> unregisteredDevices = new Vector<>();
	private static final int CONTROL_fromDevice = 0xFF, CONTROL_fromServer = 0;
	private static final byte OP_REQ = 1, OP_RESPONSE = 2;
	private static final int DATA_EOF = 0xFF;
	private static final byte DATA_REQ_MAC = 1;
	private static int maxCnt = 5;
	private static int cnt = 0;

	public static void main(String argv[]) throws Exception {
		instance = ArduinoCommunicationServer.getInstance();
	}

	private ArduinoCommunicationServer() {
		Executors.newSingleThreadExecutor().execute(() -> startServer());
	}

	public static void startServer() {
		int port = getPort();
		System.out.println("Connecting to port " + port);

		try (ServerSocket welcomeSocket = new ServerSocket(port)) {

			boolean isRunning = true;
			while (isRunning) {
				System.out.print("Waiting for connection... ");

				Socket connectionSocket = welcomeSocket.accept();
				System.out.println("connection received!");

				// 새로운 등록
				Device device = registerDevice(connectionSocket);
				if (device != null) { // 이미 등록된 디바이스
					addDevice(device, connectionSocket);
				}
			}
		} catch (IOException e) {

		}
	}

	/**
	 * 로직수정: 연결요청이 들어오면 바로 DB에 등록 -> 등록대기상태로 두고 관리자 승인하에 등록
	 */
	private static Device registerDevice(Socket connectionSocket) {
		int read = 0, pos = 0;
		byte[] buff = new byte[1024];
		byte[] tDeviceID = new byte[2];
		BufferedInputStream bis;

		byte sensorID, groupID, controlOP, OP;
		short deviceID;
		String Mac;
		byte[] tMac = new byte[6];
		Device device = null;

		try {
			bis = new BufferedInputStream(connectionSocket.getInputStream());
			read = bis.read(buff, 0, 1024);
		} catch (IOException e) {
			System.out.println("Socket.read ERROR");
		}

		sensorID = buff[pos++]; // 0
		groupID = buff[pos++]; // 1

		System.arraycopy(buff, pos, tDeviceID, 0, 2);
		pos += 2; // 2-3
		deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();

		controlOP = buff[pos++]; // 4
		OP = buff[pos++]; // 5

		System.out.println(sensorID + " " + groupID + " " + deviceID + " " + controlOP + " " + OP);
		if (((int) (controlOP & 0xFF) == CONTROL_fromDevice) && OP == OP_REQ) // from Device & registerREQ message...
		{
			System.out.println("IN");
			device = new Device(sensorID, connectionSocket);
			while (true) {
				byte DataHeader = buff[pos++];
				byte DataLength = buff[pos++];
				if ((int) (DataHeader & 0xFF) == DATA_EOF) // If Header == EOF
				{
					break;
				}

				switch (DataHeader) {
				case DATA_REQ_MAC:
					System.arraycopy(buff, pos, tMac, 0, 6);
					pos += 6;
					Mac = byteToHex(tMac);
					device.Mac = Mac;
					DatabaseConnection dbConnection = new DatabaseConnection();
					try {
						// TODO 이미 등록된 디바이스인지 확인
						Device registeredDevice = dbConnection.checkRegisterdDevice(Mac);
						if (registeredDevice == null) { // 동록되어있지 않음
							System.out.println("unregisteredDevices 데이터 삽입");
							dbConnection.insertRequestedDevice(sensorID, Mac);
							unregisteredDevices.add(new UnregisteredDevice(Mac, connectionSocket)); // request_register.jsp에서
																									// 등록처리
							return null;
						}

						else { // 이미 등록된 디바이스
							System.out.println("이미 등록된 디바이스 (addDevice 메소드 즉시 실행");
							registeredDevice.setSocket(connectionSocket);
							return registeredDevice;
						}

					} catch (SQLException e) {
						e.printStackTrace();
					}

					break;
				// TODO : DATA_REQ + alpha
				default:
					break;
				}
			}
		}

		return device;
	}

	// 디바이스 등록 승인
	public static void addDevice(Device device, Socket connectionSocket) throws IOException {
		System.out.println("debug: addDevice");
		System.out.println("sensor id: " + device.sensorID);
		System.out.println("group id: " + device.groupID);
		System.out.println("device id: " + device.deviceID);
		System.out.println("mac: " + device.Mac);

		// 통합 id 설정
		device.setId(device.sensorID, device.groupID, device.deviceID);

		// 통합 id
		System.out.println("id: " + device.id);
		System.out.println("socket: " + device.socket.toString());
		if (device.Mac.equals("")) // REQ메시지가 올바르지 X
		{
			System.out.println("wrong register REQ...");
			connectionSocket.close();
			// TODO : Error Message
		} else // REQ가 올바르다.
		{
			
			ByteBuffer data = null;
			data = ByteBuffer.allocate(1024);
			data.order(ByteOrder.LITTLE_ENDIAN);

			data.put(device.sensorID);
			data.put(device.groupID);
			data.putShort(device.deviceID);

			data.put((byte) 0); // controlOP : Server
			data.put((byte) 2); // OP : ANSWER
			data.put((byte) 1); //HEAD : DeviceID
			data.put((byte) 2); //LEN : 2
			data.putShort(device.deviceID); //ID
			data.put((byte) 0xFF); //EOF
			// TODO : registerDeviceResponse();
			
			OutputStream os = device.socket.getOutputStream();
			os.write(data.array()); // FOR TEST...
			os.flush();
			// sendSignal(device.id, buff);
			/////////////////////////////

			DeviceHandler connection = new DeviceHandler(device);
			device.socket.setSoTimeout(10000);
			(new Thread(connection)).start();
			// Test-----
			deviceList.add(device);
			System.out.println(
					"[" + device.sensorID + "-" + device.groupID + "-" + device.deviceID + "] device registered...");
			System.out.println(deviceList + " - " + deviceList.size());
			// ---------
			// TODO : registerDeviceResponse();
		}
	}

	// request_register.jsp 에서 호출
	public void requestRegister(UnregisteredDevice unregisteredDevice) throws IOException {
		System.out.println("debug: requestRegister");
		Socket socket = unregisteredDevice.getConnectionSocket();
		Device device = new Device(unregisteredDevice.getSensorID(), unregisteredDevice.getGroupID(),
				unregisteredDevice.getDeviceID(), unregisteredDevice.getMac(), socket);

		addDevice(device, socket);
	}

	// Device list 표시
	private static String listDevices(String line) {
		String response = "";

		for (Device device : deviceList) {
			response += device.toString() + "\n";
		}

		return response;
	}

	public static String byteToHex(byte[] b) {
		StringBuilder sb = new StringBuilder();
		for (int i = 5; i >= 0; i--) {
			if (b[i] < 16) {
				System.out.print("0");
			}
			// System.out.print(b[i]);
			sb.append(String.format("%02x", b[i] & 0xff));
			if (i > 0) {
				sb.append(String.format(":"));
			}

		}

		return sb.toString();
	}

	public static int getPort() {
		int defaultPort = 12345;

		String portEnv = System.getenv("USER");
		if (portEnv != null) {
			try {
				return Integer.valueOf(System.getenv("PORT"));
			} catch (NumberFormatException e) {
				System.out.println("Invalid port: " + portEnv);
			}
		}
		return defaultPort;
	}

	public static void deleteDevice(int id) {
		Iterator<Device> iterator = deviceList.iterator();
		while (iterator.hasNext()) {
			Device device = iterator.next();
			if (device.id == id) {
				// DB 단말기 정보 삭제
				DatabaseConnection dbConnection = new DatabaseConnection();
				try {
					dbConnection.deleteStatus(device.sensorID, device.groupID, device.deviceID);
					dbConnection.deleteDevice(device.sensorID, device.groupID, device.deviceID);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				ByteBuffer data = null;
				data = ByteBuffer.allocate(1024);
				data.order(ByteOrder.LITTLE_ENDIAN);

				data.put(device.sensorID);
				data.put(device.groupID);
				data.putShort(device.deviceID);

				data.put((byte) 0); // controlOP : Server
				data.put((byte) 10); // OP : DELETE
				data.put((byte) 0xFF);
				sendSignal(device, data.array());
				
				iterator.remove();
				device.isRunning=false;
				device = null;
			}
		}
	}

	public static void resetDevice(int id) {
		Iterator<Device> iterator = deviceList.iterator();
		while (iterator.hasNext()) {
			Device device = iterator.next();
			if (device.id == id) {
				// DB 단말기 정보 삭제
				DatabaseConnection dbConnection = new DatabaseConnection();
				try {
					dbConnection.deleteStatus(device.sensorID, device.groupID, device.deviceID);
					dbConnection.deleteDevice(device.sensorID, device.groupID, device.deviceID);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				ByteBuffer data = null;
				data = ByteBuffer.allocate(1024);
				data.order(ByteOrder.LITTLE_ENDIAN);

				data.put(device.sensorID);
				data.put(device.groupID);
				data.putShort(device.deviceID);

				data.put((byte) 0); // controlOP : Server
				data.put((byte) 8); // OP : RESET
				data.put((byte) 0xFF);
			
				
				sendSignal(device, data.array());

				iterator.remove();
				device.isRunning = false;
				device = null;
			}
		}
	}

	public static void setAutoSecureMode(int id) {
		for (Device device : deviceList) {
			if (device.id == id) {
				// TODO : DB 단말기 정보 변경
				System.out.println("방범모드로 변경");
				device.auto = true;

				ByteBuffer data = null;
				data = ByteBuffer.allocate(1024);
				data.order(ByteOrder.LITTLE_ENDIAN);

				data.put(device.sensorID);
				data.put(device.groupID);
				data.putShort(device.deviceID);

				data.put((byte) 0); // controlOP : Server
				data.put((byte) 5); // OP : SecureMode

				data.put((byte) 1); // HEADER
				data.put((byte) 1); // LEN
				data.put((byte) 1); // AutoMode
				byte EOF = (byte) (255 & (byte) 0xFF);
				data.put(EOF);

				sendSignal(device, data.array());
			}
		}
	}

	public static void setNonAutoSecureMode(int id) {
		for (Device device : deviceList) {
			if (device.id == id) {
				// TODO : DB 단말기 정보 변경
				System.out.println("비방범모드로 변경");
				device.auto = false;

				ByteBuffer data = null;
				data = ByteBuffer.allocate(1024);
				data.order(ByteOrder.LITTLE_ENDIAN);

				data.put(device.sensorID);
				data.put(device.groupID);
				data.putShort(device.deviceID);

				data.put((byte) 0); // controlOP : Server
				data.put((byte) 5); // OP : SecureMode

				data.put((byte) 1); // HEADER
				data.put((byte) 1); // LEN
				data.put((byte) 0); // AutoMode
				byte EOF = (byte) (255 & (byte) 0xFF);
				data.put(EOF);

				sendSignal(device, data.array());
			}
		}
	}
	
	public boolean getMode(int id) {
		boolean auto = false;
		for(Device device : deviceList) {
			if(device.id == id) {
				auto = device.auto;
			}
		}
		return auto;
	}

	// 관리자의 호출을 받아 message 전송하는 메소드
	// TODO : opcode 4-제어, 5-밤범, 10-삭제
	// public static void sendSignal(byte sensorID, byte groupID, short deviceID,
	// byte opcode, byte state) {
	public static void sendSignal(int id, byte[] data) {
		System.out.println("3");
		for (Device device : deviceList) {
			System.out.println("4");
			if (device.id == id) {
				System.out.println("5");
				try {
					OutputStream os = device.socket.getOutputStream();

					ByteBuffer sendByteBuffer = null;

					sendByteBuffer = ByteBuffer.allocate(1024);
					sendByteBuffer.order(ByteOrder.LITTLE_ENDIAN);

					sendByteBuffer.put(device.sensorID);
					sendByteBuffer.put(device.groupID);
					sendByteBuffer.putShort(device.deviceID);

					sendByteBuffer.put((byte) 0); // controlOP
					sendByteBuffer.put(data);

					os.write(sendByteBuffer.array());
					os.flush();

				} catch (IOException e) {
					System.out.println("Can not declare OutputStream");
				}
			}
		}
	}
	
	// jsp에서 호출
	public static void sendActionSignal(int id) {
		for (Device device : deviceList) {
			if (device.id == id) {
				try {
					OutputStream os = device.socket.getOutputStream();

					ByteBuffer data = null;
					data = ByteBuffer.allocate(1024);
					data.order(ByteOrder.LITTLE_ENDIAN);

					data.put(device.sensorID);
					data.put(device.groupID);
					data.putShort(device.deviceID);

					data.put((byte) 0); // controlOP : Server
					data.put((byte) 4); // OP : Control

					data.put((byte) 1); // HEADER
					data.put((byte) 4); // LEN
					data.putInt(device.actionTime); // ActionTime
					byte EOF = (byte) (255 & (byte) 0xFF);
					data.put(EOF);
					
					os.write(data.array());
					os.flush();

				} catch (IOException e) {
					System.out.println("Can not declare OutputStream");
				}
			}
		}
	}
	
	public static void changeActionTime(int id, int actionTime)
	{
		for (Device device : deviceList) {
			if (device.id == id) {
				device.actionTime = actionTime;
			}
		}
	}

	// TODO Timer task
	
	public static void addCount()
	{
		
		if(cnt++ > maxCnt)
			sendAllDevice();
	}
	
	public static void sendAllDevice() {
		for (Device device : deviceList) {
			try {
				OutputStream os = device.socket.getOutputStream();

				ByteBuffer data = null;
				data = ByteBuffer.allocate(1024);
				data.order(ByteOrder.LITTLE_ENDIAN);

				data.put(device.sensorID);
				data.put(device.groupID);
				data.putShort(device.deviceID);

				data.put((byte) 0); // controlOP : Server
				data.put((byte) 4); // OP : Control

				data.put((byte) 1); // HEADER
				data.put((byte) 4); // LEN
				data.putInt(device.actionTime); // ActionTime
				byte EOF = (byte) (255 & (byte) 0xFF);
				data.put(EOF);
				
				os.write(data.array());
				os.flush();

			} catch (IOException e) {
				System.out.println("Can not declare OutputStream");
			}
		
		}
	}
	

	// 자동 호출 메소드
	public static void sendSignal(Device device, byte[] data) {

		try {
			OutputStream os = device.socket.getOutputStream();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// singleton
	public static ArduinoCommunicationServer getInstance() throws IOException {
		System.out.println("Get Instance");
		if (instance == null) {
			System.out.println("New one");
			instance = new ArduinoCommunicationServer();
		}
		return instance;
	}

	// getter
	public static Vector<Device> getDeviceList() {
		return deviceList;
	}

	public static Vector<UnregisteredDevice> getUnregisteredDevices() {
		return unregisteredDevices;
	}
}