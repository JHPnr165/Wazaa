package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;

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
	//Files that are in Wazaa folder.
	private ArrayList<String> filesInFolder = new ArrayList<String>();
	//File names that matched search request and will be sent.
	private ArrayList<String> fileNamesToSend = new ArrayList<String>();
	//Machines that are in database.
	private ArrayList<Machine> machines = new ArrayList<Machine>();
	//Machines to not to forward search request.
	private ArrayList<Machine> noAskMachines = new ArrayList<Machine>();
	//File name to search from Wazaa folder.
	private String requestFileName = "";
	//Pointless parameter...but voices from higher levels ordered to put :D
	private String id = "lamp";
	//Search request that will be sent.
	private String requestToSend;
	//Received search request.
	private String request;
	private final static String CRLF = "\r\n";
	//Time to live parameter.
	private int ttl;
	//Current machine port number.
	private int myPort = 0;
	//Machine that made search request.
	private Machine requestingMachine;

	/**
	 * Constructor. Used if user wants to search file.
	 * 
	 * @param ui
	 * @param requestFileName File name to search
	 * @param port This machine Wazaa port number
	 */
	public WazaaSearch(MainUi ui, String requestFileName, int port) {
		this.ui = ui;
		this.requestFileName = requestFileName;
		myPort = port;
		ttl = ui.getTTL();
	}

	/**
	 * Constructor. Used if other machine sent file search request.
	 * 
	 * @param ui
	 * @param request Received search request.
	 */
	public WazaaSearch(MainUi ui, String request) {
		this.ui = ui;
		this.request = request;
	}

	/**
	 * Process received search request.
	 */
	private void processRequest() {
		parseRequest();
		if (!machines.isEmpty() && ttl > 0) {
			ui.addInfo("Forwarding filesearch request from: " 
					+ requestingMachine.address.toString().substring(1) + ":" 
					+ requestingMachine.port + "\n");
			generateSearchRequest();
			sendSearchRequests();
		}
		checkFileExistence();
	}

	/**
	 * Method to create Thread to send file found request.
	 */
	private void sendFoundRequest() {
		WazaaFileFoundRequest found = new WazaaFileFoundRequest(ui);
		found.sendFileFound(fileNamesToSend, requestingMachine);
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
		for(Machine machine : noAskMachines) {
			requestToSend += machine.address.toString().substring(1) + "_";
		}
		for(Machine machine : machines) {
			requestToSend += machine.address.toString().substring(1) + "_";
		}
		requestToSend += getLocalIp() + " HTTP/1.0" + CRLF + CRLF;
	}

	/**
	 * Method to check if file exists.
	 */
	private void checkFileExistence() {
		for(String file : filesInFolder) {
			if(file.contains(requestFileName)) {
				fileNamesToSend.add(file);
			}
		}
		if(fileNamesToSend.size() > 0) {
			sendFoundRequest();
		}
	}

	/**
	 * Method that deletes machines from list that are in noask list.
	 */
	private void deleteNoAskMachines() {
		for(Machine machineToDelete : noAskMachines) {
			for(Machine machine : machines) {
				if(machineToDelete.equals(machine)) {
					machines.remove(machine);
					if (machines.isEmpty()) {
						break;  //Wazaa will be very sad and hangs if it will become empty.
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
					address = InetAddress.getByName(request); ////
					request = "";
				} else {
					address = InetAddress.getByName(request.substring(0, request.indexOf("_") - 1));
					request = request.substring(request.indexOf("_") + 1);
				}
				machineToDelete = new Machine(address, myPort);
				noAskMachines.add(machineToDelete);
			}
			deleteNoAskMachines();
		} catch(UnknownHostException e) {
			ui.addInfo("Extracting noask machines info from request failed!\n");
		}
	}

	/**
	 * Using String operations...just to make your eyes flicker :)
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
	 * Method to download machines info from file in web.
	 */
	private void downloadMachines() {
		String fileURL = "http://dijkstra.cs.ttu.ee/~t123650/machines.txt";
		String content = "";
		try {
			URL url = new URL(fileURL);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while((line = br.readLine()) != null) {
				content += line;
			}
			br.close();
			System.out.println(content);
		} catch (MalformedURLException e) {
			ui.addInfo("Faili aadress, mis on internetis, on vale või fail on kustutatud!\n");
		} catch (IOException e) {
			ui.addInfo("I/O exception!\n");
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
			String tokens = "[]\",{} ";
			StringTokenizer machinesTokenized = new StringTokenizer(machinesString);
			try {
				while(machinesTokenized.hasMoreTokens()) {
					address = InetAddress.getByName(machinesTokenized.nextToken(tokens));
					port = Integer.parseInt(machinesTokenized.nextToken(tokens));
					machine = new Machine(address, port);
					machines.add(machine);
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
		downloadMachines();
		if(myPort == 0) {
			processRequest();
		} else {
			searchFile();
		}
	}
}
