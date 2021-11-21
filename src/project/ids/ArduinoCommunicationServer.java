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
        System.out.println("TEST");
        System.out.println("Connecting to port " + port);
        try (ServerSocket welcomeSocket = new ServerSocket(port)) {
            boolean isRunning = true;
            while (isRunning) {
                System.out.print("Waiting for connection... ");
                
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("connection received!");
                
                registerDevice(connectionSocket);
                
                //TODO : TRUE or FALSE
                /**
                 * 관리자가 그룹을 지정하고 승인을 누르면 아래 로직 진행
                 */
                
                /*
                Device device = registerDevice(connectionSocket);

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
            	*/

            }
        } catch (IOException e) {

        }
    }
    
    // 수정
    private static void registerDevice(Socket connectionSocket)
    {
    	int read=0, pos=0;
    	byte[] buff = new byte[1024];
    	byte[] tDeviceID = new byte[2];
    	BufferedInputStream bis;
    	
    	byte sensorID, groupID, controlOP, OP;
    	short deviceID;
    	String Mac; byte[] tMac = new byte[6]; 
    	Device device;
    	
    	
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
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							System.out.println("ERROR: 이미 테이블에 등록된 디바이스입니다.");
						}
						
						// TODO : DB - get (GroupID & DeviceID) with MAC --> Device에 정보저장 하세요
        	        	break;
        	        //TODO : DATA_REQ + alpha
        	        default :
        	        	break;
        		}
    		}

        	///device = new Device(sensorID, groupID, deviceID, Mac, connectionSocket);
    	}
    	// 이미 등록한적이 있으면
    	/*
    	else
    	{
    		device = new Device(connectionSocket);
    	}
    	*/
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
    
    
    
    public static void sendSignal(int id, byte[] messages) {
        for (Device device : deviceList) {
        	if (device.id == id) {
	            try {
	                DataOutputStream sendToClient = new DataOutputStream(device.socket.getOutputStream());
	                //sendToClient.writeBytes();
	                sendToClient.write(messages);
	            } catch (IOException e) {
	
	            }
        	}
        }
    }
    
 // 관리자의 호출을 받아 message 전송하는 메소드
    //TODO : opcode 4-제어, 5-밤범,6-비방범,10-삭제 
    public static void sendSignal(int id, byte opcode) {
        for (Device device : deviceList) {
        	if (device.id == id) {
	            try {
	            	OutputStream os= device.socket.getOutputStream();
	            	ByteBuffer sendByteBuffer = null;
	                
	            	sendByteBuffer = ByteBuffer.allocate(1024);
	            	sendByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	            	
	            	sendByteBuffer.put(device.sensorID);
	            	sendByteBuffer.put(device.groupID);
	            	sendByteBuffer.putShort(device.deviceID);
	            	
	            	sendByteBuffer.put((byte)0); //controlOP
	            	sendByteBuffer.put((byte)opcode);
	            	
	            	sendByteBuffer.put((byte)1);
	            	sendByteBuffer.put((byte)1);
	            	sendByteBuffer.put((byte)2);
	            	byte temp = (byte)(255 &(byte) 0xFF);
	            	sendByteBuffer.put(temp);
	            	
	            } catch (IOException e) {
	
	            }
        	}
        }
    }
    
    public static void sendSignal(byte sensorID, byte groupID, short deviceID, byte opcode, byte state) {
    	System.out.println("3");
        for (Device device : deviceList) {
        	System.out.println("4");
        	if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
        		System.out.println("5");
	            try {
	            	OutputStream os= device.socket.getOutputStream();
	            	byte buff[] = new byte[1024];
    	        	buff[0] = device.sensorID;
    	        	buff[1] = device.groupID; //<--Group ID
    	        	buff[2] = 0; //<--Device ID(LOW)
    	        	buff[3] = 0; //<--Device ID(HIGH)
	                
	            	buff[4]=(byte)0; //controlOP
	            	buff[5]=opcode; //controlOP
	            	
	            	buff[6]=(byte)1; //head
	            	buff[7]=(byte)1; //length
	            	buff[8]=state;   //data
	            	buff[9]= (byte)(255 &(byte) 0xFF); //EOF
	            	
    	        	os.write(buff); //FOR TEST...
    	        	os.flush();
    	        	
	            } catch (IOException e) {
	
	            }
        	}
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
