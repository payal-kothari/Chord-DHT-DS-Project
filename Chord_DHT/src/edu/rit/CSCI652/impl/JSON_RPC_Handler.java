package edu.rit.CSCI652.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sun.corba.se.impl.orbutil.ObjectWriter;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.server.MessageContext;
import com.thetransactioncompany.jsonrpc2.server.RequestHandler;
import com.thetransactioncompany.jsonrpc2.util.NamedParamsRetriever;
import net.minidev.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by payalkothari on 10/19/17.
 */
public class JSON_RPC_Handler {


    public static class RPC_Calls_Handler implements RequestHandler {

        private static Node ownNode = new Node();
        private static List<FingerTableEntry> fingerTable = new ArrayList<>();
        private static int maxFingerTableSize = 0;


        public RPC_Calls_Handler(Node ownNode, List<FingerTableEntry> ft, int maxFtSize ){
            this.ownNode = ownNode;
            this.fingerTable = ft;
            this.maxFingerTableSize = maxFtSize;
        }


        @Override
        public String[] handledRequests() {
            return new String[] {"get_closest_preceding_finger"};
        }

        @Override
        public JSONRPC2Response process(JSONRPC2Request jsonrpc2Request, MessageContext messageContext) {
            if (jsonrpc2Request.getMethod().equals("get_closest_preceding_finger")) {
                System.out.println("till here **********");
                Map paramMap = (Map) jsonrpc2Request.getNamedParams();
                NamedParamsRetriever np = new NamedParamsRetriever(paramMap);
                int id = 0;
                try {
                    id = np.getInt("id");
                } catch (JSONRPC2Error jsonrpc2Error) {
                    jsonrpc2Error.printStackTrace();
                }
                Node responseNode =  get_closest_preceding_finger(id);

                String resposeNodeStr = null;
                try {
                    resposeNodeStr = new ObjectMapper().writeValueAsString(responseNode);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                System.out.println("check******** " + resposeNodeStr);

                return new JSONRPC2Response(resposeNodeStr, jsonrpc2Request.getID());
            }
            return null;
        }



        public static Node get_closest_preceding_finger(int id){
            int n = ownNode.getGUID();

            for(int i = maxFingerTableSize-1; i >= 0; i--){
                Node ans = fingerTable.get(i).getSucc();
                int ansID = ans.getGUID();
                if((n < id && ansID > n && ansID < id)
                        || (n > id && ansID > n && ansID > 0)
                        || (n > id && ansID >= 0 && ansID < id)){

                    return fingerTable.get(i).getSucc();

                }
            }

            return ownNode;
        }
    }
}
