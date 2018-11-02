package ca.polymtl.inf8480.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ca.polymtl.inf8480.tp2.shared.ResponseDTO;

public interface LoadBalancerInterface extends Remote {
	ResponseDTO execute(String path, String mode) throws RemoteException;
}
