package project.ids;
import java.net.Socket;
import java.util.UUID;

public class Device {
    public int id; //Sensor(1), Group(1), Device(2)
    public Socket socket;
    
    public byte sensorID, groupID, controlOP, OP;
	public short deviceID;
	public String Mac= "";
	public int actionTime = 1;
	
	public boolean auto = true;
    
	//tmp
    public Device( Socket socket) {
        
        this.socket = socket;
    }
	
	public Device(byte sensorID, Socket socket)
    {
    	this.sensorID = sensorID;
    	this.socket = socket;
    }
    
    public Device(int id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }
    
    public Device(byte sensorID, byte groupID, short deviceID, String Mac)
    {
    	this.sensorID = sensorID;
    	this.groupID = groupID; 
    	this.deviceID = deviceID;
    	this.Mac = Mac;
    }
    
    public Device(byte sensorID, byte groupID, short deviceID, String Mac, Socket socket)
    {
    	this.sensorID = sensorID;
    	this.groupID = groupID; 
    	this.deviceID = deviceID;
    	this.Mac = Mac;
    	this.socket = socket;
    }
    
    public void setSocket(Socket socket) {
    	this.socket = socket;
    }
    
    public void setAutoMode(boolean auto)
    {
    	this.auto = auto;
    }
    
    public void setId(byte sensorID, byte groupID, short deviceID) {
    	 this.id = Integer.parseInt(String.format("%d%02d%02d", sensorID, groupID, deviceID));
    }
    
    public String toString() {
        //return "[" + this.id + "](" + this.pos + ")";
    	return "[device]"+this.id+"";
    }
}
