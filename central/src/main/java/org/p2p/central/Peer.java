package org.p2p.central;

import java.util.Date;

public class Peer {
	public Peer(String hostName, int cookie, int portNumber) {
		this.hostname = hostName;
		this.cookie = cookie;
		this.portNumber = portNumber;
		this.isActive = true;
		this.registrationCount = 1;
		this.lastregisteredDate = new Date();
		this.ttl = 7200;
	}

	public String hostname;

	public int cookie;

	public boolean isActive;

	public int ttl;

	public int portNumber;

	public int registrationCount;

	public Date lastregisteredDate;

	@Override
	public String toString() {
		return this.hostname + " " + this.portNumber + " " + this.cookie + " " + this.isActive + " "
				+ this.registrationCount + " " + this.lastregisteredDate + " " + this.ttl;
	}

}
