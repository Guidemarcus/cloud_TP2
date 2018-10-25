package ca.polymtl.inf8480.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;

import ca.polymtl.inf8480.tp2.shared.ServeurDisponibleDTO;

public class Client {
	private LoadBalancerInterface loadBalancerStub = null;
	private ServiceDeNomInterface serviceDeNomStub = null;
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Please provide good arguments. See instructions.");
		}
		
		Client client = new Client(args[0], args[1]);
	}

	public Client(String serverDeNomHostName, String path) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		serviceDeNomStub = loadServiceDeNomStub(serverDeNomHostName);
		loadBalancerServerStub(path);
	}

	private LoadBalancerInterface loadBalancerServerStub(String path) {
		LoadBalancerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(this.serviceDeNomStub.getLoadBalancer().getHostName(), 5001);
			stub = (LoadBalancerInterface) registry.lookup("load_balancer");
			stub.execute(path);
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
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return stub;
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
}
