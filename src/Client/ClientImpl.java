package Client;

import java.rmi.*;
import java.rmi.server.*;

// This object is for the client side that servers as an interface implementation
public class ClientImpl extends UnicastRemoteObject implements ClientInterface {

	public ClientImpl() throws RemoteException {
		super();
	}

	@Override
	public String connectionVerification(String message) {
		String returnMessage = "Clientside call back received with message: " + message;
		System.out.println(returnMessage);
		return returnMessage;
	}

}// end CallbackClientImpl class
