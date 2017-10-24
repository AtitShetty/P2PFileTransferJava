package org.p2p.peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Hello world!
 *
 */
public class App 
{
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
    {
        System.out.println( "Hello World!" );

		Socket client = new Socket(InetAddress.getLocalHost(), 65423);

		ObjectOutputStream os = new ObjectOutputStream(client.getOutputStream());

		ObjectInputStream is = new ObjectInputStream(client.getInputStream());

		os.writeObject("My Name is Anthony");

		System.out.println((String) is.readObject());

		client.close();
    }
}
