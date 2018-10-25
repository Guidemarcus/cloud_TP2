package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.io.Serializable;

public class ServeurDisponibleDTO implements Serializable {
	public static final String SERVER_LOAD_BALANCER = "server_load_balancer";
	public static final String SERVER_CALCUL = "server_calcul";
	private String hostName;
	private String type;
	private int capacity;
	
	public ServeurDisponibleDTO(String hostName, String type) {
		this.hostName = hostName;
		this.type = type;
		this.capacity = 0;
	}
	
	public String getHostName() {
		return this.hostName;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public int getCapacity() {
		return this.capacity;
	}
}
