package project.ids;

import java.net.Socket;

public class UnregisteredDevice {
	private byte sensorID;
	private byte groupID;
	private short deviceID;
	private String mac;
	private Socket socket;
	
	public UnregisteredDevice() {
		
	}
	
	public UnregisteredDevice(String mac) {
		this.mac = mac;
	}
	
	public UnregisteredDevice(String mac, Socket connectionSocket) {
		this.mac = mac;
		this.socket = connectionSocket;
	}
	
	public byte getSensorID() {
		return sensorID;
	}
	public void setSensorID(byte sensorID) {
		this.sensorID = sensorID;
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public byte getGroupID() {
		return groupID;
	}
	public void setGroupID(byte groupID) {
		this.groupID = groupID;
	}
	public short getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(short deviceID) {
		this.deviceID = deviceID;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
}
