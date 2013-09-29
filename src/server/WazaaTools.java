package server;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * Class for methods that several Classes need. To avoid duplicate code.
 * 
 * @author marko
 *
 */
public class WazaaTools {
	/**
	 * Constructor. Not needed.
	 */
	public WazaaTools() {}

	/**
	 * Method to get local IP address.
	 * 
	 * @return Local IP address
	 */
	protected String getLocalIp() {
		String localAddress = "";
		try {
			localAddress = InetAddress.getLocalHost().getHostAddress();
			if(localAddress.contains("/")) { //Windows gives machine name with IP address.
				localAddress = localAddress.substring(localAddress.indexOf("/") + 1);
			}
			//If there are multiple interfaces it can give loopback
			if(localAddress.startsWith("127.0.")) {
				NetworkInterface interFace;
				try{
					//If it's WLAN interface
					interFace = NetworkInterface.getByName("wlan0");
					Enumeration address = interFace.getInetAddresses();
					//First is IPv6 address we don't need
					localAddress = address.nextElement().toString();
					//Now it's IPv4 address
					localAddress = address.nextElement().toString();
					//take "/" away from the beginning
					localAddress = localAddress.substring(localAddress.indexOf("/") + 1);
				} catch(Exception e1) {} //No need for it.
				try{
					//If it's oldschool and more secure...cable :)
					interFace = NetworkInterface.getByName("eth0");
					Enumeration address = interFace.getInetAddresses();
					localAddress = address.nextElement().toString();
					localAddress = address.nextElement().toString();
					localAddress = localAddress.substring(localAddress.indexOf("/") + 1);
				} catch(Exception e2) {} //No need for it.
			}
		} catch (UnknownHostException e) {}
		return localAddress;
	}
}
