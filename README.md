# P2PFileTransferJava
A P2P file transfer system based on Java


## How to run?

This sproject has two maven projects.

The central project has the code for centralized server.

The peer project has the code for peer servers.

First build both the projects using "mvn package"

Then go to central/target and run 

``` java -jar central-0.0.1-jar-with-dependencies.jar ```

The go to peer/targer and run

``` java -jar peer-0.0.1-jar-with-dependencies.jar ```


Run both of them in different consoles.

You should see an acknowledgement in peer console.