package ca.polymtl.inf8480.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;

public class Client {
	public static void main(String[] args) throws Exception {
		String distantHostname = null;

		if (args.length == 0) {
			throw new Exception("Please provide the ip adress of the load balancer.");
		}
		
		distantHostname = args[0];

		Client client = new Client(distantHostname);
	}
	
	//private LoadBalancerInterface distantServerStub = null;
	private ServiceDeNomInterface distantServerStub = null;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		distantServerStub = loadServerStub(distantServerHostname);
	}

	/*private LoadBalancerInterface loadServerStub(String hostName) {
		LoadBalancerInterface stub = null;

		try {
			System.out.println("Client connecting to the server " + hostName);
			Registry registry = LocateRegistry.getRegistry(hostName, 5001);
			stub = (LoadBalancerInterface) registry.lookup("load_balancer");
			stub.execute();
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Access exception thrown");
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Remote exception throws");
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}*/
	
	private ServiceDeNomInterface loadServerStub(String hostName) {
		ServiceDeNomInterface stub = null;

		try {
			System.out.println("Client connecting to the server " + hostName);
			Registry registry = LocateRegistry.getRegistry(hostName, 5001);
			stub = (ServiceDeNomInterface) registry.lookup("service_de_nom");
			stub.execute();
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Access exception thrown");
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			e.printStackTrace();
			System.out.println("Remote exception throws");
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}
}
