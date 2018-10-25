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
	
	public InstructionDTO(String operation, int operande) {
		this.operation = operation;
		this.operande = operande;
	}
	
	public String getOperation() {
		return this.operation;
	}
	
	public int getOperande() {
		return this.operande;
	}
}
