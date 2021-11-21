package project.ids;

public class UnregisteredDevice {
	private byte sensorID;
	private String mac;
	
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

}
