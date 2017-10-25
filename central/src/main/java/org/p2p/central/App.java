package org.p2p.central;

import java.io.IOException;


public class App {

	public static void main(String[] args) throws IOException {
		Server server = new Server();

		server.start();
	}
}
