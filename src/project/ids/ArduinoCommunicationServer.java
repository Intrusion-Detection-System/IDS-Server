package project.ids;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.naming.NamingException;

//import IoT_Project.IoT_Server;

public class ArduinoCommunicationServer {
    //private static List<Device> deviceList = new ArrayList<>();
    private static Vector<Device> deviceList = new Vector<>();
    private static Vector<UnregisteredDevice> unregisteredDevices = new Vector<>();
    private static final int CONTROL_fromDevice = 0xFF, CONTROL_fromServer = 0;
    private static final byte OP_REQ = 1, OP_RESPONSE = 2;
    private static final int DATA_EOF = 0xFF;
    private static final byte DATA_REQ_MAC = 1;

    public static void main(String argv[]) throws Exception {
        instance = ArduinoCommunicationServer.getInstance();
    }

    private ArduinoCommunicationServer() {
    	startServer();
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
                if(device != null) { // 이미 등록된 디바이스
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
    	int read=0, pos=0;
    	byte[] buff = new byte[1024];
    	byte[] tDeviceID = new byte[2];
    	BufferedInputStream bis;
    	
    	byte sensorID, groupID, controlOP, OP;
    	short deviceID;
    	String Mac; byte[] tMac = new byte[6]; 
    	Device device = null;
    	
    	try {
    		bis = new BufferedInputStream(connectionSocket.getInputStream());
			read = bis.read(buff, 0, 1024);
		} catch (IOException e) {
			System.out.println("Socket.read ERROR");
		}
    	
    	sensorID = buff[pos++]; //0
    	groupID = buff[pos++]; //1
    	
    	System.arraycopy(buff, pos, tDeviceID, 0, 2);  pos += 2; //2-3
    	deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();
    	
    	controlOP = buff[pos++]; //4
    	OP = buff[pos++];  //5

    	System.out.println(sensorID+ " " + groupID+ " " + deviceID+ " " + controlOP+ " " + OP);
    	if ( ( (int)(controlOP & 0xFF) == CONTROL_fromDevice ) && OP == OP_REQ)	//from Device & registerREQ message... 
    	{
    		System.out.println("IN");
    		device = new Device(sensorID, connectionSocket);
    		while(true)
    		{
    			byte DataHeader = buff[pos++];
    			byte DataLength = buff[pos++];
        		if((int)(DataHeader & 0xFF) == DATA_EOF)	//If Header == EOF
        		{
        			break;
        		}
        		
        		switch(DataHeader)
        		{
        			case DATA_REQ_MAC:
        				System.arraycopy(buff, pos, tMac, 0, 6); pos += 6;
        	        	Mac = byteToHex(tMac);
        	        	device.Mac = Mac;
        	        	
        	        	// 등록요청된 디바이스 객체 db에 저장
                        DatabaseConnection dbConnection = new DatabaseConnection();
						try {
							dbConnection.insertRequestedDevice(sensorID, Mac);
							unregisteredDevices.add(new UnregisteredDevice(Mac, connectionSocket)); // request_register.jsp 에서 등록 처리
						} catch (SQLException e) {
							// TODO Auto-generated catch block
						}
						
        	        	break;
        	        //TODO : DATA_REQ + alpha
        	        default :
        	        	break;
        		}
    		}
    	}
    	// 이미 등록한적이 있으면
    	else
    	{
    		device = new Device(connectionSocket);
    	}
    	
    	return device;
    }
    
    // 디바이스 등록 승인
    public static void addDevice(Device device, Socket connectionSocket) throws IOException {
    	if (device.Mac.equals("")) //REQ메시지가 올바르지 X
        {
        	System.out.println("wrong register REQ...");
        	connectionSocket.close();
        	//TODO : Error Message
        }
        else //REQ가 올바르다.
        {
        	
        	//TODO : registerDeviceResponse();
            byte buff[] = new byte[1024];
        	buff[0] = device.sensorID;
        	buff[1] = device.groupID; //<--Group ID
        	buff[2] = 0; //<--Device ID(LOW)
        	buff[3] = 0; //<--Device ID(HIGH)
        	buff[4] = 0; //server
        	buff[5] = 2; //ANSWER
        	buff[6] = 1; //HEAD[Group ID]
        	buff[7] = 1; //<--Group ID
        	buff[8] = 2; //HEAD[DeviceID]
        	buff[9] = 2; //<--Device ID(HIGH)
        	buff[10] = 3;//<--Device ID(LOW)
        	buff[11] = (byte)0xFF;
        	OutputStream os = device.socket.getOutputStream();
        	os.write(buff); //FOR TEST...
        	os.flush();
        	//sendSignal(device.id, buff);
        	/////////////////////////////
        	
        	DeviceHandler connection = new DeviceHandler(device);
            (new Thread(connection)).start();
            deviceList.add(device);
            System.out.println("[" + device.sensorID + "-" + device.groupID + "-" + device.deviceID + "] device registered..." );
            
          //TODO : registerDeviceResponse();
        }
    }
    
    // request_register.jsp 에서 호출
    public void requestRegister(UnregisteredDevice unregisteredDevice) throws IOException {
    	Socket socket = unregisteredDevice.getConnectionSocket();
    	Device device = new Device(unregisteredDevice.getSensorID(), unregisteredDevice.getGroupID(), 
    							   unregisteredDevice.getDeviceID(), unregisteredDevice.getMac(), socket);
    	
    	addDevice(device, socket);
    }
    
    //Device list 표시
    private static String listDevices(String line) {
        String response = "";

        for (Device device : deviceList) {
            response += device.toString() + "\n";
        }

        return response;
    }

    public static String byteToHex(byte[] b) {
	    StringBuilder sb = new StringBuilder();
	   for (int i = 5; i>=0; i--) {
	       	if(b[i] < 16) {
	       		System.out.print("0");
	       	}
	       	//System.out.print(b[i]);
	       	sb.append(String.format("%02x", b[i]&0xff));
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
    
    public static void deleteDevice(byte sensorID, byte groupID, short deviceID)
    {
    	for (Device device : deviceList) {
    		if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
    			//TODO : DB 단말기 정보 삭제
    			
    			ByteBuffer data = null;
    			data = ByteBuffer.allocate(1024);
    			data.order(ByteOrder.LITTLE_ENDIAN);
    			
    			data.put(device.sensorID);
    			data.put(device.groupID);	
    			data.putShort(device.deviceID);	
            	
    			data.put((byte)0); 		//controlOP : Server
    			data.put((byte)10); 	//OP : DELETE
    			
    			sendSignal(device, data.array());
    			
    			deviceList.remove(device);
    		}
    	}
    }
    
    public static void setAutoSecureMode(byte sensorID, byte groupID, short deviceID)
    {
    	for (Device device : deviceList) {
    		if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
    			//TODO : DB 단말기 정보 변경
    			
    			device.auto = true;
    			
    			ByteBuffer data = null;
    			data = ByteBuffer.allocate(1024);
    			data.order(ByteOrder.LITTLE_ENDIAN);
    			
    			data.put(device.sensorID);
    			data.put(device.groupID);	
    			data.putShort(device.deviceID);	
            	
    			data.put((byte)0); 		//controlOP : Server
    			data.put((byte)5); 	//OP : SecureMode
    			
    			data.put((byte)1);	//HEADER
    			data.put((byte)1);	//LEN
    			data.put((byte)1);	//AutoMode
    			byte EOF = (byte)(255 &(byte) 0xFF);
            	data.put(EOF);	
    			
            	sendSignal(device, data.array());
            	
    		}
    	}
    }
    
    public static void setNonAutoSecureMode(byte sensorID, byte groupID, short deviceID)
    {
    	for (Device device : deviceList) {
    		if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
    			//TODO : DB 단말기 정보 변경
    			
    			device.auto = false;
    			
    			ByteBuffer data = null;
    			data = ByteBuffer.allocate(1024);
    			data.order(ByteOrder.LITTLE_ENDIAN);
    			
    			data.put(device.sensorID);
    			data.put(device.groupID);	
    			data.putShort(device.deviceID);	
            	
    			data.put((byte)0); 		//controlOP : Server
    			data.put((byte)5); 	//OP : SecureMode
    			
    			data.put((byte)1);	//HEADER
    			data.put((byte)1);	//LEN
    			data.put((byte)0);	//AutoMode
    			byte EOF = (byte)(255 &(byte) 0xFF);
            	data.put(EOF);	
    			
            	sendSignal(device, data.array());
    		}
    	}
    }
    
