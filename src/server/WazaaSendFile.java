package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;

import ui.MainUi;

/**
 * Class to send requested file.
 * 
 * @author marko
 *
 */
public class WazaaSendFile {
	private final static String CRLF = "\r\n";
	//Gui reference.
	private MainUi ui;
	//File that will be sent.
	private File file;

	/**
	 * Constructor.
	 * 
	 * @param ui
	 */
	public WazaaSendFile(MainUi ui) {
		this.ui = ui;
	}

	/**
	 * Method that sends the file.
	 * 
	 * @param fis
	 * @param os
	 * @throws Exception
	 */
	private void sendBytes(FileInputStream fis, OutputStream os)
			throws Exception {
		byte[] buffer = new byte[1024];
		while ((fis.read(buffer)) != -1) { //Is it end of the world...file.
			os.write(buffer, 0, buffer.length);
		}
	}

	/**
	 * Method to get content-type.
	 * Basically pointless because it's not for the browsers. Too lazy to delete it :)
	 * 
	 * @param fileName
	 * @return
	 */
	private String contentType(String fileName) {
		if (fileName.endsWith(".htm") || fileName.endsWith(".html")
				|| fileName.endsWith(".txt")) {
			return "text/html";
		} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (fileName.endsWith(".gif")) {
			return "image/gif";
		} else {
			return "application/octet-stream";
		}
	}

	/**
	 * Method that sends request headers.
	 * 
	 * @param fileName
	 * @param output
	 */
	public void sendFile(String fileName, OutputStream output) {
		try{
			long start = System.currentTimeMillis();
			file = new File("./wazaa/" + fileName.substring(18));

			FileInputStream fis = null;
			boolean fileExists = true;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				fileExists = false;
			}
			String statusLine = null;
			String contentTypeLine = null;
			String entityBody = null;
			String contentLengthLine = "error" + CRLF;
			if (fileExists) {
				ui.addInfo(ui.getRequestIndex() + "Sending file: " + fileName.substring(8) + "...\n");
				statusLine = "HTTP/1.0 200 OK" + CRLF;
				contentTypeLine = "Content-type: " + contentType(fileName)
						+ CRLF;
				contentLengthLine = "Content-Length: "
						+ (new Integer(fis.available())).toString() + CRLF;
			} else {
				statusLine = "HTTP/1.0 404 Not Found" + CRLF;
				contentTypeLine = "Content-type: text/html" + CRLF;     //In case client is using browser.
				entityBody = "<html>"                                   //In case client is using browser.
						+ "<head><title>Wazaa is unhappy :(</title></head>"
						+ "<body>I'm sorry! I searched and searched...but didn't find the file :("
						+ "<br></body></html>";
			}

			PrintWriter out = new PrintWriter(output);
			out.write(statusLine);
			out.write(contentTypeLine);
			out.write(contentLengthLine);
			out.write(CRLF);
			out.flush();

			// Send the entity body.
			if (fileExists) {
				sendBytes(fis, output);
				fis.close();
				long time = System.currentTimeMillis() - start;
				ui.addInfo("File sent! Time: " + time + " ms\n");
			} else {
				ui.addInfo("Requested file doesn't exist!\n");
				output.write(entityBody.getBytes());
			}
		} catch(Exception e) {
			ui.addInfo("Couldn't send file!\n");
		}
	}
}
