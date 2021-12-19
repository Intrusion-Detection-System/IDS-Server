
package project.ids;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;

public class DeviceHandler implements Runnable {
	private Device device;

	public DeviceHandler(Device device) {
		this.device = device;
		System.out.println("sensor id " + device.sensorID);
		System.out.println("group id " + device.groupID);
		System.out.println("device id " + device.deviceID);
		System.out.println(isRightDevice(device.sensorID, device.groupID, device.deviceID));
	}

	@Override
	public void run() {
		System.out.println("New DeviceHandler");
		try {
			handleMessage();
		} catch (IOException e) {

		}
	}

	public void handleMessage() throws IOException {
		int read = 0, pos = 0;
		byte[] buff = new byte[1024];
		byte[] tDeviceID = new byte[2];

		byte sensorID, groupID, controlOP, OP;
		short deviceID;
		// To Read
		BufferedInputStream bis = new BufferedInputStream(this.device.socket.getInputStream());
		// To write
		OutputStream outputStream = this.device.socket.getOutputStream();
		// DataOutputStream backToClient = new DataOutputStream(outputStream);

		while (device.isRunning) {
			// readMessage(bis);

			try {
				read = bis.read(buff, 0, 1024);
			} catch (IOException e) {
				//System.out.println("Socket.read ERROR");
				//e.printStackTrace();
				continue;
			}
			
			if(read == -1)
			{
				this.device.socket.close();
				System.out.println("Error: " + read);
				device.isRunning=false;
				break;
			}
			
			System.out.print("Message [ ");
			while (read >= pos) {
				System.out.print(buff[pos++] + " ");
			}
			System.out.println("]");
			pos = 0;

			/* ID */
			sensorID = buff[pos++];
			groupID = buff[pos++];

			System.arraycopy(buff, pos, tDeviceID, 0, 2);
			pos += 2;
			deviceID = ByteBuffer.wrap(tDeviceID).order(ByteOrder.LITTLE_ENDIAN).getShort();

			/* OP */
			controlOP = buff[pos++];
			OP = buff[pos++];

			/* DATA */
			if ((int) (controlOP & 0xFF) == 0xFF && isRightDevice(sensorID, groupID, deviceID)) {
				switch (OP) {
				case 0:
					break;

				case 1: // OP_REQ
					System.out.println("DEV_REQ");
					break;

				case 2: // OP_RESPONSE
					System.out.println("DEV_ANSWER");
					break;

				case 3: // OP_DATA
					System.out.println("DATA");
					recvData(buff, read, pos);
					// ArduinoCommunicationServer.sendSignal(sensorID, "hi");
					break;
				case 4: // OP_CONTROL
					System.out.println("CONTROL");
					break;

				case 5: // OP_MODE_CHANGE
					System.out.println("MODE CHANGE");
					break;

				case 9:
					break;

				case 10: // OP_RESET
					System.out.println("RESET");

					resetDevice();
					break;

				case 11: // OP_ERROR
					System.out.println("ERROR");
					break;

				default:
					System.out.println("예외처리");
				}
			} else if (!((int) (controlOP & 0xFF) == 0xFF)) {
				System.out.println("Error - ControlOP is not Device");
				System.out.println("[" + sensorID + "-" + groupID + "-" + deviceID + "-" + controlOP + "-" + OP + "]");
			} else {
				System.out.println("Error - ID Segment Error2");
			}

		}
		System.out.println("[Thread close]"+ device.deviceID);
	}

	

	private void recvData(byte[] buff, int read, int pos) // Data, DataLength, pos
	{
		/* TODO : device 추가될 때 마다 이곳에 추가 */
		switch (device.sensorID) {
		case 1: // sensorID(1) : 출입 탐지
			recvDoorData(buff, read, pos);
			break;
		case 2: // sensorID(2) : 온도 탐지 ex...
			recvTempData();
			break;
		default:
			System.out.println("Wrong SensorID....");
			break;
		}

	}

