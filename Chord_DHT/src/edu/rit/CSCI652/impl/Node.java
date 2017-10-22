package edu.rit.CSCI652.impl;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by payalkothari on 10/18/17.
 */


public class Node implements Serializable{

    private InetAddress ip;
    private int GUID ;
    private int port;

//    @JsonDeserialize(using = CustomDeserializer.class)
//    @JsonBackReference

//    @JsonIgnoreProperties("succ")
    private List<FingerTableEntry> fingerTable;

    public List<FingerTableEntry> getFingerTable() {
        return fingerTable;
    }

    public void setFingerTable(List<FingerTableEntry> fingerTable) {
        this.fingerTable = fingerTable;
    }

    public int getGUID() {
        return GUID;
    }

    public void setGUID(int GUID) {
        this.GUID = GUID;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Node(){

    }

    public Node(int GUID, InetAddress ip, int port){
        this.GUID = GUID;
        this.ip = ip;
        this.port = port;
    }
}
