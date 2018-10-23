package SOEN;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

	private static ServerImpl exportedObj = null;

	public static void main(String[] args) throws RemoteException {

		/*
		 * 
		 * SOEN - RMI server starts at 8080 - UDP server starts at 8081
		 * 
		 * INSE - RMI server starts at 9090 - UDP server starts at 9091
		 * 
		 * COMP - RMI server starts at 10010 - UDP server starts at 10011
		 */
		exportedObj = new ServerImpl();

		Runnable task = () -> {
			startRMI(8080);
		};

		Runnable task2 = () -> {
			try {
				startUDP(8081);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		};

		Thread thread = new Thread(task);
		Thread thread2 = new Thread(task2);

		thread.start();
		thread2.start();

	}

	/**
	 * UDP Server related configurations from this point onward.
	 * 
	 * @param port
	 */
	// This method starts the UDP service
	private static void startUDP(int portVar) throws RemoteException {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(portVar);
			byte[] buffer = new byte[1000];
			System.out.println("UDP Server ready at port " + portVar);

			while (true) {

				// The three lines of code below are responsible for blocking the UDP method
				// until a new client request is received.
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String requestData = request.getData() + "";

				System.out.println("startUDP: " + portVar + " is " + new String(request.getData()));

				System.out.println("Client sent " + new String(request.getData()));

				// I want to get the data again in another byte array so that I will actually
				// send the right String value after receiving it.
				byte[] bufferCopy = new byte[request.getLength()];

				// Copies an array from the specified source array, beginning at the specified
				// position, to the specified position of the destination array.
				System.arraycopy(request.getData(), request.getOffset(), bufferCopy, 0, request.getLength());

				// Creating a new string for the copy
				String bufferData = new String(bufferCopy);

				String stringMessage = "" + exportedObj.semesterCourses(bufferData);
				byte[] message = stringMessage.getBytes();

				DatagramPacket reply = new DatagramPacket(message, message.length, request.getAddress(),
						request.getPort());

				aSocket.send(reply);
			}
		} catch (SocketException e) {
			System.out.println("Socket error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO error: " + e.getMessage());
		}
		// We don't want to close the UDP connections
		finally {
			if (aSocket != null)
				aSocket.close();
		}

	}

	/**
	 * RMI Server related configurations from this point onward.
	 * 
	 * @param port
	 */

	// This method starts the RMI service
	private static void startRMI(int portVar) {
		// RMI server port
		int port = portVar;

		// RMI server hostname
		String hostName = "localhost";

		try {

			startRegistry(port);

			String registryURL = "rmi://" + hostName + ":" + port + "/callback";

			Naming.rebind(registryURL, exportedObj);

			System.out.println("RMI Server ready at port " + port);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	// This method starts the registry if it does not already exist
	private static void startRegistry(int port) throws RemoteException {
		try {
			Registry registry = LocateRegistry.getRegistry(port);
			registry.list();
			// This call will throw an exception
			// if the registry does not already exist
		} catch (RemoteException e) {
			// No valid registry at that port.
			Registry registry = LocateRegistry.createRegistry(port);
		}
	}

}
