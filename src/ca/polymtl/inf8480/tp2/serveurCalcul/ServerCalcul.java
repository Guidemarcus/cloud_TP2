package ca.polymtl.inf8480.tp2.serverCalcul;

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
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.polymtl.inf8480.tp2.shared.ServerCalculInterface;
import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;
import ca.polymtl.inf8480.tp2.shared.InstructionDTO;
import ca.polymtl.inf8480.tp2.shared.Operations;
import ca.polymtl.inf8480.tp2.shared.OperandInvalidException;
import ca.polymtl.inf8480.tp2.shared.CanPerformCalculationException;

public class ServerCalcul implements ServerCalculInterface {
	private static int nInstructions = 0;
	private final Lock canPerformlock = new ReentrantLock(true);
	private final Lock calculationLock = new ReentrantLock(true);
	
	public static void main(String[] args) throws Exception {
		if (args.length != 4) {
			throw new Exception("Make sure to provide the capacity and the maliciousness.");
		}
		
		ServerCalcul serverCalcul = new ServerCalcul(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]));
		serverCalcul.run();
		serverCalcul.addCalculServerToServiceDeNomServer();
	}
	
	private String hostName;
	private int capacity;
	private String serviceDeNomHostName;
	private ServiceDeNomInterface serviceDeNomStub;
	private int maliciousness;

	public ServerCalcul(String hostName, int capacity, String serviceDeNomHostName, int maliciousness) {
		super();
		this.hostName = hostName;
		this.capacity = capacity;
		this.serviceDeNomHostName = serviceDeNomHostName;
		this.serviceDeNomStub = null;
		this.maliciousness = maliciousness;
	}

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerCalculInterface stub = (ServerCalculInterface) UnicastRemoteObject.exportObject(this, 5002);
			Registry registry = LocateRegistry.getRegistry(5001);
			registry.rebind("server_calcul", stub);
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}
	
	/*
	 * Register this server to the serviceDeNom server
	 * @return void
	 */
	private void addCalculServerToServiceDeNomServer() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
		
		try {
			Registry registry = LocateRegistry.getRegistry(this.serviceDeNomHostName, 5001);
			this.serviceDeNomStub = (ServiceDeNomInterface) registry.lookup("service_de_nom");
			this.serviceDeNomStub.addCalculServer(this.hostName, this.capacity);
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage() + "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Access exception thrown");
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Remote exception throws");
			System.out.println("Erreur: " + e.getMessage());
		}
	}
	
	/*
	 * Calculate the incoming instructions
	 * @param  ArrayList<InstructionDTO> instructions
	 * @return int response
	 */
	 @Override
	public int calculate(ArrayList<InstructionDTO> instructions) throws RemoteException, OperandInvalidException, CanPerformCalculationException {
		this.canPerformlock.lock();
		try {
			if (!this.canPerformCalculation(this.calculateT(this.nInstructions + instructions.size()))) {
				throw new CanPerformCalculationException("Cannot perform this calculation");
			}
			this.nInstructions += instructions.size();
		} catch(CanPerformCalculationException e) {
			throw e;
		}
		this.canPerformlock.unlock();
		
		this.calculationLock.lock();
		int response = 0;
		for(InstructionDTO instruction : instructions) {
			if (instruction.getOperation().equals(InstructionDTO.OPERATION_PELL)) {
				response = (response + Operations.pell(instruction.getOperande())) % 4000;
			} else if (instruction.getOperation().equals(InstructionDTO.OPERATION_PRIME)) {
				response = (response + Operations.prime(instruction.getOperande())) % 4000;
			} else {
				throw new OperandInvalidException("Invalid operation");
			}
			response = transformResponseWithMaliciousness(response);
		}
		
		this.nInstructions -= instructions.size();
		this.calculationLock.unlock();
		
		return response;
	}
	
	/*
	 * Transform a given response depending on the maliciousness of the calcul server
	 * @param  int oldResponse
	 * @return int newResponse
	 */
	private int transformResponseWithMaliciousness(int oldResponse) {
		// Number between 0 and 100
		Random random = new Random();
		int randomNumber = random.nextInt(100 + 1);
		if (randomNumber <= this.maliciousness) {
			return random.nextInt(100000); // Return a wrong answer
		}
		
		return oldResponse;
	}
	
	/*
	 * Calculate the value of T
	 * @param  int lengthOfInstructions
	 * @return int
	 */
	private int calculateT(int lengthOfInstructions) {
		System.out.println("We have " + lengthOfInstructions + " instructions right now");
		int numerateur = lengthOfInstructions - this.capacity;
		int denominateur = 4 * this.capacity;
		return (int) ((numerateur / denominateur)) * 100;
	}
	
	/*
	 * Check if the serveur de calcul will accept the request
	 * @param  int tauxDeRefus
	 * @return boolean
	 */
	private boolean canPerformCalculation(int tauxDeRefus) {
		if (tauxDeRefus <= 0) {
			return true;
		}
		
		if (tauxDeRefus >= 100) {
			return false;
		}
		
		// Number between 0 and 100
		Random random = new Random();
		int randomNumber = random.nextInt(100 + 1);
		if (tauxDeRefus <= randomNumber) {
			return false;
		}
		
		return true;
	}
}

