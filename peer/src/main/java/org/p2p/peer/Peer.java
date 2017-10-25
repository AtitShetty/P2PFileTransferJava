package org.p2p.peer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Hello world!
 *
 */
public class Peer {

	private static Timer timer = new Timer("timer");

	private static int cookie;

	private static Integer myPort;

	private static RFCServer rfcServer;

	public static List<PeerInfo> peerInfoList = Collections.synchronizedList(new LinkedList<Peer.PeerInfo>());

	public static List<RFCNode> rfcList = Collections.synchronizedList(new LinkedList<RFCNode>());

	public static List<String> existingFiles = new LinkedList<String>();

	public static String LOCALHOST = "";

	public static String rsServerHostname = "";

	public static int rsServerPort = 65423;

	public static String rfcFolderPath = "";

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		LOCALHOST = InetAddress.getLocalHost().getHostAddress();

		rsServerHostname = LOCALHOST;

		Scanner sc = new Scanner(System.in);

		System.out.println("Please enter a port number for your RFC server");

		while (myPort == null) {
			try {

				myPort = sc.nextInt();

				System.out.println("You have entered:" + myPort);

				rfcServer = new RFCServer(myPort);

			} catch (IOException e) {
				System.out.println("Port is already in use. Please use another port");
				myPort = null;
			}
		}

		System.out.println("Please enter absolute path of your rfc files directory");

		while (StringUtils.isEmpty(rfcFolderPath)) {
			String temp = sc.next();

			if (Paths.get(temp) != null && (new File(Paths.get(temp).toString())).isDirectory()) {
				rfcFolderPath = temp;
				//System.out.println(rfcFolderPath);
				System.out.println((new File(Paths.get(temp).toString())).list().length +" files found");
			} else {
				System.out.println("Error with given path");
			}
		}

		updateMyRFCList();

		scheduleTask();

		rfcServer.start();

