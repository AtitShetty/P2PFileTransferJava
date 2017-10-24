package org.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class Peer {
	int cookie;
	
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException {

		/*
		 * System.out.println("Hello World!");
		 * 
		 * Socket client = new Socket(InetAddress.getLocalHost(), 65423);
		 * 
		 * ObjectOutputStream os = new
		 * ObjectOutputStream(client.getOutputStream());
		 * 
		 * ObjectInputStream is = new
		 * ObjectInputStream(client.getInputStream());
		 * 
		 * os.writeObject("My Name is Anthony");
		 * 
		 * System.out.println((String) is.readObject());
		 * 
		 * client.close();
		 */

		Peer p = new Peer();
		Scanner sc = new Scanner(System.in);
		while (true) {
			System.out.println(
					"1.Register\n" + "2.Leave\n" + "3.PQuery \n" + "4.Keep Alive\n" + "5.RFCQuery \n" + "6.GetRFC \n");
			int choice = sc.nextInt();
			switch (choice) {
			case 1:p.register();
				break;
			case 2:p.leave();
			default:
				break;
			}
			break;
		}
		sc.close();
	}

	void register() {
		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);
			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());
			ObjectInputStream fromRS = new ObjectInputStream(client.getInputStream());
			toRS.writeObject("Register");
			cookie = Integer.parseInt(fromRS.readObject().toString());
			toRS.close();
			fromRS.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void leave() {
		try {

			Socket client = new Socket(InetAddress.getLocalHost(), 65423);
			ObjectOutputStream toRS = new ObjectOutputStream(client.getOutputStream());
			toRS.writeObject("Leave"+cookie);
			toRS.close();
			client.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}