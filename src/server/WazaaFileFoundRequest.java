package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import ui.MainUi;

/**
 * Class to handle requests that give file names that was found in other machines.
 * 
 *
 */
public class WazaaFileFoundRequest extends WazaaTools {
	private final static String CRLF = "\r\n";
	//GUI reference.
	private MainUi ui;
	//Request body.
	private String body;
	//Request headers.
	private String headers;
	//Request body in chars.
	private char[] chars;
	//Machine that made search request.
	private Machine requestingMachine;
	//File names that were found and will be sent.
	private ArrayList<String> filesToSend;

	public WazaaFileFoundRequest(MainUi ui) {
		this.ui = ui;
	}

	/**
	 * Method that handles file found request sending.
	 * 
	 * @param filesToSend File names that were found.
	 * @param requestingMachine Machine to send the request.
	 */
	public void sendFileFound(ArrayList<String> filesToSend, Machine requestingMachine) {
		this.filesToSend = filesToSend;
		this.requestingMachine = requestingMachine;
		makeRequest();
		sendRequest();
	}

	/**
	 * Method that creates body and headers for filefound request.
	 */
	private void makeRequest() {
		body = "{\"id\":\"lamp\",\"files\":[";
		for(String file : filesToSend) {
			body += "{\"ip\":\"" + getLocalIp() 
					+ "\", \"port\":\"" + ui.getPort() + "\", \"name\":\"" 
					+ file + "\"},";
		}
		body = body.substring(0, body.length() - 1) + "]}";
		headers = "POST /foundfile HTTP/1.0" + CRLF + "Content-Length: " + body.length() + CRLF + CRLF;
	}

	/**
	 * Method that sends file found request.
	 */
	private void sendRequest() {
		ui.addInfo(ui.getRequestIndex() + "Sending filefound request to: " 
				+ requestingMachine.address.toString().substring(1) 
				+ ":" + requestingMachine.port + "...\n");
		Socket socket;
		OutputStream out;
		try {
			socket = new Socket(requestingMachine.address, requestingMachine.port);
			out = socket.getOutputStream();
			out.write(headers.getBytes());
			out.flush();
			out.write(body.getBytes());
			out.flush();
			out.close();
			socket.close();
			ui.addInfo("Filefound request sent to: " + requestingMachine.address.toString().substring(1) 
					+ ":" + requestingMachine.port + "\n");
		} catch (IOException e) {
			ui.addInfo("Couldn't send filefound request to: " + requestingMachine.address 
					+ ":" + requestingMachine.port + "\n");
		}
	}

	/**
	 * Method to parse body. Extracts info about machines and files from json and prints to GUI.
	 */
	private void parseBody() {
		String toPrint = "";
		JSONObject bodyObject = new JSONObject(body);
		JSONArray arrayOfFiles = bodyObject.getJSONArray("files");
		for(int i = 0; i < arrayOfFiles.length(); i++) {
			toPrint += "IP: " + arrayOfFiles.getJSONObject(i).getString("ip")
					+ " port: " + arrayOfFiles.getJSONObject(i).getString("port")
					+ " file name: " + arrayOfFiles.getJSONObject(i).getString("name") + "\n";
		}
		ui.addSearchResult(toPrint);
	}

	/**
	 * Method to get content length of the body.
	 * 
	 * @param br
	 */
	public void processRequest(BufferedReader br) {
		try {
			while(true) {
				body = br.readLine();
				if(body.startsWith("Content-Length")) {
					int contentLength = Integer.parseInt(body.substring(16));
					chars = new char[contentLength];
					getBody(br);
					break;
				}
			}
		} catch (IOException e) {
			ui.addInfo("Connection lost while getting filefound request!\n");
		}
	}

	/**
	 * Method to get request body.
	 * 
	 * @param br
	 */
	private void getBody(BufferedReader br) {
		try {
			while(true) {
				body = br.readLine();
				if(body.equals("")) {
					br.read(chars);
					body = String.copyValueOf(chars);
					break;
				}
			}
			parseBody();
		} catch(IOException e) {
			ui.addInfo("filefound request had wrong Content-Length value!\n");
		}
	}
}
