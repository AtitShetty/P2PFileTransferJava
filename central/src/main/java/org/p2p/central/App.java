package org.p2p.central;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Hello world!
 *
 */
public class App 
{

	private static ServerSocket serverSocket;

	public static void main(String[] args) throws IOException
    {
        System.out.println( "Hello World!" );

		start(65423);
    }

	public static void start(int port) throws IOException {

		serverSocket = new ServerSocket(port);

		while (true) {
			new RequestHandler(serverSocket.accept()).run();
		}
	}

	public void stop() throws IOException {
		serverSocket.close();
	}

	private static class RequestHandler extends Thread {

		private Socket clientSocket;

		public RequestHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {

			try {
				ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());

				ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());

				String input = (String) is.readObject();

				System.out.println("Input from peer" + input);

				os.writeObject("Received input:" + input);

			} catch (Exception e) {
				System.out.println("cannot fulfil request" + e.getMessage());
			} finally {
				try {
					this.clientSocket.close();
				} catch (IOException e) {
					System.out.println("Cannot close socket");
				}
			}
		}
	}
}
