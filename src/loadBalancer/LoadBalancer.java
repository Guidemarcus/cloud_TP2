package loadBalancer;

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

import shared.LoadBalancerInterface;

public class LoadBalancer implements LoadBalancerInterface {
	
	public static void main(String[] args) {
		LoadBalancer loadBalancer = new LoadBalancer();
		loadBalancer.run();
	}

	public LoadBalancer() {
		super();
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			LoadBalancerInterface stub = (LoadBalancerInterface) UnicastRemoteObject
					.exportObject(this, 0);
			Registry registry = LocateRegistry.createRegistry("132.207.12.44", 5001);
			registry.bind("load_balancer", stub);
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	@Override
	public void execute() throws RemoteException {
		System.out.println("Hit inside the execute method");
	}
}

