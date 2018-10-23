package Client;

import java.net.MalformedURLException;
import java.rmi.*;
import java.util.Scanner;

public class Client {

	public static Scanner input = new Scanner(System.in);

	public static void main(String args[]) {

		try {

			boolean recognized = false;
			String department = "";
			boolean isAdvisor = false;

			do {

				System.out.println("Please enter your full person ID (try testing SOENS1234 or SOENS1122): ");
				String personID = input.nextLine();
				personID = personID.toUpperCase();

				if (personID.contains("COMPA") || personID.contains("SOENA") || personID.contains("INSEA")) {
					isAdvisor = true;
					System.out.println("You have been recognized as an advisor: " + personID);
					recognized = true;
				} else if (personID.contains("COMPS") || personID.contains("SOENS") || personID.contains("INSES")) {
					isAdvisor = false;
					System.out.println("You have been recognized as a student: " + personID);
					recognized = true;
				} else {
					recognized = false;
					System.out.println("The given ID is not recognized. Please try inserting your full person ID");
				}

				if (recognized == true) {

					if (personID.contains("COMPA") || personID.contains("COMPS")) {
						department = "COMP";
					} else if (personID.contains("SOENA") || personID.contains("SOENS")) {
						department = "SOEN";
					} else if (personID.contains("INSEA") || personID.contains("INSES")) {
						department = "INSE";
					}

					System.out.println("You belong to the " + department + " department");

					clientHandler(personID, department, isAdvisor);
				}

			} while (recognized == false);

		} catch (Exception e) {
			System.out.println("Exception in RMICallbackClient: " + e);
		}

	}

	public static void clientHandler(String personID, String department, boolean isAdvisor) {
		System.out.println("Client handler invoked");

		// The below int corresponds to a particular method. Both advisor and student
		// has 3 available methods to choose from
		int choose = 4;

		do {
			try {
				System.out.println(
						"Please choose one of the following available remote service methods by pressing corresponding numeric value or press 0 numeric key to exit:");
				
				System.out.println("0. Exit the system");
				
				if (isAdvisor) {
					System.out.println("1. Add a course");
					System.out.println("2. Remove a course");
					System.out.println("3. List course availability");
				} else {
					System.out.println("1. Enrol to a course");
					System.out.println("2. Get class schedule");
					System.out.println("3. Drop a course");
				}

				choose = input.nextInt();
				callRMIService(personID, department, choose, isAdvisor);
			} catch (Exception e) {
				System.out.println("Sorry, an error occurred. Please use the numeric keypad.");
			}

		} while (choose != 0);

		// String personID, String department, int method
		
	}



