package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;

import ui.MainUi;

/**
 * Class for downloading file.
 * 
 *
 */
public class WazaaGetFile implements Runnable {
	private static final String CRLF = "\r\n";
	private Socket socket;
	//IP of machine from where you download the file.
	private InetAddress address;
	//Port number of machine where you download the file.
	private int port;
	//File name you download.
	private String fileName;
	//GUI reference.
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
			fileName = URLEncoder.encode(fileName, "UTF-8");
			socket = new Socket(address, port);
			ui.addInfo(ui.getRequestIndex() + "Trying to get file: " + fileName + "...\n");
			String request = "GET /getfile?fullname=" + fileName + " HTTP/1.0" + CRLF + CRLF;
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.write(request);
			out.flush();
			ui.addInfo("Request sent. waiting response...\n");
			receiveFile();
			out.close();
			socket.close();
		} catch (IOException e) {
			ui.addInfo("Couldn't connect to machine to download the file!\n");
		}
	}

	/**
	 * Method to check if file exists.
	 * 
	 * @param fileNameToSave File name to check
	 * @return True if file name exists.
	 */
	private boolean isFileExisting(String fileNameToSave) {
		ArrayList<String> filesInFolder = new WazaaTools().getFileNames();
		for(String fileInFolder : filesInFolder) {
			if(fileInFolder.equals(fileNameToSave)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates and returns File object.
	 * 
	 * @return
	 */
	private File createFile() {
		File file;
		if(!isFileExisting(fileName)) {
			return file = new File("./wazaa/" + fileName);
		} else {
			int i = 1;
			do{
				try{
					fileName = fileName.substring(0, fileName.indexOf("(") + 1) + i
							+ fileName.substring(fileName.indexOf(")"));
				} catch(Exception e) {
					fileName = fileName.subSequence(0, fileName.lastIndexOf("."))
							+"(" + i + ")" + fileName.substring(fileName.lastIndexOf("."));
				}
				i++;
			} while(isFileExisting(fileName));
			return file = new File("./wazaa/" + fileName);
		}
	}

	/**
	 * Downloads file if it exists.
	 */
	private void receiveFile() {
		try {
			long start = System.currentTimeMillis();
			InputStream inputStream = socket.getInputStream();
			DataInputStream input = new DataInputStream(inputStream);
			String line = input.readLine();

			if(line.contains("200 OK")) {
				while(!line.equals("")) { //Skipime mõtetu infi mille saatja võib panna (muu headeri).
					line = input.readLine();
				}
				File file = createFile();
				FileOutputStream output = new FileOutputStream(file);
				byte[] buffer = new byte[4096];
				int bufferLength;

				while((bufferLength = input.read(buffer)) != -1) {
					output.write(buffer, 0, bufferLength);
				}

				long time = System.currentTimeMillis() - start;
				ui.addInfo("File received! Time: " + time + " ms\n");
				inputStream.close();
				output.close();
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
