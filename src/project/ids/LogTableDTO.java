package project.ids;

import java.sql.Timestamp;

public class LogTableDTO {
	private Timestamp measurement_time;
	private String position;
	private String action;
	private int sensor_data;
	
	public Timestamp getMeasurement_time() {
		return measurement_time;
	}
	public void setMeasurement_time(Timestamp measurement_time) {
		this.measurement_time = measurement_time;
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
	public int getSensor_data() {
		return sensor_data;
	}
	public void setSensor_data(int sensor_data) {
		this.sensor_data = sensor_data;
	}
}
