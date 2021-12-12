package project.ids;

import java.sql.Timestamp;

public class LogTableDTO {
	private Timestamp measurementTime;
	private String location;
	private String action;
	private int sensorData;
	
	public Timestamp getMeasurementTime() {
		return measurementTime;
	}
	public void setMeasurementTime(Timestamp measurementTime) {
		this.measurementTime = measurementTime;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public int getSensorData() {
		return sensorData;
	}
	public void setSensorData(int sensorData) {
		this.sensorData = sensorData;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
}
