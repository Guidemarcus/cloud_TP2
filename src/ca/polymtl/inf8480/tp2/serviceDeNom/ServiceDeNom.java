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
import java.io.FileReader;
import java.io.FileWriter;

import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;

public class ServiceDeNom implements ServiceDeNomInterface {
	
	public static void main(String[] args) {
		ServiceDeNom loadBalancer = new ServiceDeNom();
		loadBalancer.run();
	}

	public ServiceDeNom() {
		super();
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
	
	@Override
	public void execute() throws RemoteException {
		System.out.println("Hit inside the execute method of the service de nom");
	}
}

