package org.p2p.peer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

import org.apache.commons.io.IOUtils;
import org.p2p.peer.Peer.RFCNode;

public class RFCServer extends Thread {

	private ServerSocket serverSocket;

	public boolean stopServer;

	public RFCServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.stopServer = false;
	}

	public void run() {
		//System.out.println("Started RFCServer");

		try {
			while (!stopServer) {
				new RequestHandler(this.serverSocket.accept()).run();
				;
			}
			this.serverSocket.close();
		} catch (Exception e) {
			System.out.println("RFC server is closed due to:\n" + e);
		}

		System.out.println("Stopped RFCServer");
	}

	private static class RequestHandler extends Thread {

		private Socket clientSocket;

		public RequestHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void run() {
			try {

				// System.out.println("fulfill");
				ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());

				ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());

				String request = is.readObject().toString();

				String[] requestArr = request.split("\n");

				System.out.println("Request:\n\n" + request);

				if (requestArr[0].startsWith("GET RFC-Index")) {
					os.writeObject(fulfillRFCIndexRequest(requestArr));
				} else if (requestArr[0].startsWith("GET RFC")) {
					os.writeObject(fulfillRFCRequest(requestArr));

				} else {
					os.writeObject("BAD_REQUEST\nCannot Fulfill Request");
				}
			} catch (Exception e) {
				System.out.println("Exception occured" + e);

			} finally {
				try {
					this.clientSocket.close();
				} catch (IOException e) {
					System.out.println("Cannot close socket");
				}
			}
		}

		public String fulfillRFCIndexRequest(String[] request) {

			// System.out.println("fulfill RFCIndexRequest");
			try {
				StringBuilder response = new StringBuilder("OK");

				for (RFCNode rfc : Peer.rfcList) {
					if (rfc.ttl > 0) {
						response.append("\n" + rfc.toString());
					}
				}

				return response.toString();
			} catch (Exception e) {
				return "BAD_REQUEST";
			}

		}

		public String fulfillRFCRequest(String[] request) {

			// System.out.println("fulfill RFCIndexRequest");
			try {

				int rfcIndex = Integer.parseInt(request[1]);

				File rfcFile = new File(Paths.get(Peer.rfcFolderPath + "/" + rfcIndex + ".txt").toString());

				if (rfcFile.exists()) {
					FileInputStream fis = new FileInputStream(rfcFile);
					String response = IOUtils.toString(fis, "UTF-8");
					fis.close();
					return response;

				} else {
					return "404/nFile not found";
				}
			} catch (Exception e) {
				return "BAD_REQUEST";
			}

		}

	}
}
