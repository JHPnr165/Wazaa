package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import ui.MainUi;

/**
 * Class for downloading file.
 * 
 * @author marko
 *
 */
public class WazaaGetFile implements Runnable {
	private static final String CRLF = "\r\n";
	private Socket socket;
	private InetAddress address;
	private int port;
	private String fileName;
	private MainUi ui;

	/**
	 * Constructor.
	 * 
	 * @param address Machine IP from where to download file.
	 * @param port Machine port number from where to download file.
	 * @param fileName File name to download.
	 * @param ui
	 */
	public WazaaGetFile(InetAddress address, int port, String fileName, MainUi ui) {
		this.address = address;
		this.port = port;
		this.fileName = fileName;
		this.ui = ui;
	}

	/**
	 * Sends request to download file.
	 */
	private void getFile() {
		try {
			socket = new Socket(address, port);
			ui.addInfo(ui.getRequestIndex() + "Trying to get file: " + fileName + "...\n");
			String request = "GET /getfile?fullname=" + fileName + " HTTP/1.0" + CRLF;
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.write(request);
			out.flush();
			receiveFile();
			out.close();
		} catch (IOException e) {
			ui.addInfo("Couldn't connect to machine to download the file!\n");
		}
	}

	/**
	 * Downloads file if it exists.
	 */
	private void receiveFile() {
		long start, time;
		String line;
		BufferedReader br;
		try {
			start = System.currentTimeMillis();
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			line = br.readLine();
			if(line.endsWith("200 OK")) {
				while(!line.equals("")) { //Skipime mõtetu infi (muu headeri).
					line = br.readLine();
				}

				File file = new File("./wazaa/" + fileName);
				FileOutputStream fos = new FileOutputStream(file);

				byte[] buffer = new byte[1024];
				InputStream is = socket.getInputStream();
				int len = 0;
				while ((len = is.read(buffer)) > 0) {  
		            fos.write(buffer, 0, len);
		        }

				time = System.currentTimeMillis() - start;
				ui.addInfo("File received! Time: " + time + " ms\n");
				is.close();
				fos.close();
				socket.close();
			} else {
				ui.addInfo("File you requested doesn't exist or recieved request is not correct!\n");
			}
		} catch (IOException e) {
			ui.addInfo("Can't get file! Can't get stream.\n");
		}
	}

	@Override
	public void run() {
		getFile();
		
	}
}
