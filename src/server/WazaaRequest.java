package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import ui.MainUi;

/**
 * Requests controller Class.
 * Class that decides which Class must handle the request.
 *
 */
public class WazaaRequest implements Runnable {
	private final static String CRLF = "\r\n";
	private Socket socket;
	private OutputStream output;
	private BufferedReader br;
	private MainUi ui;

	/**
	 * Constructor.
	 * 
	 * @param socket
	 * @param ui
	 */
	public WazaaRequest(Socket socket, MainUi ui) {
		this.socket = socket;
		this.ui = ui;
		try{
			this.output = socket.getOutputStream();
			this.br = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
		} catch(IOException e) {
			ui.addInfo("Reading request failed!\n");
		}
	}

	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			ui.addInfo("Can't process request!\n");
		}
	}

	/**
	 * Method that checks if received request is known.
	 * 
	 * @param string Request that was sent.
	 * @return true if known request.
	 */
	private boolean isValid(String string) {
		if(string.startsWith("/searchfile")) {
			return true;
		} else if(string.startsWith("/getfile")) {
			return true;
		} else if(string.startsWith("/foundfile")) {
			return true;
		}
		return false;
	}

	/**
	 * Method to process request. Decides which Class must handle the request.
	 * 
	 * @throws IOException
	 */
	private void processRequest() throws IOException {
		while (true) {
			String headerLine = br.readLine();
			if (headerLine.equals(CRLF) || headerLine.equals(""))
				break;

			StringTokenizer s = new StringTokenizer(headerLine);
			String method = s.nextToken();
			String request = s.nextToken();

			if (method.equals("GET") && isValid(request)) {
				ui.addInfo(ui.getRequestIndex() + socket.getInetAddress().toString().substring(1) 
						+ ":" + socket.getPort() + " requested: " + request + "\n");
				if(request.startsWith("/getfile?")) {
					WazaaSendFile response = new WazaaSendFile(ui);
					response.sendFile(request, output);
					break;
				} else if(request.startsWith("/searchfile?")) {
					WazaaSearch search = new WazaaSearch(ui, request);
					Thread thread = new Thread(search);
					thread.start();
					break;
				}
			} else if(method.equals("POST") && isValid(request)) {
				if(request.startsWith("/foundfile")) {
					WazaaFileFoundRequest found = new WazaaFileFoundRequest(ui);
					found.processRequest(br);
					break;
				}
			}
		}

		try {
			output.flush();
			output.close();
			br.close();
			socket.close();
		} catch (Exception e) {
			ui.addInfo("Closing connection to: " + socket.getInetAddress().toString().substring(1)
					+ ":" + socket.getPort() + " failed!\n");
		}
	}
}
