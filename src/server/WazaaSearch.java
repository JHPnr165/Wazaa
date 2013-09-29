package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import ui.MainUi;

/**
 * Class that handles file search requests. Sends out requests created by user and forwards
 * other machine requests if necessary (if TTL is big enough).
 * 
 * @author marko
 *
 */
public class WazaaSearch extends WazaaTools implements Runnable {
	private MainUi ui;
	private ArrayList<String> filesInFolder = new ArrayList<String>();
	private ArrayList<String> filesToSend = new ArrayList<String>();
	private ArrayList<Machine> machines = new ArrayList<Machine>();
	private ArrayList<Machine> machinesToDelete = new ArrayList<Machine>();
	private String requestFileName = "";
	private String id = "lamp";
	private String requestToSend;
	private String request;
	private final static String CRLF = "\r\n";
	private int ttl = 1;
	private int myPort = 0;
	private Machine requestingMachine;

	/**
	 * Constructor. Used if user wants to search file.
	 * 
	 * @param ui
	 * @param requestFileName
	 * @param port
	 */
	public WazaaSearch(MainUi ui, String requestFileName, int port) {
		this.ui = ui;
		this.requestFileName = requestFileName;
		myPort = port;
	}

	/**
	 * Constructor. Used if other machine sent file search request.
	 * 
	 * @param ui
	 * @param request
	 */
	public WazaaSearch(MainUi ui, String request) {
		this.ui = ui;
		this.request = request;
	}

	/**
	 * 
	 */
	private void processRequest() {
		parseRequest();
		if (!machines.isEmpty() && ttl > 0) {
			ui.addInfo("Forwarding filesearch request from: " 
					+ requestingMachine.address.toString().substring(1) + ":" 
					+ requestingMachine.port + "\n");
			sendSearchRequests();
			ui.addInfo("Forwarding filesearch request completed!\n");
		}
		checkFileExistence();
	}

	/**
	 * Method to create Thread to send file found request.
	 */
	private void sendFoundRequest() {
		WazaaFileFoundRequest found = new WazaaFileFoundRequest(ui);
		found.sendFileFound(filesToSend, requestingMachine);
	}

	/**
	 * Method that handles file search request generating and sending.
	 */
	private void searchFile() {
		try {
			String tmpAddress = getLocalIp();
			requestingMachine = new Machine(InetAddress.getByName(tmpAddress), myPort);
			generateSearchRequest();
			ui.addInfo(ui.getRequestIndex() + "Sending filesearch request...\n");
			sendSearchRequests();
			ui.addInfo("Filesearch request sent!\n");
		} catch (UnknownHostException e) {
			ui.addInfo("Unknown Host!\n");
		}
	}

	/**
	 * Method that generates request for searching file.
	 */
	private void generateSearchRequest() {
		requestToSend = "GET /searchfile?name=" + requestFileName 
				+ "&sendip=" + requestingMachine.address.toString().substring(1) 
				+ "&sendport=" + requestingMachine.port + "&ttl=" + ttl 
				+ "&id=" + id + "&noask=";
		for(Machine machine : machinesToDelete) {
			requestToSend += machine.address.toString().substring(1) + "_";
		}
		for(Machine machine : machines) {
			requestToSend += machine.address.toString().substring(1) + "_";
		}
		if(myPort == 0) {
			requestToSend += requestingMachine.address.toString().substring(1) + "_";
		}
		requestToSend = requestToSend.substring(0, requestToSend.length() - 1) + " HTTP/1.0" + CRLF;
	}

	/**
	 * Method to check if file exists.
	 */
	private void checkFileExistence() {
		for(String aFilesInFolder : filesInFolder) {
			if(aFilesInFolder.contains(requestFileName)) {
				filesToSend.add(aFilesInFolder);
			}
		}
		if(filesToSend.size() > 0) {
			sendFoundRequest();
		}
	}

	/**
	 * Method that deletes machines from list that are in noask list.
	 */
	private void deleteNoAskMachines() {
		for(Machine machineToDelete : machinesToDelete) {
			for(Machine machine : machines) {
				if(machineToDelete.equals(machine)) {
					machines.remove(machine);
					if (machines.isEmpty()) {
						break;  //Hangs if there is no break if machines list becomes empty.
					}
				}
			}
		}
	}

