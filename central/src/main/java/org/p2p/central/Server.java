package org.p2p.central;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

	private static final int TTL_VAL = 7200;

	private ServerSocket serverSocket;

	private Map<Integer, Peer> peers;

	private int cookieIndex;

	public Server() throws NumberFormatException, IOException {

		serverSocket = new ServerSocket(65423);

		peers = new ConcurrentHashMap<Integer, Peer>();

		this.cookieIndex = 0;
	}

	public synchronized int getCookieIndex() {

		this.cookieIndex += 1;
		return cookieIndex;
	}

	public void setCookieIndex(int cookieIndex) {
		this.cookieIndex = cookieIndex;
	}

	public Map<Integer, Peer> getPeers() {
		return peers;
	}

	public void setPeers(Map<Integer, Peer> peers) {
		this.peers = peers;
	}

	public void addPeer(Integer cookie, Peer peer) {
		this.peers.put(cookie, peer);
	}

	public void start() throws IOException {

		System.out.println("RS server is up and running");

		scheduleTask();

		while (true) {
			new RequestHandler(serverSocket.accept(), this).run();
		}
	}

	public void stop() throws IOException {
		serverSocket.close();
	}

	private static class RequestHandler extends Thread {

		private Socket clientSocket;

		private Server server;

		public RequestHandler(Socket socket, Server server) {
			this.clientSocket = socket;
			this.server = server;

		}

		public void run() {

			try {
				ObjectInputStream is = new ObjectInputStream(clientSocket.getInputStream());

				ObjectOutputStream os = new ObjectOutputStream(clientSocket.getOutputStream());
				String requestmessage = is.readObject().toString();
				String[] request = requestmessage.split("\n");

				System.out.println("Request Message:\n\n" + requestmessage+"\n");

				if (request[0].startsWith("REGISTER")) {
					os.writeObject(fulfillRegisterRequest(request));
				} else if (request[0].startsWith("LEAVE")) {
					os.writeObject(fulfillLeaveRequest(request));
				} else if (request[0].startsWith("KEEP_ALIVE")) {
					os.writeObject(fulfillKeepAliveRequest(request));
				} else if (request[0].startsWith("GET")) {
					os.writeObject(fulfillKeepPQueryRequest(request));
				} else {
					os.writeObject("BAD_REQUEST\nCannot Fulfill Request");
				}

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

		public String fulfillRegisterRequest(String[] request) {
			//System.out.println("Register request");
			try {

				int portNumber = Integer.parseInt(request[2].split(" ")[1]);

				String hostName = request[1].split(" ")[1];

				for (Entry<Integer, Peer> p : this.server.peers.entrySet()) {
					if (p.getValue().hostname.equals(hostName) && p.getValue().portNumber == portNumber) {

						//System.out.println("Before" + p.getValue().toString());
						p.getValue().isActive = true;
						p.getValue().ttl = TTL_VAL;
						p.getValue().registrationCount += 1;
						p.getValue().lastregisteredDate = new Date();
						//System.out.println("After" + this.server.peers.get(p.getKey()).toString());
						return "REGISTERED\nCookie: " + p.getKey();
					}
				}

				int cookie = this.server.getCookieIndex();

				this.server.peers.put(cookie, new Peer(hostName, cookie, portNumber));
				return "REGISTERED\nCookie: " + cookie;

			} catch (Exception e) {
				return "BAD_REQUEST";
			}
		}

		public String fulfillLeaveRequest(String[] request) {
			//System.out.println("Leave Request");
			try {
				int cookie = Integer.parseInt(request[1].split(" ")[1]);

				if (!this.server.peers.containsKey(cookie)) {
					return "BAD_REQUEST\nNot Registered";
				}

				//System.out.println("Before: " + this.server.peers.get(cookie).isActive);
				this.server.peers.get(cookie).isActive = false;
				//System.out.println("After: " + this.server.peers.get(cookie).isActive);
				return "OK";
			} catch (Exception e) {
				return "BAD_REQUEST";
			}

		}

		public String fulfillKeepAliveRequest(String[] request) {
			//System.out.println("Keep Alive Request");
			try {
				int cookie = Integer.parseInt(request[1].split(" ")[1]);

				if (!this.server.peers.containsKey(cookie)) {
					return "BAD_REQUEST\nNot Registered";
				}

				if (checkIfPeerAlive(cookie)) {
					//System.out.println("Before:" + this.server.peers.get(cookie).ttl);

					this.server.peers.get(cookie).ttl = TTL_VAL;

					//System.out.println("After:" + this.server.peers.get(cookie).ttl);
					return "OK";
				} else {
					return "BAD_REQUEST\nREGISTER AGAIN";
				}

			} catch (Exception e) {
				return "BAD_REQUEST";
			}

		}

		public String fulfillKeepPQueryRequest(String[] request) {
			//System.out.println("PQuery Request");
			try {
				int cookie = Integer.parseInt(request[1].split(" ")[1]);

				if (!this.server.peers.containsKey(cookie)) {
					return "BAD_REQUEST\nNot Registered";
				}

				if (checkIfPeerAlive(cookie)) {
					this.server.peers.get(cookie).ttl = TTL_VAL;

					StringBuilder sb = new StringBuilder();

					sb.append("OK");

					for (Entry<Integer, Peer> peer : this.server.peers.entrySet()) {
						if (peer.getValue().isActive) {
							sb.append("\n" + peer.getValue().hostname + " " + peer.getValue().portNumber);
						}
					}

					return sb.toString();
				} else {
					return "BAD_REQUEST\nREGISTER AGAIN";
				}

			} catch (Exception e) {
				System.out.println(e.getMessage());
				return "BAD_REQUEST\nCannot Fulfill";
			}

		}

		public boolean checkIfPeerAlive(int cookie) {
			return this.server.peers.get(cookie).isActive;
		}

	}

	public void scheduleTask() {
		Timer timer = new Timer("timer");

		TimerTask task = new CustomTimerTask(this.peers);

		timer.scheduleAtFixedRate(task, 0, 60000);

	}

	private static class CustomTimerTask extends TimerTask {

		private Map<Integer, Peer> peers;

		public CustomTimerTask(Map<Integer, Peer> peers) {
			this.peers = peers;
		}

		@Override
		public void run() {

			// System.out.println("Inside Timer");

			for (Entry<Integer, Peer> p : this.peers.entrySet()) {
				Peer peer = p.getValue();
				// System.out.println("Before" + p.getValue().toString());
				peer.ttl = peer.ttl >= 60 ? peer.ttl - 60 : 0;
				if (peer.ttl <= 0) {
					peer.isActive = false;
				}

				// System.out.println("After" +
				// this.peers.get(p.getKey()).toString());

			}
		}
	}

}
