package ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import javax.swing.*;
import server.*;

/**
 * Class that creates GUI.
 * 
 * @author marko
 *
 */
public class MainUi extends JFrame {
	WazaaServer server;

	private final JLabel portLabel = new JLabel("Port:");
	private final JLabel fileLabel = new JLabel("File name to search:");
	private final JLabel serverRunningLabel = new JLabel("Server: not running");
	private final JLabel getPortLabel = new JLabel("Port:");
	private final JLabel getAddressLabel = new JLabel("Address:");
	private final JLabel getFileLabel = new JLabel("File name:");
	private final JLabel ttlLabel = new JLabel("TTL:");

	private JButton connectButton = new JButton("Start");
	private JButton disconnectButton = new JButton("Stop");
	private JButton findButton = new JButton("Find!");
	private JButton getButton = new JButton("Get it!");

	private JEditorPane infoField = new JEditorPane();
	private JScrollPane scrollPane = new JScrollPane(infoField);
	private JEditorPane fileField = new JEditorPane();
	private JScrollPane filePane = new JScrollPane(fileField);
	private JTextField fileNameField = new JTextField();
	private JTextField getPortField = new JTextField();
	private JTextField getAddressField = new JTextField();
	private JTextField getFileNameField = new JTextField();

	private SpinnerNumberModel portSpinnerModel = new SpinnerNumberModel(1024, 1024, 65535, 1);
	private JSpinner portSpinner = new JSpinner(portSpinnerModel);
	private SpinnerNumberModel ttlSpinnerModel = new SpinnerNumberModel(1, 1, 10, 1);
	private JSpinner ttlSpinner = new JSpinner(ttlSpinnerModel);

	private JPanel mainPanel = new JPanel();
	private JPanel serverPanel = new JPanel();
	private JPanel infoPanel = new JPanel();
	private JPanel filePanel = new JPanel();
	private JPanel resultPanel = new JPanel();
	private JPanel getPanel = new JPanel();

	//Variable to count requests.
	private int requestNumber = 1;

	/**
	 * Constructor.
	 */
	public MainUi() {
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(600, 600);
		this.setResizable(false);
		this.setTitle("Wazaa!");
		this.setVisible(true);

		//Server control area.
		disconnectButton.setEnabled(false);
		connectButton.addActionListener(new ConnectActionListener());
		disconnectButton.addActionListener(new DisconnectActionListener());
		portLabel.setForeground(Color.WHITE);
		portLabel.setToolTipText("Serveri port peab olema vahemikus 1024-65535");
		serverRunningLabel.setForeground(Color.WHITE);
		portSpinner.setToolTipText("Serveri port peab olema vahemikus 1024-65535");
		serverRunningLabel.setToolTipText("Serveri staatus");
		serverPanel.setBackground(Color.GREEN);
		serverPanel.add(portLabel);
		serverPanel.add(portSpinner);
		serverPanel.add(connectButton);
		serverPanel.add(disconnectButton);
		serverPanel.add(serverRunningLabel);

		//File search area.
		fileNameField.setPreferredSize(new Dimension(240, 25));
		filePanel.setBackground(Color.GREEN);
		fileLabel.setForeground(Color.WHITE);
		fileLabel.setToolTipText("Sisesta otsitava faili nimes sisalduv string");
		fileNameField.setToolTipText("Sisesta otsitava faili nimes sisalduv string");
		findButton.setEnabled(false);
		findButton.addActionListener(new FindActionListener());
		ttlLabel.setForeground(Color.WHITE);
		ttlLabel.setBackground(Color.GREEN);
		ttlLabel.setToolTipText("Vali mitu hoppi päring tegema peab (teised masinad päringut edasi saatma)");
		ttlSpinner.setToolTipText("Vali mitu hoppi päring tegema peab (teised masinad päringut edasi saatma)");
		filePanel.add(fileLabel);
		filePanel.add(fileNameField);
		filePanel.add(ttlLabel);
		filePanel.add(ttlSpinner);
		filePanel.add(findButton);

		//File search answers area.
		fileField.setEditable(false);
		fileField.setText("Search results:\n");
		fileField.setBackground(Color.GREEN);
		fileField.setForeground(Color.WHITE);
		fileField.setToolTipText("Siia tulevad failiotsingu tulemused");
		filePane.setMaximumSize(new Dimension(600, 200));
		filePane.setPreferredSize(new Dimension(600, 200));
		filePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		resultPanel.setBackground(Color.GREEN);
		resultPanel.setForeground(Color.WHITE);
		resultPanel.add(filePane);

		//Server info area.
		infoField.setEditable(false);
		infoField.setText("Server log:\n");
		infoField.setBackground(Color.GREEN);
		infoField.setForeground(Color.WHITE);
		infoField.setToolTipText("Siia tuleb info saabunud/väljuvate päringute kohta");
		scrollPane.setMaximumSize(new Dimension(600, 200));
		scrollPane.setPreferredSize(new Dimension(600, 200));
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		infoPanel.setBackground(Color.GREEN);
		infoPanel.setForeground(Color.WHITE);
		infoPanel.add(scrollPane);

		//File download area.
		getAddressField.setPreferredSize(new Dimension(120, 25));
		getAddressField.setToolTipText("Siia sisesta masina IP aadress kust tahad faili alla laadida");
		getAddressLabel.setBackground(Color.GREEN);
		getAddressLabel.setForeground(Color.WHITE);
		getPortField.setPreferredSize(new Dimension(45, 25));
		getPortField.setToolTipText("Siia sisesta pordi number");
		getPortLabel.setBackground(Color.GREEN);
		getPortLabel.setForeground(Color.WHITE);
		getFileNameField.setPreferredSize(new Dimension(140, 25));
		getFileNameField.setToolTipText("Siia sisesta allalaaditava faili nimi koos laiendiga");
		getFileLabel.setBackground(Color.GREEN);
		getFileLabel.setForeground(Color.WHITE);
		getButton.setEnabled(false);
		getButton.addActionListener(new GetFileActionListener());
		getPanel.setBackground(Color.GREEN);
		getPanel.add(getAddressLabel);
		getPanel.add(getAddressField);
		getPanel.add(getPortLabel);
		getPanel.add(getPortField);
		getPanel.add(getFileLabel);
		getPanel.add(getFileNameField);
		getPanel.add(getButton);

		//Add all panels to main panel.
		mainPanel.setBackground(Color.GREEN);
		mainPanel.add(serverPanel);
		mainPanel.add(infoPanel);
		mainPanel.add(filePanel);
		mainPanel.add(resultPanel);
		mainPanel.add(getPanel);
		this.add(mainPanel);
	}

