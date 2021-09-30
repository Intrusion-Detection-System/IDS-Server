package project.ids;

import java.sql.Timestamp;

public class DeviceTableDTO {
	private int deviceID;
	private String position;
	private String action;
	private Timestamp measurementTime;
	
	public int getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(int deviceID) {
		this.deviceID = deviceID;
	}
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
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
