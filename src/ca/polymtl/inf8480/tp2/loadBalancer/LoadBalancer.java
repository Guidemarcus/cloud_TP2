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
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.polymtl.inf8480.tp2.shared.LoadBalancerInterface;
import ca.polymtl.inf8480.tp2.shared.ServiceDeNomInterface;
import ca.polymtl.inf8480.tp2.shared.ServeurDisponibleDTO;
import ca.polymtl.inf8480.tp2.shared.InstructionDTO;
import ca.polymtl.inf8480.tp2.shared.CanPerformCalculationException;
import ca.polymtl.inf8480.tp2.shared.OperandInvalidException;
import ca.polymtl.inf8480.tp2.shared.ServerCalculInterface;

public class LoadBalancer implements LoadBalancerInterface {
	public static ArrayList<InstructionDTO> instructions = new ArrayList<InstructionDTO>();
	public static boolean hasInstructions = true;
	public static boolean securise = false;
	public static Lock calculationLock = new ReentrantLock(true);
	
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("Please provide the good arguments.");
		}
		
		LoadBalancer loadBalancer = new LoadBalancer();
		loadBalancer.run();
		loadBalancer.setServiceDeNomStub(loadBalancer.loadServiceDeNomStub(args[1]));
		loadBalancer.registerLoadBalancerToTheServiceDeNomServer(args[0]);
	}
	
	private static ServiceDeNomInterface serviceDeNomStub = null;
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
	
	@Override
	public void execute(String path, String mode) throws RemoteException {
		this.securise = mode.equals("1") ? true : false; 
		this.instructions.clear();
		this.hasInstructions = true;
		this.readFile(path);
		
		Thread stopThread = new Thread(new HandleStopOperation());
        stopThread.start();
		
		int n = 1; // Number of threads 
        //for (int i = 0; i < n; i++) { 
            Thread executeThread = new Thread(new MultithreadingExecute()); 
            executeThread.start();
        //}
	}
	
	/*
	 * Remplir la liste d'instructions
	 * @param  String path
	 * @return void
	 */
	private void readFile(String path) throws RemoteException {
		File file = new File(path);
		if (file.exists()) {
			try {			
				// Build the response
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
				String singleLine = "";
				while((singleLine = bufferedReader.readLine()) != null) {
					String[] array = singleLine.split(" ");
					InstructionDTO instruction = new InstructionDTO(array[0], Integer.parseInt(array[1]), this.securise, this.instructions.size());
					this.instructions.add(instruction);
				}
				bufferedReader.close();
			} catch (IOException e) {
				throw new RemoteException("Une erreur est survenue lors de la lecture du fichier.");
			}
		} else {
			throw new RemoteException("The file you provided does not exists.");
		}
	}
	
	/*
	 * Excecute the instructions send by the client
	 * @return void
	 */
	public static void executeInstructions(String name) {
		long start = System.currentTimeMillis();
		while (LoadBalancer.hasInstructions) {
			try {
				ArrayList<InstructionDTO> localInstructions = LoadBalancer.getInstructions(1, name);
				ArrayList<ServeurDisponibleDTO> serveursDeCalcul = LoadBalancer.serviceDeNomStub.getAvailableCalculServers();
				for (ServeurDisponibleDTO serveurDeCalcul : serveursDeCalcul) {
					try {
						Registry registry = LocateRegistry.getRegistry(serveurDeCalcul.getHostName(), 5001);
						ServerCalculInterface stub = (ServerCalculInterface) registry.lookup("server_calcul");
						int response = stub.calculate(localInstructions);
						System.out.println("Response : " + response);
						for (int i = 0; i < localInstructions.size(); i++) {
							localInstructions.get(i).setResponse(response);
							LoadBalancer.instructions.set(localInstructions.get(i).getIndex(), localInstructions.get(i));
						}
						break;
					} catch (CanPerformCalculationException e) {
						System.out.println("Hit inside the inner CanPerformCalculationException");
					} catch (OperandInvalidException e) {
						System.out.println("Hit inside the inner OperandInvalidException");
					} catch (RemoteException e) {
						System.out.println("Hit inside the inner RemoteException");
					} catch (NotBoundException e) {
						System.out.println("Hit inside the inner not bound exception");
					}
				}
			} catch (RemoteException e) {
				System.out.println("Hit inside the outer RemoteException");
			}
		}
		long finish = System.currentTimeMillis();
		long timeElapsed = finish - start;
		System.out.println("Time is : " + timeElapsed);
		int total = 0;
		for (InstructionDTO instruction : LoadBalancer.instructions) {
			total = (total + instruction.getResponse()) % 4000;
		}
		System.out.println("The response is :" + total);
	}
	
	/*
	 * Get the instructions to calculate for a single thread
	 * @param  int maxSizeOfInstructionsToCompute
	 * @return ArrayList<InstructionDTO> instructionsToCompute
	 */
	private static ArrayList<InstructionDTO> getInstructions(int maxSizeOfInstructionsToCompute, String name) {
		LoadBalancer.calculationLock.lock();
		ArrayList<InstructionDTO> instructionsToCompute = new ArrayList<InstructionDTO>();
		boolean arrayIsFull = false;
		for (int i = 0; i < LoadBalancer.instructions.size(); i++) {
			if (LoadBalancer.instructions.get(i).canBeCalculated()) {
				if (! arrayIsFull) {
					instructionsToCompute.add(LoadBalancer.instructions.get(i));
					LoadBalancer.instructions.get(i).setIsBeingCalculated(true);
					if (instructionsToCompute.size() >= maxSizeOfInstructionsToCompute) {
						arrayIsFull = true;
					}
				}
			}
		}
		LoadBalancer.calculationLock.unlock();
		return instructionsToCompute;
	}
}

class MultithreadingExecute extends Thread 
{ 
    public void run() {
		LoadBalancer.executeInstructions(Thread.currentThread().getName());
    } 
}

class HandleStopOperation extends Thread
{
	public void run() {
		while (LoadBalancer.hasInstructions) {
			int remaining = 0;
			for (int i = 0; i < LoadBalancer.instructions.size(); i++) {
				if (! LoadBalancer.instructions.get(i).isFinished()) {
					remaining += 1;
				}
			}
			if (remaining == 0) {
				LoadBalancer.hasInstructions = false;
			}
		}
	}
}

