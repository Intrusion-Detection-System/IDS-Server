package project.ids;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;;

class ArduinoCommunicationServer {
    //private static List<Device> deviceList = new ArrayList<>();
    private static Vector<Device> deviceList = new Vector<>();
    
    private static final int CONTROL_fromDevice = 0xFF, CONTROL_fromServer = 0;
    private static final byte OP_REQ = 1, OP_RESPONSE = 2;
    private static final int DATA_EOF = 0xFF;
    private static final byte DATA_REQ_MAC = 1;

    public static void main(String argv[]) throws Exception {
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
                
                Device device = registerDevice(connectionSocket);

                if (device.Mac.equals("")) //REQ메시지가 올바르지 X
                {
                	System.out.println("wrong register REQ...");
                	connectionSocket.close();
                }
                else //REQ가 올바르다.
                {
                	DeviceHandler connection = new DeviceHandler(device);
                    (new Thread(connection)).start();
                    deviceList.add(device);
                    System.out.println("[" + device.sensorID + "-" + device.groupID + "-" + device.deviceID + "] device registered..." );
                    //TODO : registerDeviceResponse();
                }

            }
        } catch (IOException e) {

        }
    }

    private static Device registerDevice(Socket connectionSocket)
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
    	
    	System.arraycopy(buff, pos, tDeviceID, 0, 2);  pos += 2; //2~3
    	deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();
    	
    	controlOP = buff[pos++]; //4
    	OP = buff[pos++];  //5

    	
    	if ( ( (int)(controlOP & 0xFF) == CONTROL_fromDevice ) && OP == OP_REQ)	//from Device & registerREQ message... 
    	{
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
        	    		// TODO : DB - get (GroupID & DeviceID) with MAC
        	        	// TODO: group id, device id 정해서 디비 등록
        	        	break;
        	        //TODO : DATA_REQ + alpha
        	        default :
        	        	break;
        		}
    		}

        	///device = new Device(sensorID, groupID, deviceID, Mac, connectionSocket);
    	}
    	else
    	{
    		device = new Device(connectionSocket);
    	}
    	
    	return device;
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
    
    /* 1010
    // 관리자의 호출을 받아 message 전송하는 메소드
    public static void sendSignal(int id,String message) {
        for (Device device : deviceList) {
        	if (device.id == id) {
	            try {
	                DataOutputStream sendToClient = new DataOutputStream(device.socket.getOutputStream());
	                sendToClient.writeBytes(message);
	            } catch (IOException e) {
	
	            }
        	}
        }
    }
    */
    
 // 관리자의 호출을 받아 message 전송하는 메소드
    public static void sendSignal(byte id,String message) {
        for (Device device : deviceList) {
        	if (device.sensorID == id) {
	            try {
	            	OutputStream os= device.socket.getOutputStream();
	            	ByteBuffer sendByteBuffer = null;
	                
	            	sendByteBuffer = ByteBuffer.allocate(1024);
	            	sendByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
	            	
	            	sendByteBuffer.put(device.sensorID);
	            	sendByteBuffer.put(device.groupID);
	            	sendByteBuffer.putShort(device.deviceID);
	            	
	            	sendByteBuffer.put((byte)0);
	            	sendByteBuffer.put((byte)4);
	            	
	            	sendByteBuffer.put((byte)1);
	            	sendByteBuffer.put((byte)1);
	            	sendByteBuffer.put((byte)2);
	            	sendByteBuffer.put((byte)0xFF);
	            	
	            } catch (IOException e) {
	
	            }
        	}
        }
    }
    
}
