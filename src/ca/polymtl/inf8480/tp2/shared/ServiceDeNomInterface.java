package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;

import ca.polymtl.inf8480.tp2.shared.ServeurDisponibleDTO;

public interface ServiceDeNomInterface extends Remote {
	boolean authenticate(String login, String password, String hostName) throws RemoteException;
	ArrayList<ServeurDisponibleDTO> getAvailableCalculServers() throws RemoteException;
	ServeurDisponibleDTO getLoadBalancer() throws RemoteException, Exception;
	void addCalculServer(String hostName, int capacity) throws RemoteException;
	boolean removeCalculServer(String hostName) throws RemoteException;
}
