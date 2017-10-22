package edu.rit.CSCI652.impl;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by payalkothari on 10/21/17.
 */
public class incomingNotifHandler extends Thread{

    private static Node newNode;

    public void run(){
        try {
        while (true){
                Socket socket = ClientNode.getServerSocket().accept();
                ObjectInputStream objectInStream = new ObjectInputStream(socket.getInputStream());
                String in = objectInStream.readUTF();
                switch (in){
                    case "New Node" :
                        System.out.println("******  New node has joined the network, updating the values......");
                        newNode = (Node) objectInStream.readObject();

                        ClientNode.setSuccessor(find_successor(ClientNode.getOwnGUID(), ClientNode.getSuccessor()));
                        ClientNode.setPredecessor(find_predecessor(ClientNode.getOwnGUID(), ClientNode.getPredecessor()));

                        update_fingerTable();
                }


            }

        }catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void update_fingerTable() {

        List<FingerTableEntry> fingerTable =  ClientNode.getFingerTable();
        int tableSize = ClientNode.getMaxFingerTableSize();

        for (int i = 0; i < tableSize; i++){
            FingerTableEntry entry = fingerTable.get(i);
            int start = entry.getStart();
            entry.setSucc(find_successor(start, entry.getSucc()));
        }
    }

    private Node find_successor(int GUID, Node oldSucc) {

        int newNodeGUID = newNode.getGUID();
        int succGUID = oldSucc.getGUID();

        int max = Math.max(newNodeGUID, succGUID);
        int min = Math.min(newNodeGUID, succGUID);

        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(min);
        arr.add(max);

        int successor = GUID;

        for(int i = 0; i < 2;i++){
            int temp = arr.get(i);
            if(temp > successor){
                successor = temp;
                break;
            }
        }

        if (successor == GUID){
            successor = min;
        }

        if(successor == newNodeGUID){
            return newNode;
        }

        return oldSucc;
    }

    private Node find_predecessor(int GUID, Node oldPred) {

        int newNodeGUID = newNode.getGUID();
        int predGUID = oldPred.getGUID();

        int max = Math.max(newNodeGUID, predGUID);
        int min = Math.min(newNodeGUID, predGUID);

        ArrayList<Integer> arr = new ArrayList<>();
        arr.add(max);
        arr.add(min);

        int predecessor = GUID;

        for(int i = 0; i < 2;i++){
            int temp = arr.get(i);
            if(temp < predecessor){
                predecessor = temp;
                break;
            }
        }

        if (predecessor == GUID){
            predecessor = max;
        }

        if(predecessor == newNodeGUID){
            return newNode;
        }

        return oldPred;
    }


}
