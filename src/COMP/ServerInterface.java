package COMP;

import java.rmi.*;

import Client.ClientInterface;

public interface ServerInterface extends Remote {

	// This method is for verifying the connection and server response
	public String connectionVerification() throws java.rmi.RemoteException;

	// This method is for adding a new course for advisors
	public String addCourse(String courseID, String semester) throws java.rmi.RemoteException;

	// This method is for removing a course for advisors
	public String removeCourse(String courseID, String semester) throws java.rmi.RemoteException;

	// This method is for listing course availability for advisors
	public String listCourseAvailability(String semester) throws java.rmi.RemoteException;

	// This method is for enrolling to a course for students
	public String enrolCourse(String studentID, String courseID, String semester) throws java.rmi.RemoteException;

	// This method is for getting a class schedule for students
	public String getClassSchedule(String studentID) throws java.rmi.RemoteException;

	// This method is for dropping a course for students
	public String dropCourse(String studentID, String courseID) throws java.rmi.RemoteException;

	// This remote method allows an object client to register for callback. @param
	// callbackClientObject is a reference to the object of the client; to be used
	// by the server to make its callbacks.
	public void registerForCallback(ClientInterface callbackClientObject) throws java.rmi.RemoteException;

	// This remote method allows an object client to cancel its registration for
	// callback
	public void unregisterForCallback(ClientInterface callbackClientObject) throws java.rmi.RemoteException;
}
