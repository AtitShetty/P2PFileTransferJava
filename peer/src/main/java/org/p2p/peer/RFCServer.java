package org.p2p.peer;

import java.io.IOException;
import java.net.ServerSocket;

public class RFCServer {
	private static final int TTL_VAL = 7200;

	private ServerSocket serverSocket;

	private int port;

	public RFCServer(int port) throws IOException {
		this.serverSocket = new ServerSocket(port);
		this.port = port;
	}

	public void start() {

		while (true) {
			new RequestHandler(this);
		}
	}

	private static class RequestHandler extends Thread {

		private RFCServer server;

		public RequestHandler(RFCServer server) {
			this.server = server;
		}

		public void run() {
			System.out.println("Got a request");
		}

	}
}
