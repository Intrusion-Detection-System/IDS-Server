package project.ids;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DeviceHandler implements Runnable {
    private Device device;
    boolean isRunning = true;
    
    public DeviceHandler(Device device) {
        this.device = device;
    }

    @Override
    public void run() {
        try {
            handleMessage();
        } catch (IOException e) {

        }
    }

    public void handleMessage() throws IOException {
    	//To Read
        BufferedInputStream bis = new BufferedInputStream(this.device.socket.getInputStream());
        //InputStream inputStream = this.device.socket.getInputStream();
        //InputStreamReader reader = new InputStreamReader(inputStream);
        //BufferedReader buffer = new BufferedReader(reader);
        
        
        //To write
        OutputStream outputStream = this.device.socket.getOutputStream();
        //DataOutputStream backToClient = new DataOutputStream(outputStream);

        
        while (isRunning) {
        	readMessage(bis);
        }
    }

    private void readMessage(BufferedInputStream bis)
    {
    	int read=0, pos=0;
    	byte[] buff = new byte[1024];
    	byte[] tDeviceID = new byte[2];
    	
    	byte sensorID, groupID, controlOP, OP;
    	short deviceID;
    	
    	try {
			read = bis.read(buff, 0, 1024);
		} catch (IOException e) {
			System.out.println("Socket.read ERROR");
		}
    		
    	sensorID = buff[pos++];
    	groupID = buff[pos++];
    	
    	System.arraycopy(buff, pos, tDeviceID, 0, 2);
    	deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();
    	pos += 2;
    	
    	controlOP = buff[pos++];
    	OP = buff[pos++];
    	
    	if (controlOP == 0xFF && isRightDevice(sensorID, groupID, deviceID))
    	{
    		switch(OP)
    		{
    		case 0:
    			break;
    		case 1:
    			System.out.println("DEV_REQ");
    			break;
    		case 2:
    			System.out.println("DEV_ANSWER"); 
    			break;
    		case 3:
    			System.out.println("DATA"); 
    			recvData(buff, read, pos);
    			break;
    		case 4:
    			System.out.println("CONTROL"); break;
    		case 5:
    			System.out.println("MODE CAHNGE"); break;
    		case 9:
    			 break;
    		case 10:
    			System.out.println("RESET"); 
    			resetDevice();
    			break;
    		case 11:
    			System.out.println("ERROR"); break;
    		default :
    			System.out.println("예외처리");
    		}
    	}
    	else
    	{
    		System.out.println("Error");
    	}
    }

    private void recvData(byte[] buff, int read, int pos) //Data, DataLength, pos
    {
    	
    	switch (device.sensorID)
    	{
    	case 1:
    		recvDoorData(buff, read, pos); //sensorID(1) : 출입 탐지
    	case 2:
    		recvTempData(); //sensorID(2) : 다양한 탐지
    	default :
    		System.out.println("Wrong SensorID...");
    	}

    }
    
    public void recvDoorData(byte[] buff, int read, int pos)
    {
    	
    	
    	while( read>pos ) //DATA segment길이만큼 반복하여 DATA 추출
    	{
    		byte Header, Length;
    		
    		Header = buff[pos++];
    		Length = buff[pos++];
    		

    		if (Length == 0xFE)	//String DATA
    		{
    			String Data = "";
    			while(buff[pos] != '\n')
    				Data += buff[pos++];
    			
    			System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    		}
    		else	//Non-String Data
    		{
    			if(Length == 1)
    			{
    				byte Data = buff[pos++];
    				System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    			}
    			else if(Length == 2)
    			{
    				short Data; byte[] tData = new byte[2];
    				System.arraycopy(buff, pos, tData, 0, 2); pos += 2;
    		    	Data = ByteBuffer.wrap(tData).order(ByteOrder.LITTLE_ENDIAN).getShort();
    		    	System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    			}
    			else if(Length == 4)
    			{
    				int Data; byte[] tData = new byte[4];
    				System.arraycopy(buff, pos, tData, 0, 4); pos += 4;
    		    	Data = ByteBuffer.wrap(tData).order(ByteOrder.LITTLE_ENDIAN).getInt();
    		    	System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    			}
    			else
    			{
    				System.out.println("[RECV DATA] : wrong Length...]");
    			}

    		}
    	}
    	//TODO : 데이터 저장
    	
    	//TODO : 메시지 자동 전송 로직
    	//ArduinoCommunicationServer.broadcast(this.device.toString() + ": " + response);
        //ArduinoCommunicationServer.sendSignal(id, message);
    }
    
    public void recvTempData()
    {
    }
    
    private void resetDevice()
    {
    	try {
    		
    		isRunning = false;
			device.socket.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private boolean isRightDevice(byte sensorID, byte groupID, short deviceID)
    {
    	if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID)
    	{
    		return true;
    	}
    	else 
    		return false;
    }
}
