package org.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class Peer {
	private static int cookie;

	private static Integer myPort;

	private static RFCServer rfcServer;

	private static List<PeerInfo> peerInfo = new LinkedList<Peer.PeerInfo>();

	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		Scanner sc = new Scanner(System.in);

		System.out.println(myPort);

		System.out.println("Please enter a port number");

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

		// rfcServer.start();

		while (true) {
			
			boolean exit = false;

			System.out.println(
					"1.Register\n" + "2.Leave\n" + "3.PQuery \n" + "4.Keep Alive\n" + "5.RFCQuery \n" + "6.GetRFC \n"
							+ "7.Exit");
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
				// rfcQuery();
				break;
			case 6:
				getRFC();
				break;
			case 7:
				exit = true;
				break;
			default:
				break;
			}
			
			if(exit){
				break;
			}
		}
		sc.close();
	}

	public static void register() {
		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);
			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());
			toRS.writeObject("REGISTER\nHOST " + client.getInetAddress() + "\nPORT " + myPort);
			String res = fromRS.readObject().toString();
			System.out.println("Response is:\n" + res);

			if (res.startsWith("REGISTERED")) {
				Peer.cookie = Integer.parseInt(res.split("\n")[1].split(" ")[1]);
			}

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not register:\n" + e.getMessage());
		}
	}

	public static void leave() {
		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("LEAVE\ncookie " + cookie);

			System.out.println(fromRS.readObject().toString());

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not unregister:\n" + e.getMessage());
		}
	}

	public static void pQuery() {

		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("GET PEER_LIST\ncookie: " + cookie);

			String response = fromRS.readObject().toString();

			String[] respArr = response.split("\n");

			if (response.startsWith("OK")) {

				for (int i = 1; i < respArr.length; i++) {

					String arr[] = respArr[i].split(" ");

					peerInfo.add(new PeerInfo(arr[0], Integer.parseInt(arr[1])));
				}
			}

			System.out.println(response);

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Could not deregister:\n" + e.getMessage());
		}

	}

	public static void keepAlive() {

		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);

			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());

			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());

			toRS.writeObject("KEEP_ALIVE\ncookie " + cookie);

			System.out.println(fromRS.readObject().toString());

			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			System.out.println("Exception:\n" + e.getMessage());
		}

	}

	public static void getRFC() {

	}

	private static class PeerInfo {
		public String hostName;

		public int port;

		public PeerInfo(String hostName, int port) {

			this.hostName = hostName;
			this.port = port;
		}

	}
}