	/**
	 * 
	 * @return returns current port number.
	 */
	public int getPort() {
		return (Integer) portSpinner.getValue();
	}

	/**
	 * Method that adds Server activity info to infoField.
	 * 
	 * @param infoToAdd Text that will be added to infoField.
	 */
	public void addInfo(String infoToAdd) {
		infoField.setText(infoField.getText() + infoToAdd);
	}

	/**
	 * Method that adds file search results to file search field.
	 * 
	 * @param result File search results.
	 */
	public void addSearchResult(String result) {
		fileField.setText(fileField.getText() + result);
	}

	/**
	 * Method that creates new file search Thread and starts it.
	 */
	private void searchFile() {
		if (fileNameField.getText() != "") {
			WazaaSearch search = new WazaaSearch(this, fileNameField.getText(), (Integer) portSpinner.getValue());
			Thread thread = new Thread(search);
			thread.start();
		}
	}

	/**
	 * Method to get the file name to search.
	 * 
	 * @return File name.
	 */
	public String getFileNameToSearch() {
		return fileNameField.getText();
	}

	/**
	 * Creates new WazaaServer.
	 */
	private void makeServer() {
		server = new WazaaServer(this);
	}

	/**
	 * Method that counts how many requests have been made. Just for helping to separate
	 * different requests in infoField.
	 * 
	 * @return Returns String "#n: " where n is number.
	 */
	public String getRequestIndex() {
		return "#" + requestNumber++ + ": ";
	}

	/**
	 * Gets parameters from GUI and creates Thread to download the file.
	 */
	private void getFile() {
		try {
			InetAddress address = InetAddress.getByName(getAddressField.getText());
			int port = Integer.parseInt(getPortField.getText());
			String fileName = getFileNameField.getText();
			WazaaGetFile getFile = new WazaaGetFile(address, port, fileName, this);
			Thread thread = new Thread(getFile);
			thread.start();
		} catch (Exception e) {
			addInfo("Downloading failed! Wrong values!\n");
		}
	}

	/**
	 * ActionListener Class for Connect button.
	 * 
	 * @author marko
	 *
	 */
	private class ConnectActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (connectButton.isEnabled()) {
				findButton.setEnabled(true);
				getButton.setEnabled(true);
				makeServer();
				connectButton.setEnabled(false);
				disconnectButton.setEnabled(true);
				serverRunningLabel.setText("Server: running...");
				try {
					server.setPort((Integer) portSpinner.getValue());
				} catch (Exception ex) {
					server.setPort(6666);
					addInfo("Couldn't get port number! Using default: 6666\n");
				}
				Thread thread = new Thread(server);
				thread.start();
			}
		}
	}

	/**
	 * ActionListener Class for Disconnect button.
	 * 
	 * @author marko
	 *
	 */
	private class DisconnectActionListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (disconnectButton.isEnabled()) {
				findButton.setEnabled(false);
				getButton.setEnabled(false);
				disconnectButton.setEnabled(false);
				connectButton.setEnabled(true);
				serverRunningLabel.setText("Server: not running");
				server.stopServer();
			}
		}
	}

	/**
	 * ActionListener Class for findButton.
	 * 
	 * @author marko
	 *
	 */
	private class FindActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			searchFile();
		}
	}

	/**
	 * ActionListener Class for getButton.
	 * 
	 * @author marko
	 *
	 */
	private class GetFileActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			getFile();
		}
	}
}