	/**
	 * Method that extracts noask addresses from request and puts them into ArrayList.
	 */
	private void getNoAskMachines() {
		try {
			while(request.length() > 1) {
				Machine machineToDelete;
				InetAddress address;
				if(!request.contains("_")) {
					address = InetAddress.getByName(request);
					request = "";
				} else {
					address = InetAddress.getByName(request.substring(0, request.indexOf("_") - 1));
					request = request.substring(request.indexOf("_") + 1);
				}
				machineToDelete = new Machine(address, myPort);
				machinesToDelete.add(machineToDelete);
			}
			deleteNoAskMachines();
		} catch(UnknownHostException e) {
			ui.addInfo("Extracting noask machines info from request failed!\n");
		}
	}

	/**
	 * Get request parameters from search request except noask addresses.
	 */
	private void parseRequest() {
		try {
			requestFileName = request.substring(request.indexOf("name") + 5, request.indexOf(
					"&", request.indexOf("name")));
			InetAddress machineAddress = InetAddress.getByName(request.substring(
					request.indexOf("sendip") + 7, request.indexOf("&", request.indexOf("sendip"))));
			int machinePort = Integer.parseInt(request.substring(
					request.indexOf("sendport") + 9, request.indexOf(
					"&", request.indexOf("sendport"))));
			ttl = Integer.parseInt(request.substring(request.indexOf("ttl") + 4, request.indexOf(
					"&", request.indexOf("ttl"))));
			id = request.substring(request.indexOf("id=") + 3, request.indexOf(
					"&", request.indexOf("id=")));

			requestingMachine = new Machine(machineAddress, machinePort);
			ttl--;
			request = request.substring(request.indexOf("noask") + 6);
			getNoAskMachines();
		} catch (UnknownHostException e) {
			ui.addInfo("Extracting info from search request failed!\n");
		}
	}

	/**
	 * Method that handles sending search request to every machine on the list.
	 */
	private void sendSearchRequests() {
		if(!machines.isEmpty()) {
			generateSearchRequest();
			for(Machine machine : machines) {
				sendFileSearchRequest(machine);
			}
		}
	}

	/**
	 * Method that sends request to given machine.
	 * 
	 * @param machineToSend Machine to send the search request.
	 */
	private void sendFileSearchRequest(Machine machineToSend) {
		PrintWriter out;
		try {
			Socket socket = new Socket(machineToSend.address, machineToSend.port);
			out = new PrintWriter(socket.getOutputStream(), true);
			out.write(requestToSend);
			out.flush();
			out.close();
			socket.close();
		} catch(IOException e) {
			ui.addInfo("Can't connect to: " + machineToSend.address.toString().substring(1)
					+ ":" + machineToSend.port + " to send search request\n");
		}
	}

	/**
	 * Method to get file names from folder.
	 */
	private void getFileNames() {
		String path = "./wazaa";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		if (listOfFiles != null) {
			for(File file : listOfFiles) {
				if(file.isFile()) {
					filesInFolder.add(file.getName());
				}
			}
		}
	}

	/**
	 * Method to read info of machines from file.
	 */
	private void getMachines() {
		String fileName = "machines.txt";
		String machinesString = "";
		String line;
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			line = br.readLine();
			while(line != null) {
				machinesString += line;
				line = br.readLine();
			}
			br.close();
			parseMachinesString(machinesString);
		} catch(Exception e) {
			ui.addInfo("Masinate info importimine failist ebaõnnestus!\n");
		}
	}

	/**
	 * Get machines info from json.
	 * 
	 * @param machinesString json of machines.
	 */
	private void parseMachinesString(String machinesString) {
		if(machinesString.length() > 10) {
			Machine machine;
			InetAddress address;
			int port;
			machinesString = machinesString.substring(3);
			int index;
			try {
				while(machinesString.length() > 10) {
					index = machinesString.indexOf("\"");
					address = InetAddress.getByName(machinesString.substring(0, index));
					port = Integer.parseInt(machinesString.substring(
							index + 3, machinesString.indexOf("\"", index + 3)));
					machine = new Machine(address, port);
					machines.add(machine);
					try {
						machinesString = machinesString.substring(machinesString.indexOf("]") + 4);
					} catch(Exception ex) {
						machinesString = ""; //et lõpmatusse loopi ei jääks
					}
				}
			} catch(Exception e) {
				ui.addInfo("Ei saa masinate infot kätte! Info vales formaadis!\n");
			}
		}
	}

	@Override
	public void run() {
		getMachines();
		getFileNames();
		if(myPort == 0) {
			processRequest();
		} else {
			searchFile();
		}
	}
}
