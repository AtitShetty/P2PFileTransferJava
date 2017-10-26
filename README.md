# P2P File Transfer Protocol using Java

A P2P file transfer system based on Java.

## About

The P2P system consists of two components.

A central server that will keep track of active peers.

Multiple peer servers that will query central server for a list of active peers and then interact with each other to transfer files.

Java sockets were used to create central and peer servers.

### Central Server

The central server will have a ServerSocket, that will listen on default port "65423". When it detects a new request, it will create a new Thread that will take care of the request and give appropriate response. This frees the server, so that it can keep listening for new requests.

The server maintains a list of servers that have been registered. A TTL counter will be set to 7200 seconds, after which the peer is set as inactive.

A peer can send a Keep Alive message to reset the TTL or register after TTL has expired.

The code for central server is located in "central" server.


### Peer

Each peer has a client which runs on main Thread and an RFC server that runs on separate Thread.

The peer will register first the RS server and then request a list of active peers.

It will then scan the list and ask every peer for their RFC index list. Once it receives the RFC indices, it will check the RFC in it's repository. It will then query the peer that has this RFC and dowload it.

If the peer cannot provide the RFC or connection is timeout, it will ask other peers, till it has exhausted the peer list.

## Design

The central and peer server are different maven projects.

In order to run the program, go to specific project and run 

```mvn package``` 

It will generate an executable jar inside "target" folder.

You can run the jar by following command:

```java -jar $jarname.jar```

e.g.

```jar -jar central-0.0.1-jar-with-dependencies```

```java -jar peer-0.0.1-jar-with-dependencies```

In order to create an out of box testing env, we have included a shell script 'buildPackages.sh'. 

This will perform 'mvn package'. Create a folder called as Testing, inside which it will create one central server and 6 peers. Each will have an executable jar in it.

The peers will have an RFC folder, where you can drop the RFC files.

To start a server, just execute above jar command.

## How to run?

Run buildPackges.sh which will create Testing env, or manually build mvn packages and run the executables inside target directory.

First start the central server.

Then start the peer servers.

For peer server you will need to provide, the port number and location of RFC folder.

You can maanually set the RS server hostname and port, however it has default configurations.

Reminder, both are executable jars. To create multiple instances, just run each jar in a new console.

The peer server is interactive with a menu that is self-descriptive.
