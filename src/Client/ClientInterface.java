package Client;

public interface ClientInterface extends java.rmi.Remote {
	public String connectionVerification(String message) throws java.rmi.RemoteException;

} 
