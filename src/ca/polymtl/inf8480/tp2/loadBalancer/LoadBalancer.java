package ca.polymtl.inf8480.tp2.loadBalancer;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;

public class LoadBalancer implements LoadBalancerInterface {
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Please provide the good arguments.");
		}
		
		LoadBalancer loadBalancer = new LoadBalancer();
		loadBalancer.run();
		loadBalancer.setServiceDeNomStub(loadBalancer.loadServiceDeNomStub(args[1]));
		loadBalancer.registerLoadBalancerToTheServiceDeNomServer(args[0]);
	}
	
	private ServiceDeNomInterface serviceDeNomStub = null;
	private static final String LOGIN = "admin";
	private static final String PASSWORD = "password";

	public LoadBalancer() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject.exportObject(this, 5002);
			Registry registry = LocateRegistry.getRegistry(5001);
			registry.rebind("load_balancer", stub);
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	@Override
	public void execute(String path) throws RemoteException {
		System.out.println("Received the path : " + path);
		// TODO: Read the instructions and convert them to objects
		// TODO: Get the available servers de calcul & calculate 
	}
	
	/*
	 * Get the stub from the service de nom server
	 * @param  String hostName
	 * @return ServiceDeNomInterface
	 */
	private ServiceDeNomInterface loadServiceDeNomStub(String hostName) {
		ServiceDeNomInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostName, 5001);
			stub = (ServiceDeNomInterface) registry.lookup("service_de_nom");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Access exception thrown");
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Remote exception throws");
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}
	
	/*
	 * Set the service de nom stub
	 * @param  ServiceDeNomInterface stub
	 * @return void
	 */
	private void setServiceDeNomStub(ServiceDeNomInterface stub) {
		this.serviceDeNomStub = stub;
	}
	
	private void registerLoadBalancerToTheServiceDeNomServer(String hostName) {
		try {
			this.serviceDeNomStub.authenticate(LOGIN, PASSWORD, hostName);
		} catch (RemoteException e) {
			System.out.println("Remote exception thrown");
			System.out.println("Erreur: " + e.getMessage());
		}
	}
}

