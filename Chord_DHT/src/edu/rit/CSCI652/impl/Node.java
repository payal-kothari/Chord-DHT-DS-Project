package edu.rit.CSCI652.impl;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

/**
 * Created by payalkothari on 10/18/17.
 */
public class Node implements Serializable{
    private int GUID ;
    private String ip;
    private int port;
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
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

    public Node(int GUID, String ip, int port){
        this.GUID = GUID;
        this.ip = ip;
        this.port = port;
    }
}
