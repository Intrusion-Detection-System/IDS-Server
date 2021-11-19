package project.ids;
import java.net.Socket;
import java.util.UUID;

public class Device {
    public int id; //Sensor(1), Group(1), Device(2)
    public Socket socket;
    
    byte sensorID, groupID, controlOP, OP;
	short deviceID;
	String Mac= "";
    
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
    
    
    
    public Device(byte sensorID, byte groupID, short deviceID, String Mac, Socket socket)
    {
    	this.sensorID = sensorID;
    	this.groupID = groupID;
    	this.deviceID = deviceID;
    	this.Mac = Mac;
    	this.socket = socket;
    }

    public String toString() {
        //return "[" + this.id + "](" + this.pos + ")";
    	return "[device]"+this.id+"";
    }
}
