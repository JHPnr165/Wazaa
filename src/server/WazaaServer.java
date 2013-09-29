package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import ui.MainUi;

/**
 * Server Class.
 * 
 * @author marko
 *
 */
public class WazaaServer implements Runnable {
	private int port;
	private ServerSocket serverSocket;
	private MainUi ui;
	private boolean serverRunning = true;

	/**
	 * Constructor.
	 * 
	 * @param ui
	 */
	public WazaaServer(MainUi ui) {
		this.ui = ui;
	}

	/**
	 * Set server port number.
	 * 
	 * @param port new port number.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Stop the server.
	 */
	public void stopServer() {
		serverRunning = false;
		try{
			serverSocket.close();
			ui.addInfo("Wazaa stopped\n");
		} catch(Exception e) {
			ui.addInfo("Can't stop server!\n");
		}
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			ui.addInfo("Wazaa running on port " + serverSocket.getLocalPort() + "\n");

			// server infinite loop
			while (serverRunning) {
				Socket socket = serverSocket.accept();

				// Construct handler to process the HTTP request message.
				try {
					WazaaRequest request = new WazaaRequest(socket, ui);
					// Create a new thread to process the request.
					Thread thread = new Thread(request);

					// Start the thread.
					thread.start();
				} catch (Exception e) {
					ui.addInfo("Couldn't accept socket!\n");
				}
			}
		} catch (IOException e) {
			//Nothing to do.
		}
	}
}
