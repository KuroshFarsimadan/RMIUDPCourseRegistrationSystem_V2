package Test;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * 
 * @author Kurosh: Instead of using JUnit or other testing framework, I thought
 *         that this type of testing would be faster. If the application would
 *         be larger, then it would be good to use JUnit.
 */
public class Test {

	// So that we can only change one field if the server is different (different
	// identifier)
	private final static String departmentIdentifier = "SOEN";

	public static void main(String args[]) throws MalformedURLException, RemoteException, NotBoundException {
		testMethods();
	}

	protected static void testMethods() throws MalformedURLException, RemoteException, NotBoundException {

		// RMI server hostname
		String hostName = "127.0.0.1";

		// Registry URL ipORname:port/serviceName
		String compRegistryURL = "rmi://" + hostName + ":10010" + "/callback";
		String soenRegistryURL = "rmi://" + hostName + ":8080" + "/callback";
		String inseRegistryURL = "rmi://" + hostName + ":9090" + "/callback";

		SOEN.ServerInterface soen = null;
		COMP.ServerInterface comp = null;
		INSE.ServerInterface inse = null;

		soen = (SOEN.ServerInterface) Naming.lookup(soenRegistryURL);

		inse = (INSE.ServerInterface) Naming.lookup(inseRegistryURL);

		comp = (COMP.ServerInterface) Naming.lookup(compRegistryURL);
		
		System.out.println(inse.addCourse("INSE 6320", "WINTER"));
		System.out.println(inse.listCourseAvailability("WINTER"));
		

	}

}
