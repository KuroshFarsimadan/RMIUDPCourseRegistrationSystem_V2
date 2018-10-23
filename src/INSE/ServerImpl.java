package INSE;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import Client.ClientInterface;

import java.util.Vector;

public class ServerImpl extends UnicastRemoteObject implements ServerInterface {

	private Vector clientList;

	private HashMap<String, HashMap> courseRecords = new HashMap<String, HashMap>();

	// So that we can only change one field if the server is different (different
	// identifier)
	private final String departmentIdentifier = "INSE";

	// Logger service for logging the server requests
	private LoggerService s = new LoggerService();

	public ServerImpl() throws RemoteException {
		super();
		clientList = new Vector();
		initDatabase();
	}

	@Override
	public String connectionVerification() throws java.rmi.RemoteException {
		return ("Handshake sucessful");
	}

	/*****************************************
	 * Methods for advisors
	 *****************************************/
	@Override
	public synchronized String addCourse(String courseID, String semester) throws RemoteException {
		String message = null;
		String requestState = "Failed";

		try {

			String semLower = semester.toLowerCase();
			// Check that the course we want to add, is not already in the database
			if (courseExistsInSemester(courseID, semester) == false) {

				if (semLower.equalsIgnoreCase("fall") || semLower.equalsIgnoreCase("summer")
						|| semLower.equalsIgnoreCase("winter")) {

					HashMap<String, HashMap> course = null;

					try {
						// Each term can have ..* courses, but the semester might not exist
						course = courseRecords.get(semester);
					} catch (Exception e) {
						course = new HashMap<String, HashMap>();
						courseRecords.put("FALL", new HashMap<String, HashMap>());
					}

					HashMap<String, HashMap<String, String>> courseInformation = new HashMap<String, HashMap<String, String>>();

					courseInformation.put("Capacity", new HashMap<String, String>());
					courseInformation.get("Capacity").put("TotalCapacity", "1");
					courseInformation.get("Capacity").put("Registered", "0");

					// Each course can have ..* course information fields
					courseInformation.put("Information", new HashMap<String, String>());

					// Each course can have 0..* students
					courseInformation.put("RegisteredStudents", new HashMap<String, String>());

					course.put(courseID, courseInformation);

					courseRecords.put(semester, course);

					requestState = "Successful";
					message = "A new course " + courseID + " has been added for " + semester + " semester";
				} else {
					message = "Unknown semester " + semester
							+ " was given. Approved semesters are Fall, Winter, and Summer";
				}
			} else {
				message = "A course " + courseID + " already exists for " + semester + " semester";
			}

		} catch (Exception e) {
			message = "An unknown error occurred: " + e;
		}

		// message =
		s.logger("Add a course (advisor)",
				"semester = " + semester + ", courseID = " + courseID + ", semester = " + semester, requestState,
				message);

		return message;
	}

	@Override
	public synchronized String removeCourse(String courseID, String semester) throws RemoteException {
		String message = null;
		String requestState = "Failed";

		try {
			String semLower = semester.toLowerCase();

			// Check that the course we want to add, is not already in the database
			if (courseExistsInSemester(courseID, semester) == true) {
				if (semLower.equalsIgnoreCase("fall") || semLower.equalsIgnoreCase("summer")
						|| semLower.equalsIgnoreCase("winter")) {

					// Each term can have ..* courses
					HashMap<String, HashMap> course = courseRecords.get(semester);

					course.remove(courseID);

					courseRecords.put(semester, course);

					requestState = "Successful";
					message = "A course " + courseID + " has been deleted for " + semester
							+ " semester and the students have been dropped from the course";
				} else {
					message = "Unknown semester " + semester
							+ " was given. Approved semesters are Fall, Winter, and Summer";
				}
			} else {
				message = "A course " + courseID + " does not exists for " + semester + " semester";
			}

		} catch (Exception e) {
			message = "An unknown error occurred: " + e;
		}

		// message =
		s.logger("Delete a course (advisor)",
				"semester = " + semester + ", courseID = " + courseID + ", semester = " + semester, requestState,
				message);

		return message;
	}

	@Override
	public synchronized String listCourseAvailability(String semester) throws RemoteException {
		String message = "";
		String requestState = "Failed";

		/*
		 * 
		 * SOEN - RMI server starts at 8080 - UDP server starts at 8081
		 * 
		 * INSE - RMI server starts at 9090 - UDP server starts at 9091
		 * 
		 * COMP - RMI server starts at 10010 - UDP server starts at 10011
		 */
		try {
			message += sendUDPMessage(10011, semester);
			message += sendUDPMessage(8081, semester);
			message += semesterCourses(semester); // courseRecords.get(semester).toString();
		} catch (Exception e) {

		}

		if (!message.contains("nullnull") && !message.contains("nullnullnull")) {
			requestState = "Successful";
			s.logger("List course availability (advisor)", "semester = " + semester, requestState, message);
		} else {
			requestState = "Failed";
			message = "No available UDP services for course retrieval";
			s.logger("List course availability (advisor)", "semester = " + semester, requestState, message);
		}

		return message;
	}

