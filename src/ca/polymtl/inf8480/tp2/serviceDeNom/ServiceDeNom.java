package ca.polymtl.inf8480.tp2.serviceDeNom;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;
import ca.polymtl.inf8480.tp2.shared.ServeurDisponibleDTO;

public class ServiceDeNom implements ServiceDeNomInterface {
	private ArrayList<ServeurDisponibleDTO> serveursDisponibles;
	private static final String login = "admin";
	private static final String password = "password";
	
	public static void main(String[] args) {
		ServiceDeNom loadBalancer = new ServiceDeNom();
		loadBalancer.run();
	}

	public ServiceDeNom() {
		super();
		this.serveursDisponibles = new ArrayList<ServeurDisponibleDTO>();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServiceDeNomInterface stub = (ServiceDeNomInterface) UnicastRemoteObject.exportObject(this, 5002);
			Registry registry = LocateRegistry.getRegistry(5001);
			registry.rebind("service_de_nom", stub);
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	/*
	 * Authenticate the load balancer with the name server.
	 * @param  String login
	 * @param  String password
	 * @param  String hostName
	 * @return boolean
	 */
	@Override
	public boolean authenticate(String login, String password, String hostName) throws RemoteException {
		if (ServiceDeNom.login.equals(login) && ServiceDeNom.password.equals(password)) {
			if (! this.alreadyExist(hostName)) {
				this.serveursDisponibles.add(new ServeurDisponibleDTO(hostName, ServeurDisponibleDTO.SERVER_LOAD_BALANCER));
			}
			return true;
		}
		
		return false;
	}
	
	/*
	 * Add a calcul server to the array of available servers
	 * @param 
	 */
	@Override
	public void addCalculServer(String hostName, int capacity) throws RemoteException {
		if (! this.alreadyExist(hostName)) {
			ServeurDisponibleDTO server = new ServeurDisponibleDTO(hostName, ServeurDisponibleDTO.SERVER_CALCUL);
			server.setCapacity(capacity);
			this.serveursDisponibles.add(server);
			
			System.out.println("The server the calcul was added");
		}
	}
	
	/*
	 * Check if a hostname already exist inside the servers array
	 * @param  String hostname
	 * @return boolean
	 */
	private boolean alreadyExist(String hostName) {
		for (ServeurDisponibleDTO server: this.serveursDisponibles) {
			if (server.getHostName().equals(hostName)) {
				return true;
			}
		}
		
		return false;
	}
	
	/*
	 * Get the available calcul servers
	 * @return ArrayList<ServeurDisponibleDTO>
	 */
	@Override
	public ArrayList<ServeurDisponibleDTO> getAvailableCalculServers() throws RemoteException {
		ArrayList<ServeurDisponibleDTO> tempServers = new ArrayList<ServeurDisponibleDTO>();
		for (ServeurDisponibleDTO server : this.serveursDisponibles) {
			if (server.getType().equals(ServeurDisponibleDTO.SERVER_CALCUL)) {
				tempServers.add(server);
			}
		}
		
		return tempServers;
	}
	
	/*
	 * Remove a server de calcul if he goes down like a little bitch.
	 * @param  String hostName
	 * @return void
	 */
	@Override
	public boolean removeCalculServer(String hostName) throws RemoteException {
		try {
			for (ServeurDisponibleDTO server : this.serveursDisponibles) {
				if (server.getHostName().equals(hostName)) {
					this.serveursDisponibles.remove(server);
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			// Prevent out of bound
			return false;
		}
	}
	
	/*
	 * Get the load balancer server object
	 * @return ServeurDisponibleDTO
	 */
	@Override
	public ServeurDisponibleDTO getLoadBalancer() throws RemoteException, Exception {
		for (ServeurDisponibleDTO server : this.serveursDisponibles) {
			if (server.getType().equals(ServeurDisponibleDTO.SERVER_LOAD_BALANCER)) {
				return server;
			}
		}
		
		throw new Exception("Server not found.");
	}
}

