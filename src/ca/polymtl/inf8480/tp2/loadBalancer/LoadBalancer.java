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
	private static ArrayList<InstructionDTO> instructions = new ArrayList<InstructionDTO>();
	private static boolean hasInstructions = true;
	private static boolean securise = false;
	private final Lock calculationLock = new ReentrantLock(true);
	
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
		this.executeInstructions();
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
	private void executeInstructions() {
		while (this.hasInstructions) {
			try {
				ArrayList<InstructionDTO> localInstructions = getInstructions(1);
				ArrayList<ServeurDisponibleDTO> serveursDeCalcul = this.serviceDeNomStub.getAvailableCalculServers();
				for (ServeurDisponibleDTO serveurDeCalcul : serveursDeCalcul) {
						Registry registry = LocateRegistry.getRegistry(serveurDeCalcul.getHostName(), 5001);
						ServerCalculInterface stub = (ServerCalculInterface) registry.lookup("server_calcul");
						int response = stub.calculate(localInstructions);
						if (localInstructions.size() > 0) {
							System.out.println("Sending to " + serveurDeCalcul.getHostName() + " to calculate " + localInstructions.get(0).getOperande() + " and response is : " + response);
						}
						for (int i = 0; i < localInstructions.size(); i++) {
							localInstructions.get(i).setResponse(response);
							this.instructions.set(localInstructions.get(i).getIndex(), localInstructions.get(i));
						}
						break;
				}
			} catch (CanPerformCalculationException e) {
			} catch (OperandInvalidException e) {
			} catch (RemoteException e) {
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int total = 0;
		for (InstructionDTO instruction : this.instructions) {
			total += instruction.getResponse();
			total %= 4000;
		}
		System.out.println("The response is :" + total);
	}
	
	/*
	 * Get the instructions to calculate for a single thread
	 * @param  int maxSizeOfInstructionsToCompute
	 * @return ArrayList<InstructionDTO> instructionsToCompute
	 */
	private ArrayList<InstructionDTO> getInstructions(int maxSizeOfInstructionsToCompute) {
		this.calculationLock.lock();
		ArrayList<InstructionDTO> instructionsToCompute = new ArrayList<InstructionDTO>();
		boolean arrayIsFull = false;
		int instructionsLeft = 0;
		for (int i = 0; i < this.instructions.size(); i++) {
			if (this.instructions.get(i).canBeCalculated()) {
				instructionsLeft += 1;
				if (! arrayIsFull) {
					instructionsToCompute.add(this.instructions.get(i));
					this.instructions.get(i).setIsBeingCalculated(true);
					if (instructionsToCompute.size() >= maxSizeOfInstructionsToCompute) {
						arrayIsFull = true;
					}
				}
			}
		}
		if (instructionsToCompute.size() == 0) {
			this.hasInstructions = false;
		}
		this.calculationLock.unlock();
		return instructionsToCompute;
	}
}