	protected synchronized String sendUDPMessage(int serverPort, String semester) {
		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket(null);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}

		String courses = "";
		try {

			aSocket.setSoTimeout(10000);

			courses = "No courses for semester available";

			byte[] message = semester.getBytes();

			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(message, semester.length(), aHost, serverPort);

			aSocket.send(request);

			byte[] buffer = new byte[1000];

			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);

			// I want to get the data again in another byte array so that I will actually
			// send
			// the right String value after receiving it.
			byte[] bufferCopy = new byte[reply.getLength()];

			// Copies an array from the specified source array, beginning at the specified
			// position, to the specified position of the destination array.
			System.arraycopy(reply.getData(), reply.getOffset(), bufferCopy, 0, reply.getLength());

			// Creating a new string for the copy
			String bufferData = new String(bufferCopy);

			courses = bufferData;

		} catch (SocketTimeoutException e) {
			courses = "Timeout occurred for port " + serverPort;
		} catch (SocketException e) {
			return null;
			// We don't want to return a detailed exception
			// System.out.println("Socket error: " + e.getMessage());
			// return "Socket error: " + e.getMessage();
		} catch (IOException e) {
			return null;
			// System.out.println("IO error: " + e.getMessage());
			// return "IO error: " + e.getMessage();
		} finally {
			if (aSocket != null)
				aSocket.close();
		}

		return courses;
	}

	/*****************************************
	 * Methods for students
	 *****************************************/

	// We assume that the client sends the request to the right server i.e. the
	// client sends a request for the course department, but we will still check
	// whether or not the student really belongs to this servers department.
	@Override
	public synchronized String enrolCourse(String studentID, String courseID, String semester) throws RemoteException {
		String message = null;
		String requestState = "Failed";

		try {

			// Check if the semester matches any known semester in the database
			if (courseRecords.get(semester) != null) {

				// Check if the semester course matches any semester course in the database
				if (courseRecords.get(semester).get(courseID) != null) {

					// Check if the course has any room by iterating through the database
					HashMap<String, HashMap<String, String>> courseInformation = (HashMap<String, HashMap<String, String>>) courseRecords
							.get(semester).get(courseID);

					int totalCapacity = Integer.parseInt(courseInformation.get("Capacity").get("TotalCapacity"));
					int registered = Integer.parseInt(courseInformation.get("Capacity").get("Registered"));

					// If the course still has some room.
					if (registered < totalCapacity) {

						// We need to make sure that the student has taken max 3 courses per semester if
						// part of the department and max 2 courses from this department if the student
						// is from another department. There is a small problem though. This method does
						// not check whether or not the student has been already registered for this
						// course, but it is not a problem since it will overwrite the hashmap value.
						// It would be better of course if we would return a proper error message.
						if (studentSemesterCourses(studentID, semester) == true) {

							if (courseInformation.get("RegisteredStudents").get(studentID) == null) {
								registered += 1;

								// Each term can have ..* courses
								HashMap<String, HashMap> course = new HashMap<String, HashMap>();

								// Each course can have 0..* students
								HashMap<String, String> students = courseInformation.get("RegisteredStudents");
								students.put(studentID, "NoName");

								courseInformation.get("Capacity").put("Registered", registered + "");

								courseInformation.put("RegisteredStudents", new HashMap<String, String>());
								courseInformation.put("RegisteredStudents", students);

								course.put(courseID, courseInformation);

								courseRecords.get(semester).replace(courseID, course.get(courseID));

								requestState = "Successful";
								message = "The student " + studentID + " is registered for the course " + courseID;
							} else {
								message = "The student " + studentID + " is already registered for the course "
										+ courseID;
							}

						} else {
							message = "The student cannot register for more courses in the given semester";
						}

					} else {
						message = "The course " + courseID + " has no room left";
					}

				} else {
					message = "Course  " + courseID + "  not found in the database";
				}
			} else {
				message = "Semester not found in the database";
			}

		} catch (Exception e) {
			message = "An unknown error occurred: " + e;

		}

		s.logger("Enrol to a course (student)",
				"studentID = " + studentID + ", courseID = " + courseID + ", semester = " + semester, requestState,
				message);
		return message;
	}

	@Override
	public synchronized String getClassSchedule(String studentID) throws RemoteException {
		String message = "Student courses = ";
		String requestState = "Successful";

		try {
			// Looping through all of the semesters
			for (Map.Entry me : courseRecords.entrySet()) {

				HashMap<String, HashMap> semesterRow = courseRecords.get(me.getKey());

				for (Entry<String, HashMap> me2 : semesterRow.entrySet()) {

					HashMap<String, String> courseStudentsRow = (HashMap<String, String>) semesterRow.get(me2.getKey())
							.get("RegisteredStudents");
					if (courseStudentsRow.get(studentID) != null) {
						message += "semester is " + me.getKey() + " and course is " + me2.getKey() + ", ";
					}

				}

			}
		} catch (Exception e) {
			requestState = "Failed";
			message = "An unknown error occurred: " + e;
		}

		if (message.equalsIgnoreCase("Student courses = ")) {
			message += "Student has not enrolled to any course in any semester.";
		}

		s.logger("Get class schedule (student)", "studentID = " + studentID, requestState, message);

		return message;
	}

	@Override
	public synchronized String dropCourse(String studentID, String courseID) throws RemoteException {
		String message = null;
		String requestState = "Failed";
		try {
			// Looping through all of the semesters
			for (Map.Entry me : courseRecords.entrySet()) {
				// Looping through course records
				HashMap<String, HashMap> semesterRow = courseRecords.get(me.getKey());

				// We need to loop each semester, because a course can exist in multiple
				// semesters. The given assignment did not specify the semester id for a method
				// so this is necessary.
				for (Entry<String, HashMap> me2 : semesterRow.entrySet()) {

					HashMap<String, String> courseStudentsRow = (HashMap<String, String>) semesterRow.get(me2.getKey())
							.get("RegisteredStudents");

					// If the studentID exists for the course.
					try {

						if (courseStudentsRow.get(studentID) != null
								&& me2.getKey().toString().equalsIgnoreCase(courseID)) {
							courseStudentsRow.remove(studentID);
							semesterRow.get(me2.getKey()).put("RegisteredStudents", courseStudentsRow);
							courseRecords.put((String) me.getKey(), semesterRow); // semesterRow.get(me2.getKey())
							message = "The student with ID " + studentID + " has been dropped from the course "
									+ courseID;
							requestState = "Successful";
						} /*
							 * else { message = "The student with ID " + studentID +
							 * " did not have a course " + courseID + " that could be dropped."; }
							 */
					} catch (NullPointerException e) {
						System.out.println("Exception");
						message = "The student with ID " + studentID + " did not have a course " + courseID
								+ " that could be dropped.";
					}
				}
			}
		} catch (Exception e) {
			requestState = "Failed";
			message = "An unknown error occurred: " + e;
		}

		if (message == null) {
			message = "The student with ID " + studentID + " did not have a course " + courseID
					+ " that could be dropped.";
		}

		s.logger("Drop a course (student)", "studentID = " + studentID + ", courseID = " + courseID, requestState,
				message);

		return message;
	}

	// This method checks if the student belongs to the department
	protected synchronized boolean studentBelongsToDepartment(String studentID) {
		boolean belongsToDepartment = false;
		// Check if the student belongs to this department
		if (studentID.toLowerCase().contains(departmentIdentifier.toLowerCase())) {
			belongsToDepartment = true;
			// System.out.println("Student " + studentID + " is part of the department");
		} else {
			belongsToDepartment = false;
			// message = "Student " + studentID + " is NOT part of the department";
		}
		return belongsToDepartment;
	}

	// This method returns boolean. We need to make sure that the student has taken
	// max 3 courses per semester if part of the department and max 2 courses from
	// this department if the student is from another department
	protected synchronized boolean studentSemesterCourses(String studentID, String semester) {
		int counter = 0;

		// Each term can have ..* courses
		HashMap<String, HashMap> course = courseRecords.get(semester);

		// Looping through course
		for (Map.Entry me : course.entrySet()) {

			HashMap<String, HashMap> student = (HashMap<String, HashMap>) course.get(me.getKey())
					.get("RegisteredStudents");

			// Student has taken the course
			if (student.get(studentID) != null) {
				counter += 1;
			}

		}

		if (studentBelongsToDepartment(studentID) == true && counter <= 3) {
			return true;
		} else {
			return false;
		}

	}

	// The below method checks if a course exists in the given semester
	protected synchronized boolean courseExistsInSemester(String courseID, String semester) {
		boolean exists = false;
		try {
			// Each term can have ..* courses
			HashMap<String, HashMap> course = courseRecords.get(semester);

			if (course.get(courseID) != null) {
				exists = true;
			}
		} catch (Exception e) {
			exists = false;
		}
		return exists;
	}

	// The below method returns all of the courses for a given semester
	protected synchronized String semesterCourses(String semester) {
		String message = "";
		try {
			// Each term can have ..* courses
			HashMap<String, HashMap> semesterCourses = courseRecords.get(semester);
			// Looping through course
			for (Map.Entry me : semesterCourses.entrySet()) {

				HashMap<String, HashMap> course = (HashMap<String, HashMap>) semesterCourses.get(me.getKey());

				int totalCapacity = Integer.parseInt((String) course.get("Capacity").get("TotalCapacity"));
				int registered = Integer.parseInt((String) course.get("Capacity").get("Registered"));
				int calc = totalCapacity - registered;
			
				message = " " + semester + " - " + me.getKey() + " " + calc + ",";

			}

		} catch (Exception e) {
			message = "No courses found for semester " + semester + " in department " + departmentIdentifier;
		}
		return message;
	}


	/*****************************************
	 * Methods for client-server connection
	 *****************************************/
	@Override
	public synchronized void registerForCallback(ClientInterface callbackClientObject) throws java.rmi.RemoteException {
		// store the callback object into the vector
		if (!(clientList.contains(callbackClientObject))) {
			clientList.addElement(callbackClientObject);
			System.out.println("Registered new client ");
			doCallbacks();
		}
	}

	// This remote method allows an object client to
	// cancel its registration for callback
	// @param id is an ID for the client; to be used by
	// the server to uniquely identify the registered client.
	public synchronized void unregisterForCallback(ClientInterface callbackClientObject)
			throws java.rmi.RemoteException {
		if (clientList.removeElement(callbackClientObject)) {
			System.out.println("Unregistered client ");
		} else {
			System.out.println("unregister: clientwasn't registered.");
		}
	}

	// When each client connects the first time, this method loops (again) and calls
	// back to the client with a message
	// "Number of registered clients = x"
	private synchronized void doCallbacks() throws java.rmi.RemoteException {
		// Make callback to each registered client
		System.out.println("Server callback method initiated");
		for (int i = 0; i < clientList.size(); i++) {
			System.out.println("doing " + i + "-th callback\n");
			// Convert the vector object to a callback object
			ClientInterface nextClient = (ClientInterface) clientList.elementAt(i);
			// Invoke the callback method
			nextClient.connectionVerification("Number of registered clients=" + clientList.size());
		}
		System.out.println("Server callback method ended");
	}

	private void initDatabase() {
		// Each term can have ..* courses
		HashMap<String, HashMap> course = new HashMap<String, HashMap>();

		// Each course can have ..* course information fields
		HashMap<String, HashMap<String, String>> courseInformation = new HashMap<String, HashMap<String, String>>();

		// Each course can have 0..* students
		HashMap<String, String> students = new HashMap<String, String>();
		students.put(departmentIdentifier + "S1234", "Kurosh Farsimadan");

		courseInformation.put("Capacity", new HashMap<String, String>());
		courseInformation.get("Capacity").put("TotalCapacity", "3");
		courseInformation.get("Capacity").put("Registered", "1");

		courseInformation.put("Information", new HashMap<String, String>());
		courseInformation.get("Information").put("Details", "This is testing material");

		courseInformation.put("RegisteredStudents", new HashMap<String, String>());
		courseInformation.put("RegisteredStudents", students);

		course.put(departmentIdentifier + "349", courseInformation);

		// Each course can have ..* course information fields
		HashMap<String, HashMap<String, String>> courseInformation2 = new HashMap<String, HashMap<String, String>>();
		// Each course can have 0..* students
		HashMap<String, String> students2 = new HashMap<String, String>();
		students2.put(departmentIdentifier + "S1234", "Kurosh Farsimadan");

		courseInformation2.put("Capacity", new HashMap<String, String>());
		courseInformation2.get("Capacity").put("TotalCapacity", "3");
		courseInformation2.get("Capacity").put("Registered", "1");

		courseInformation2.put("Information", new HashMap<String, String>());
		courseInformation2.get("Information").put("Details", "This is testing material");

		courseInformation2.put("RegisteredStudents", new HashMap<String, String>());
		courseInformation2.put("RegisteredStudents", students2);
		course.put(departmentIdentifier + "1234", courseInformation2);

		courseRecords.put("WINTER", course);

		courseRecords.put("FALL", new HashMap<String, HashMap<String, String>>());

		courseRecords.put("SUMMER", new HashMap<String, HashMap<String, String>>());

		// The below two lines cost me 2 days. After debugging, I noticed that everytime
		// I try to change "WINTER" sub hashmaps all of the other hashmaps like "SUMMER"
		// and "FALL" had the same values as I was changing. No wonder as I am
		// referencing the same course on all hashmaps. This means that they all share
		// the same address by reference.

		// courseRecords.put("FALL", course);
		// courseRecords.put("SUMMER", course);
	}

}
