package server;

import java.net.InetAddress;

/**
 * Class to save information about machines (InetAddress and port number pairs).
 * 
 * @author marko
 *
 */
public class Machine {
	InetAddress address;
	int port;

	/**
	 * Constructor.
	 * 
	 * @param address InetAddress of the machine
	 * @param port Port number of the machine
	 */
	public Machine(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}

	/**
	 * Equals method. Compares only InetAddress.
	 * 
	 * @param machine Machine to compare with.
	 * @return true if it's same IP.
	 */
	public boolean equals(Machine machine) {
		return machine.address.equals(this.address);
	}
}
