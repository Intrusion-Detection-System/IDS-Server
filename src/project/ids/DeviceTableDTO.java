package project.ids;

import java.sql.Timestamp;

public class DeviceTableDTO {
	private byte sensorID;
	private byte groupID;
	private byte deviceID;
	private String location;
	private String action;
	private Timestamp measurementTime;
	
	public byte getSensorID() {
		return sensorID;
	}
	public void setSensorID(byte sensorID) {
		this.sensorID = sensorID;
	}
	public byte getGroupID() {
		return groupID;
	}
	public void setGroupID(byte groupID) {
		this.groupID = groupID;
	}
	public byte getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(byte deviceID) {
		this.deviceID = deviceID;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public Timestamp getMeasurementTime() {
		return measurementTime;
	}
	public void setMeasurementTime(Timestamp measurementTime) {
		this.measurementTime = measurementTime;
	}
}
