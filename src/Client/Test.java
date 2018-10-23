package Client;

import java.rmi.RemoteException;

/**
 * 
 * @author Kurosh: Instead of using JUnit or other testing framework, I thought
 *         that this type of testing would be faster. If the application would
 *         be larger, then it would be good to use JUnit.
 */
public class Test {
	
	public static void main(String args[]) {
		testMethods();
	}
	
	protected static void testMethods() {
	
		Client.callRMIService("SOENS1122", "SOEN", 1, false);
		Client.callRMIService("SOENS1122", "SOEN", 2, false);
		Client.callRMIService("SOENS1122", "SOEN", 3, false);
		Client.callRMIService("SOENS1122", "SOEN", 1, false);
		Client.callRMIService("SOENS1122", "SOEN", 2, false);
		Client.callRMIService("SOENS1122", "SOEN", 3, false);
		Client.callRMIService("SOENS3344", "SOEN", 1, false);
		Client.callRMIService("SOENS3344", "SOEN", 2, false);
		Client.callRMIService("SOENS3344", "SOEN", 3, false);

	}

}