	public static void callRMIService(String personID, String department, int method, boolean isAdvisor) {

		try {

			// RMI server port
			int port = 0;

			// For the courseID input
			String courseID = "";

			// For the semester input
			String semester = "";

			// For retrieving the message
			String message = "";

			// For transforming the method integer to meaningful string
			String requestType = "";

			/*
			 * SOEN - RMI server starts at 8080
			 * 
			 * INSE - RMI server starts at 9090
			 * 
			 * COMP - RMI server starts at 10010
			 */
			if (department.equalsIgnoreCase("COMP")) {
				port = 10010;
			} else if (department.equalsIgnoreCase("INSE")) {
				port = 9090;
			} else {
				port = 8080;
			}

			// RMI server hostname
			String hostName = "127.0.0.1";

			// Registry URL ipORname:port/serviceName
			String registryURL = "rmi://" + hostName + ":" + port + "/callback";

			SOEN.ServerInterface soen = null;
			COMP.ServerInterface comp = null;
			INSE.ServerInterface inse = null;

			// Find the remote object and cast it to an interface object. After the below
			// line's have executed successfully, the lookkup has completed.
			if (department.equalsIgnoreCase("COMP")) {
				// The below import is for the COMP server interface
				comp = (COMP.ServerInterface) Naming.lookup(registryURL);
			} else if (department.equalsIgnoreCase("INSE")) {
				// The below import is for the INSE server interface
				inse = (INSE.ServerInterface) Naming.lookup(registryURL);
			} else {
				// The below import is for the SOEN server interface
				soen = (SOEN.ServerInterface) Naming.lookup(registryURL);
			}

			// If the person is an advisor or student, because the advisors have different
			// sets of methods available for them and vice versa. We don't want the students
			// to access advisor methods. Also, some methods require more or less passed
			// data values. This whole functionality could be moved to its own method, but
			// as a result of time constraint, it looks like what it looks like in the
			// below.
			if (isAdvisor) {
				if (method == 1) {
					requestType = "Add a course";

					System.out.println("Please provide the courseID for the given service (try testing SOEN1234): ");
					courseID = input.next();

					System.out.println("Please provide the semester for the given service (try testing WINTER): ");
					semester = input.next();

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.addCourse(courseID, semester);
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.addCourse(courseID, semester);
					} else if (department.equalsIgnoreCase("INSE")) {
						message = soen.addCourse(courseID, semester);
					}

				} else if (method == 2) {
					requestType = "Remove a course";

					System.out.println("Please provide the courseID for the given service (try testing SOEN1234): ");
					courseID = input.next();

					System.out.println("Please provide the semester for the given service (try testing WINTER): ");
					semester = input.next();

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.removeCourse(courseID, semester.toUpperCase());
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.removeCourse(courseID, semester.toUpperCase());
					} else if (department.equalsIgnoreCase("INSE")) {
						message = inse.removeCourse(courseID, semester.toUpperCase());
					}

				} else if (method == 3) {
					requestType = "List course availability";

					System.out.println("Please provide the semester for the given service (try testing WINTER): ");
					semester = input.next();

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.listCourseAvailability(semester.toUpperCase());
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.listCourseAvailability(semester.toUpperCase());
					} else if (department.equalsIgnoreCase("INSE")) {
						message = inse.listCourseAvailability(semester.toUpperCase());
					}

				}
			} else {
				if (method == 1) {
					requestType = "Enrol to a course";

					System.out.println("Please provide the courseID for the given service (try testing SOEN1234): ");
					courseID = input.next();

					System.out.println("Please provide the semester for the given service (try testing WINTER): ");
					semester = input.next();

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.enrolCourse(personID, courseID, semester.toUpperCase());
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.enrolCourse(personID, courseID, semester.toUpperCase());
					} else if (department.equalsIgnoreCase("INSE")) {
						message = inse.enrolCourse(personID, courseID, semester.toUpperCase());
					}

				} else if (method == 2) {
					requestType = "Get class schedule";

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.getClassSchedule(personID);
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.getClassSchedule(personID);
					} else if (department.equalsIgnoreCase("INSE")) {
						message = inse.getClassSchedule(personID);
					}

				} else if (method == 3) {
					requestType = "Drop a course";

					System.out.println("Please provide the courseID for the given service (try testing SOEN1234): ");
					courseID = input.next();

					if (department.equalsIgnoreCase("COMP")) {
						message = comp.dropCourse(personID, courseID);
					} else if (department.equalsIgnoreCase("SOEN")) {
						message = soen.dropCourse(personID, courseID);
					} else if (department.equalsIgnoreCase("INSE")) {
						message = inse.dropCourse(personID, courseID);
					}

				}

			}

			System.out.println("The message from the server is " + message);
			LoggerService log = new LoggerService();

			log.logger(personID, requestType, message);

			/*
			 * // We dont need callbacks ClientInterface callbackObj = new ClientImpl(); //
			 * register for callback soen.registerForCallback(callbackObj);
			 * System.out.println("Registered for callback.");
			 * soen.unregisterForCallback(callbackObj);
			 * System.out.println("Unregistered for callback.");
			 */

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}

}
