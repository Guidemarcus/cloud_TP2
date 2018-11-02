package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.io.Serializable;

public class ResponseDTO implements Serializable {
	private int response;
	private long time;
	
	public ResponseDTO(int response, long time) {
		this.response = response;
		this.time = time;
	}
	
	public int getResponse() {
		return this.response;
	}
	
	public long getTime() {
		return this.time;
	}
}
