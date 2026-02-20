package org.example;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PacketInfo {
    private IntegerProperty count;
    private StringProperty source;
    private StringProperty destination;
    private StringProperty protocol;
    private IntegerProperty length;
    private StringProperty header;
    private StringProperty payload;
    private StringProperty timestamp;

    public PacketInfo(Integer count, String source, String destination, String protocol, Integer length, String header, String payload, String timestamp) {
        this.count = new SimpleIntegerProperty(count);
        this.source = new SimpleStringProperty(source);
        this.protocol = new SimpleStringProperty(protocol);
        this.length = new SimpleIntegerProperty(length);
        this.destination = new SimpleStringProperty(destination);
        this.header = new SimpleStringProperty(header);
        this.payload = new SimpleStringProperty(payload);
        this.timestamp = new SimpleStringProperty(timestamp);
    }

    public IntegerProperty countProperty() {return count;}
    public StringProperty sourceProperty() {return source;}
    public StringProperty protocolProperty() {return protocol;}
    public IntegerProperty lengthProperty() {return length;}
    public StringProperty destinationProperty() {return destination;}
    public StringProperty timestampProperty() {return timestamp;}

    public int getCount() {return count.get();}
    public String getSource() {return source.get();}
    public String getDestination() {return destination.get();}
    public String getProtocol() {return protocol.get();}
    public int getLength() {return length.get();}
    public String getHeader() {return header.get();}
    public String getPayload() {return payload.get();}
    public String getTimestamp() {return timestamp.get();}
}
