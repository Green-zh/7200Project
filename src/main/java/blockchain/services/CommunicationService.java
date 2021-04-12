package blockchain.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class CommunicationService {

    private Integer myPortNumber;
    private List<Integer> nodes;

    public CommunicationService() {
        //Initialize
        try {
            File nodesFile = new File("src/main/resources/nodes.txt");
            Scanner scanner = new Scanner(nodesFile);
            String jsonString = "";
            while(scanner.hasNextLine()) {
                jsonString += scanner.nextLine();
            }
            nodes = new Gson().fromJson(jsonString, new TypeToken<List<Integer>>(){}.getType());
            this.setAddress(nodes.get(0));
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Integer setAddress(Integer portNumber) {

        this.myPortNumber = portNumber;
        return this.myPortNumber;
    }

    public void startServer() {

        try {
            ServerSocket serverSocket = new ServerSocket(myPortNumber);
            System.out.println("Server with Address(Port) " + myPortNumber + " created.");
            Socket clientSocket;
            while(true){
                try{
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected.");
                    PrintStream out = new PrintStream(clientSocket.getOutputStream());
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String op = in.readLine();
                    switch(op) {
                        case "1": //query latest block
                            out.println(getLastBlock());
                            break;
                        case "2": //broadcast new block
                            String newBlockJson = in.readLine();
                            out.println(addNewBlock(newBlockJson));
                            break;
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String connectToServer(Integer portNumber, String op, String msg) {

        if(!nodes.contains(portNumber)) {
            nodes.add(portNumber);
            writeDown();
        }
        String ret = "";
        try {
            Socket socket = new Socket("localhost", portNumber);
            if(op.equals("2")) {
                System.out.println("127.0.0.1:" + portNumber);
            }
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.writeBytes(op + "\n" + msg + "\n");
            ret = in.readLine();
            out.close();
            in.close();
        } catch (UnknownHostException e) {
//            e.printStackTrace();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return ret;
    }

    public String queryLastBlock(Integer portNumber) {

        String ret = connectToServer(portNumber, "1", getLastBlock());
        if(ret.equals("")) {
            ret = "Cannot to connect to the target node.";
        }
        return ret;
    }

    public void broadcast(String newBlockJson) {

        System.out.println("The new Block has been broadcast to: ");
        for(Integer node : nodes) {
            if(node != myPortNumber) {
                connectToServer(node, "2", newBlockJson);
            }
        }
    }

    private String getLastBlock() {

        String ret;
        BlockService blockService = new BlockService();
        ret = blockService.getLastBlockJson();
        return ret;
    }

    private String addNewBlock(String newBlockJson) {

        String ret;
        BlockService blockService = new BlockService();
        ret = blockService.addNewBlockByBroadcast(newBlockJson);
        return ret;
    }

    private void writeDown() {

        try {
            FileWriter writer = new FileWriter("src/main/resources/nodes.txt");
            String jsonString = new Gson().toJson(nodes);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        CommunicationService c = new CommunicationService();
//        c.startServer();
        c.writeDown();
    }
}