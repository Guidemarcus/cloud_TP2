package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

import ca.polymtl.inf8480.tp2.shared.InstructionDTO;
import ca.polymtl.inf8480.tp2.shared.OperandInvalidException;
import ca.polymtl.inf8480.tp2.shared.CanPerformCalculationException;

public interface ServerCalculInterface extends Remote {
	int calculate(ArrayList<InstructionDTO> instructions) throws RemoteException, OperandInvalidException, CanPerformCalculationException;;
}
