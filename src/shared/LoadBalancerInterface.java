package shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LoadBalancerInterface extends Remote {
	void execute() throws RemoteException;
}
