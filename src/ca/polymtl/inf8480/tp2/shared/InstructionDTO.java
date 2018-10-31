package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.io.Serializable;

public class InstructionDTO implements Serializable {
	public static final String OPERATION_PELL = "pell";
	public static final String OPERATION_PRIME = "prime";
	
	private String operation;
	private int operande;
	private boolean isConfirmed;
	private boolean isBeingCalculated;
	private int response;
	private boolean hasResponse;
	private int index;
	
	public InstructionDTO(String operation, int operande, boolean isConfirmed, int index) {
		this.operation = operation;
		this.operande = operande;
		this.isBeingCalculated = false;
		this.isConfirmed = isConfirmed;
		this.response = 0;
		this.hasResponse = false;
		this.index = index;
	}
	
	public String getOperation() {
		return this.operation;
	}
	
	public int getOperande() {
		return this.operande;
	}
	
	public boolean getIsConfirmed() {
		return this.isConfirmed;
	}
	
	public void setIsBeingCalculated(boolean isBeingCalculated) {
		this.isBeingCalculated = isBeingCalculated;
	}
	
	public boolean getIsBeingCalculated() {
		return this.isBeingCalculated;
	}
	
	public void setResponse(int response) {
		if (this.response == response) {
			System.out.println("Is confirmed");
			this.isConfirmed = true;
		}
		this.response = response;
		this.isBeingCalculated = false;
		this.hasResponse = true;
	}
	
	public int getResponse() {
		return this.response;
	}
	
	public int getIndex() {
		return this.index;
	}
	
	public boolean canBeCalculated() {
		// Si j'ai pas de reponse et qu'un autre process n'est pas en train de la calculer, on doit la calculer
		if (! this.hasResponse && ! this.isBeingCalculated) {
			return true;
		}
		
		// Si j'ai une reponse, mais elle n'est pas confirme et qu'elle n'est pas en train de se faire calculer
		if (this.hasResponse && ! this.isBeingCalculated && ! this.isConfirmed) {
			return true;
		}
		
		return false;
	}
}