	// -->주형
	public void recvDoorData(byte[] buff, int read, int pos) {
		boolean isErrorMessage = false;
		final int DATA_STRING = 0xFE;
		byte state = 0, isActed = 0;
		int angle = 0;

		while (read >= pos && isErrorMessage == false) // DATA segment길이만큼 반복하여 DATA 추출
		{
			System.out.println("M=" + read + " " + pos);
			for (int i = pos; i < read; i++) {
				System.out.print(buff[i]);
				System.out.print(" ");
			}
			byte Header, Length;

			Header = buff[pos++];

			if ((int) (Header & 0xFF) == 0xFF) {
				System.out.println("EOF");
				break;
			}

			Length = buff[pos++];

			switch (Header) {

			case 1:
				System.out.println("DOOR STATE");
				if (Length == 1) {
					state = buff[pos++];
					System.out.println("Data : [Header:" + Header + "], [Length:" + Length + "], [Data:" + state + "]");
				} else {
					System.out.println("ERROR - Wrong Length");
					isErrorMessage = true;
				}
				break;
			case 2:
				System.out.println("DOOR ANGLE");
				if (Length == 4) {
					byte[] tData = new byte[4];
					System.arraycopy(buff, pos, tData, 0, 4);
					pos += 4;
					angle = ByteBuffer.wrap(tData).order(ByteOrder.LITTLE_ENDIAN).getInt();
					System.out.println("Data : [Header:" + Header + "], [Length:" + Length + "], [Data:" + angle + "]");
				} else {
					System.out.println("ERROR - Wrong Length");
					isErrorMessage = true;
				}
				break;
			case 3:
				System.out.println("IS SECURED");
				if (Length == 1) {
					isActed = buff[pos++];
					System.out.println("Data : [Header:" + Header + "], [Length:" + Length + "], [Data:" + isActed + "]");
				} else {
					System.out.println("ERROR - Wrong Length");
					isErrorMessage = true;
				}
				break;
			case 4:
				break;
			case -127:
				System.out.println("EOF2");

			}

		}
		if (isErrorMessage == false) {
			// TODO : 데이터 저장 - (boolean)state, (int)angle
			DatabaseConnection dbConnection = new DatabaseConnection();
			try {
				if (state == 1) {
					dbConnection.insertLogTable(device.sensorID, device.groupID, device.deviceID, "열림", angle);
				} else if (state == 0) {
					dbConnection.insertLogTable(device.sensorID, device.groupID, device.deviceID, "닫힘", angle);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			//TODO : Q1_방범모드와 비방범모드 정보는 DB에 저장하지 않는가?
			
			if (device.auto == true && state == 1)// TODO : 메시지 자동 전송 로직 (auto, opened)
			{
				ArduinoCommunicationServer.addCount();
			}
			
		}
	}

	public void recvTempData() // sensorID(2) EXAMPLE...
	{
	}

	private void resetDevice() {
		try {
			device.isRunning = false;
			device.socket.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isRightDevice(byte sensorID, byte groupID, short deviceID) {
		if (device.sensorID == sensorID && device.groupID == groupID && device.deviceID == deviceID) {
			return true;
		} else
			return false;
	}

	/*
	 * public void sendSignal(byte sensorID, byte groupID, short deviceID, byte
	 * opcode, byte state) { System.out.println("3"); try { System.out.println("4");
	 * OutputStream os= device.socket.getOutputStream(); byte buff[] = new
	 * byte[1024]; buff[0] = device.sensorID; buff[1] = device.groupID; //<--Group
	 * ID buff[2] = 0; //<--Device ID(LOW) buff[3] = 0; //<--Device ID(HIGH)
	 * 
	 * buff[4]=(byte)0; //controlOP buff[5]=opcode; //controlOP
	 * 
	 * buff[6]=(byte)1; //head buff[7]=(byte)1; //length buff[8]=state; //data
	 * buff[9]= (byte)(255 &(byte) 0xFF); //EOF
	 * 
	 * os.write(buff); //FOR TEST... os.flush();
	 * 
	 * } catch (IOException e) {
	 * 
	 * } }
	 */
}
