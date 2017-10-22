package edu.rit.CSCI652.impl;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.Dispatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by payalkothari on 10/19/17.
 */
public class JSON_RPC_Subclass extends Thread{
    private String name;
    private Socket socket;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private Dispatcher dispatcher;
    private static Node ownNode = new Node();
    private static List<FingerTableEntry> fingerTable = new ArrayList<>();
    private static int maxFingerTableSize = 0;

    public JSON_RPC_Subclass(Node ownNode, List<FingerTableEntry> ft, int maxFtSize ) {

        this.ownNode = ownNode;
        this.fingerTable = ft;
        this.maxFingerTableSize = maxFtSize;
    }


public void run(){

            try {
                while (true) {
                    System.out.println("waiting for connection");
                    Socket socket = ClientNode.getServerSocket().accept();
                    System.out.println("Received RPC request");
                    new RPC_SubClass_two(socket, ownNode, fingerTable, maxFingerTableSize).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

}

    private static class RPC_SubClass_two extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader bufferReader;
        private PrintWriter printWriter;
        private Dispatcher dispatcher;
        private static Node ownNode = new Node();
        private static List<FingerTableEntry> fingerTable = new ArrayList<>();
        private static int maxFingerTableSize = 0;

        public RPC_SubClass_two(Socket socket, Node ownNode, List<FingerTableEntry> ft, int maxFtSize) {
            this.socket = socket;
            this.ownNode = ownNode;
            this.fingerTable = ft;
            this.maxFingerTableSize = maxFtSize;

            this.dispatcher =  new Dispatcher();
            dispatcher.register(new JSON_RPC_Handler.RPC_Calls_Handler(ownNode, fingerTable, maxFingerTableSize));


        }

        public void run() {
            try {
                System.out.println("IN JSON_RPC_SUbCLASS");
                bufferReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                String line = bufferReader.readLine();
                boolean post = line.startsWith("POST");
                int contentLen = 0;
                String req = "";
                String temp = "" + line;
                line = bufferReader.readLine();
                while (!line.equals("")) {
                    temp = temp + '\n' + line;
                    if (post == true) {
                        if (line.startsWith("Content-Length: ")) {
                            contentLen = Integer.parseInt(line.substring("Content-Length: ".length()));
                        }
                    }
                    line = bufferReader.readLine();
                }

                int ch = 0;
                if (post == true) {
                    for (int i = 0; i < contentLen; i++) {
                        ch = bufferReader.read();
                        req = req + (char) ch;
                    }
                }

                System.out.println("body " + req);
                JSONRPC2Request request = JSONRPC2Request.parse(req);

                JSONRPC2Response resp = dispatcher.process(request, null);

                System.out.println(resp.toString());
                printWriter.write("HTTP/1.1 200 OK\r\n");
                printWriter.write("Content-Type: application/json\r\n");
                printWriter.write("\r\n");
                System.out.println("return string : " + resp.toJSONString());
                printWriter.write(resp.toJSONString());
                printWriter.flush();
                printWriter.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONRPC2ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
