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

                if (device.Mac.equals("")) //REQ�޽����� �ùٸ��� X
                {
                	System.out.println("wrong register REQ...");
                	connectionSocket.close();
                }
                else //REQ�� �ùٸ���.
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

    private static Device registerDevice(Socket connectionSocket) {
    	int read=0, pos=0;
    	byte[] buff = new byte[1024];
    	byte[] tDeviceID = new byte[2];
    	BufferedInputStream bis;
    	
    	byte sensorID, groupID, controlOP, OP;
    	short deviceID;
    	String Mac; 
    	byte[] tMac = new byte[6]; 
    	Device device;
    	
    	try {
    		bis = new BufferedInputStream(connectionSocket.getInputStream());
			read = bis.read(buff, 0, 1024);
		} catch (IOException e) {
			System.out.println("Socket.read ERROR");
		}
    	
    	
    	sensorID = buff[0];
    	groupID = buff[1];
    	
    	System.arraycopy(buff, 2, tDeviceID, 0, 2);
    	deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();
    	
    	controlOP = buff[4];
    	OP = buff[5];
    	
    	if (controlOP == 0xFF && OP == 1) {
    		System.arraycopy(buff, pos, tMac, 0, 6);
        	Mac = byteToHex(tMac);
        	// TODO : MAC DB Ȯ�� & ID ��ȸ (MAC,sensorID, groupID, deviceID)
        	// if(��ϵ��� ���� ����̽���) 
        		device = new Device(sensorID, groupID, deviceID, Mac, connectionSocket);
        	// else if(��ϵ� ����̽���)?
    	}
    	else {
    		device = new Device(connectionSocket);
    	}
    	
    	return device;
    }
    
    //Device list ǥ��
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
        int defaultPort = 6789;

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
    
    
    // �������� ȣ���� �޾� message �����ϴ� �޼ҵ�
    public static void sendSignal(int id, String message) {
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
    
    
}
