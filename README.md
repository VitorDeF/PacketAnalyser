# Java Packet Analyzer

A network traffic analyzer built with JavaFX and Pcap4J. This project was designed to capture, display and analyze real time network packets. Captures IPv4 real time traffic from active network interface (searching mainly for the Ethernet).

## Prerequisits
* Java 25
* Maven
* Wincap
* Windows
* Administrator privileges

## How to run
* Clone the repository: git clone https://github.com/your-username/PackageAnalyser.git
* Navigate to the project folder
* Run using Maven: mvn javafx:run

## Building the Executable
To generate a standalone "Fat JAR":
* mvn clean package
The executable will be located in the target/ folder.

# Technologies
* Java 25
* JavaFX
* Pcap4J
* Maven
