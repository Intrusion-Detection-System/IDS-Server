package project.ids;

import java.sql.Timestamp;

public class DeviceTableDTO {
	private int device_id;
	private String position;
	private String action;
	private Timestamp measurement_time;
	
	public int getDevice_id() {
		return device_id;
	}
	public void setDevice_id(int device_id) {
		this.device_id = device_id;
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
	public Timestamp getMeasurement_time() {
		return measurement_time;
	}
	public void setMeasurement_time(Timestamp measurement_time) {
		this.measurement_time = measurement_time;
	}	
}