		while (true) {

			boolean exit = false;

			System.out.println("1.Register\n" + "2.Leave\n" + "3.PQuery \n" + "4.Keep Alive\n" + "5.RFCQuery \n"
					+ "6.GetRFC \n" + "7.Set RS server hostname\n" + "8.Set RS server port\n" + "9.Exit\n");
			int choice = sc.nextInt();
			switch (choice) {
			case 1:
				register();
				break;
			case 2:
				leave();
				break;
			case 3:
				pQuery();
				break;
			case 4:
				keepAlive();
				break;
			case 5:
				getRFCIndex();

				System.out.println(Arrays.toString(rfcList.toArray()));
				break;
			case 6:
				getRFC();

				break;
			case 7:
				System.out.println("Please enter a valid hostname. Default is localhost");
				rsServerHostname = sc.next();
				System.out.println("New hostname is: " + rsServerHostname);
				break;
			case 8:
				System.out.println("Please enter a valid port. Default is 65423");
				try {
					rsServerPort = sc.nextInt();
					System.out.println("New port is: " + rsServerPort);
				} catch (Exception e) {
					System.out.println("Port Invalid" + e);
					rsServerPort = 65423;
					System.out.println("Port is: " + rsServerPort);
				}

				break;
			case 9:
				exit = true;
				rfcServer.stopServer = true;
				timer.cancel();
				destroyRFCServer();
				
				break;
			default:
				break;
			}

			if (exit) {
				break;
			}
		}
		sc.close();
	}

	public static void register() {
		try {

			//System.out.println("about to register with host" + LOCALHOST);
			Socket client = new Socket(LOCALHOST, rsServerPort);
			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());
			toRS.writeObject("REGISTER\nHOST " + LOCALHOST + "\nPORT " + myPort);
			String res = fromRS.readObject().toString();
			System.out.println("Response Message:\n\n" + res +"\n");

			if (res.startsWith("REGISTERED")) {
				Peer.cookie = Integer.parseInt(res.split("\n")[1].split(" ")[1]);
			}

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not register:\n" + e);
		}
	}

	public static void leave() {
		try {

			Socket client = new Socket(rsServerHostname, rsServerPort);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("LEAVE\nCookie: " + cookie);

			System.out.println("Response Message:\n\n"+fromRS.readObject().toString()+"\n");

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not unregister:\n" + e);
		}
	}

	public static void pQuery() {

		try {

			Socket client = new Socket(rsServerHostname, rsServerPort);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("GET PEER_LIST\nCookie: " + cookie);

			String response = fromRS.readObject().toString();

			String[] respArr = response.split("\n");

			if (response.startsWith("OK")) {

				for (int i = 1; i < respArr.length; i++) {

					String arr[] = respArr[i].split(" ");

					peerInfoList.add(new PeerInfo(arr[0], Integer.parseInt(arr[1])));
				}
			}

			System.out.println("Response Message:\n\n"+response+"\n");

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not unregister:\n" + e);
		}

	}

	public static void keepAlive() {

		try {

			Socket client = new Socket(rsServerHostname, rsServerPort);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("KEEP_ALIVE\nCookie: " + cookie);

			System.out.println("Response Message:\n\n"+fromRS.readObject().toString()+"\n");

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Exception:\n" + e);
		}

	}

	public static void getRFCIndex() {
		Iterator<PeerInfo> it = peerInfoList.iterator();

		while (it.hasNext()) {
			PeerInfo peer = it.next();

			try {
				if (StringUtils.equals(LOCALHOST, peer.hostName) && peer.port == myPort) {
					continue;
				}
				System.out.println("Getting RFCIndex from peer at " + peer.toString());

				Socket client = new Socket(peer.hostName, peer.port);

				ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

				ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

				toRS.writeObject("GET RFC-Index");

				String response = fromRS.readObject().toString();

				if (response.startsWith("OK")) {
					String[] respArr = response.split("\n");

					for (int i = 1; i < respArr.length; i++) {
						String[] temp = respArr[i].split(" ");

						RFCNode tempRfc = new RFCNode(Integer.parseInt(temp[0]), temp[1], Integer.parseInt(temp[2]));

						if (!rfcList.contains(tempRfc)) {
							rfcList.add(tempRfc);
						}
					}
				}

				System.out.println("Response Message:\n\n"+response+"\n");

				toRS.close();
				fromRS.close();
				client.close();
			} catch (Exception e) {
				System.out.println("Error Getting RFCIndex from peer at " + peer.toString() + "\n" + e);
			}

		}
	}

	public static void getRFC() {
		List<RFCNode> modifiedList = new LinkedList<Peer.RFCNode>();

		Iterator<RFCNode> it = rfcList.iterator();

		while (it.hasNext()) {

			RFCNode rfc = it.next();

			if (!existingFiles.contains(rfc.rfcNumber + "")) {

				try {
					System.out
							.println("Getting RFC" + rfc.rfcNumber + " from peer at " + rfc.hostname + ":" + rfc.port);

					Socket client = new Socket(rfc.hostname, rfc.port);

					ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

					ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

					toRS.writeObject("GET RFC\n" + rfc.rfcNumber);

					String response = fromRS.readObject().toString();

					if (!response.startsWith("BAD_REQUEST")) {

						try {
							IOUtils.write(response,
									new FileOutputStream(new File(
											Paths.get(rfcFolderPath + "/" + rfc.rfcNumber + ".txt").toString())),
									"UTF-8");
							existingFiles.add("" + rfc.rfcNumber);

							modifiedList.add(new RFCNode(rfc.rfcNumber, InetAddress.getLocalHost().toString(), myPort));
						} catch (Exception e) {
							System.out.println("Couldn't save file " + rfc.rfcNumber + ".txt:\n" + e);
						}
					}

					System.out.println("Response Message:\n\n"+response+"\n");

					toRS.close();
					fromRS.close();
					client.close();
				} catch (Exception e) {
					System.out.println("Error getting RFC" + rfc.rfcNumber + "  from peer at " + rfc.hostname + ":"
							+ rfc.port + "\n" + e);
				}
			}

		}

		rfcList.addAll(modifiedList);

	}

	private static void updateMyRFCList() throws NumberFormatException, UnknownHostException {
		File folder = new File(Paths.get(rfcFolderPath).toString());

		//System.out.println(folder.isDirectory());

		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//System.out.println(listOfFiles[i].getName());
				//System.out.println(listOfFiles[i].getName().split(".txt")[0]);
				rfcList.add(new RFCNode(Integer.parseInt(listOfFiles[i].getName().split(".txt")[0].trim()),
						InetAddress.getLocalHost().getHostAddress(), myPort));
				existingFiles.add(listOfFiles[i].getName().split(".txt")[0]);

			}
		}
	}

	private static class PeerInfo {
		public String hostName;

		public int port;

		public PeerInfo(String hostName, int port) {

			this.hostName = hostName;
			this.port = port;
		}

		@Override
		public String toString() {
			return this.hostName + ":" + this.port;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof PeerInfo)) {
				return false;
			}

			PeerInfo temp = (PeerInfo) obj;

			if (StringUtils.equals(this.hostName, temp.hostName) && this.port == temp.port) {
				return true;
			} else {
				return false;
			}
		}

	}

	public static class RFCNode {

		public int rfcNumber;
		public String hostname;
		public int port;
		public int ttl;

		public RFCNode(int rfcNumber, String hostname, int port) {
			this.rfcNumber = rfcNumber;
			this.hostname = hostname;
			this.port = port;
			this.ttl = 7200;
		}

		@Override
		public String toString() {
			return "" + rfcNumber + " " + hostname + " " + port + " " + ttl;
		}

		@Override
		public boolean equals(Object obj) {

			if (!(obj instanceof RFCNode)) {
				return false;
			}
			RFCNode temp = (RFCNode) obj;

			if (StringUtils.equals(this.hostname, temp.hostname) && this.rfcNumber == temp.rfcNumber
					&& this.port == temp.port) {
				return true;
			} else {
				return false;
			}

		}

	}

	public static void destroyRFCServer() {

		try {
			Socket client = new Socket(LOCALHOST, myPort);
			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());
			toRS.close();
			toRS.writeObject("Destroy");
			client.close();
		} catch (Exception e) {
			//System.out.println("Error destroying server" + e);
		}
		System.exit(0);
	}

	public static void scheduleTask() {
		timer = new Timer("timer");

		TimerTask task = new CustomTimerTask();

		timer.scheduleAtFixedRate(task, 0, 60000);

	}

	private static class CustomTimerTask extends TimerTask {

		public CustomTimerTask() {

		}

		@Override
		public void run() {

			// System.out.println("Inside Timer");

			for (RFCNode p : rfcList) {

				if (p.hostname.equals(LOCALHOST) && p.port == myPort) {
					continue;
				}
				// System.out.println("Before" + p.toString());

				p.ttl = p.ttl >= 60 ? p.ttl - 60 : 0;

				// System.out.println("After" + p.toString());

			}
		}
	}

}