    // 관리자의 호출을 받아 message 전송하는 메소드
    //TODO : opcode 4-제어, 5-밤범, 10-삭제     
    //public static void sendSignal(byte sensorID, byte groupID, short deviceID, byte opcode, byte state) {
    public static void sendSignal(byte sensorID, byte groupID, short deviceID, byte[] data) {
    	System.out.println("3");
        for (Device device : deviceList) {
        	System.out.println("4");
        	if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
        		System.out.println("5");
	            try {
	            	OutputStream os= device.socket.getOutputStream();
	            	
	            	ByteBuffer sendByteBuffer = null;
	                
	            	sendByteBuffer = ByteBuffer.allocate(1024);
	            	sendByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	            	
	            	sendByteBuffer.put(device.sensorID);
	            	sendByteBuffer.put(device.groupID);	
	            	sendByteBuffer.putShort(device.deviceID);	
	            	
	            	sendByteBuffer.put((byte)0); 		//controlOP
	            	sendByteBuffer.put(data);
	            	
    	        	os.write(sendByteBuffer.array());
    	        	os.flush();
    	        	
	            } catch (IOException e) {
	            	System.out.println("Can not declare OutputStream");
	            }
        	}
        }
    }
    
    //자동 호출 메소드
    public static void sendSignal(Device device, byte[] data) {
    	
    	try {
			OutputStream os= device.socket.getOutputStream();
			os.write(data);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
    	
    }
    
    //singleton
  	private static ArduinoCommunicationServer instance = null;
  	public static ArduinoCommunicationServer getInstance() throws IOException {
  		System.out.println("Get Instance");
  		if(instance == null) {
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
