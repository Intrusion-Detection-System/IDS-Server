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
    	
    	/* ID */
    	sensorID = buff[pos++];
    	groupID = buff[pos++];
    	
    	System.arraycopy(buff, pos, tDeviceID, 0, 2);  pos += 2;
    	deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();
    	
    	/* OP */
    	controlOP = buff[pos++];
    	OP = buff[pos++];
    	
    	/* DATA */
    	if ((int)(controlOP&0xFF) == 0xFF && isRightDevice(sensorID, groupID, deviceID))
    	{
    		switch(OP)
    		{
    		case 0:
    			break;
    			
    		case 1:	//OP_REQ
    			System.out.println("DEV_REQ");
    			break;
    			
    		case 2:	//OP_RESPONSE
    			System.out.println("DEV_ANSWER"); break;
    			
    		case 3:	//OP_DATA
    			System.out.println("DATA"); 
    			recvData(buff, read, pos);  
    			ArduinoCommunicationServer.sendSignal(sensorID, "hi");
    			break;
    		case 4:	//OP_CONTROL
    			System.out.println("CONTROL"); break;
    			
    		case 5:	//OP_MODE_CHANGE
    			System.out.println("MODE CAHNGE"); break;
    			
    		case 9:
    			 break;
    			 
    		case 10:	//OP_RESET
    			System.out.println("RESET");
    			
    			resetDevice();	break;
    			
    		case 11:	//OP_ERROR
    			System.out.println("ERROR"); break;
    			
    		default :
    			System.out.println("예외처리");
    		}
    	}
    	else if(! ((int)(controlOP&0xFF) == 0xFF))
    	{
    		System.out.println("Error - ControlOP is not Device");
    	}
    	else
    	{
    		System.out.println("Error - ID Segment Error");
    	}
    	
    	
    	
    	
    }

    private void recvData(byte[] buff, int read, int pos) //Data, DataLength, pos
    {
    	/* TODO : device 추가될 때 마다 이곳에 추가*/
    	switch (device.sensorID)
    	{
    	case 1: //sensorID(1) : 출입 탐지
    		recvDoorData(buff, read, pos); 
    		break;
    	case 2: //sensorID(2) : 온도 탐지 ex...
    		recvTempData(); 
    		break;
    	default :
    		System.out.println("Wrong SensorID....");
    		break;
    	}

    }
    
    // header 1 -> 문의 열림/닫힘
    // header 2 -> 문의 각도(센서값)
    
    public void recvDoorData(byte[] buff, int read, int pos)
    {
    	final int EOF = 0xFF, DATA_STRING = 0xFE;
    	
    	while( read>pos ) //DATA segment길이만큼 반복하여 DATA 추출
    	{
    		byte Header, Length;
    		
    		Header = buff[pos++];
    		Length = buff[pos++];
    		
    		if ((int)(Header&0xFF) == EOF)
    			break;
    		
    		if (((int)Length & 0xFF) == DATA_STRING)	//String DATA
    		{
    			String Data = "";
    			while(buff[pos] != '\n')
    				Data += buff[pos++];
    			
    			System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    		}
    		else	//Non-String Data
    		{
    			if(Length == 1) //byte
    			{
    				byte Data = buff[pos++];
    				System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    			}
    			else if(Length == 2) //short
    			{
    				short Data; byte[] tData = new byte[2];
    				System.arraycopy(buff, pos, tData, 0, 2); pos += 2;
    		    	Data = ByteBuffer.wrap(tData).order(ByteOrder.LITTLE_ENDIAN).getShort();
    		    	System.out.println("Data : [Header:"+Header+"], [Length:"+Length+"], [Data:"+Data+"]");
    			}
    			else if(Length == 4) //int
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
    	// 센서값 데이터베이스 저장
    	//TODO : 메시지 자동 전송 로직
    	//ArduinoCommunicationServer.broadcast(this.device.toString() + ": " + response);
        //ArduinoCommunicationServer.sendSignal(id, message);
    	// todo : 방범 모드 / 비방범 모드 웹 -> 서버 메시지 전송 할수 있게
    }
    
    public void recvTempData()	//sensorID(2) EXAMPLE...
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
