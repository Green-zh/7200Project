package blockchain;

import blockchain.services.BlockService;
import blockchain.services.CommunicationService;
import blockchain.services.TransactionService;
import com.google.gson.Gson;

import java.util.Scanner;

public class Controller {

    BlockService blockService = new BlockService();
    TransactionService transactionService = new TransactionService();
    CommunicationService communicationService = new CommunicationService();

    private void getLastBlock() {

        System.out.println("The information about the last block:");
        System.out.println(blockService.getLastBlockJson());
    }

    private void mining(String blockData) {

        String newBlock = new Gson().toJson(blockService.generateNextBlock(blockData));
        System.out.println(newBlock);
        String newTransaction = new Gson().toJson(transactionService.createTransaction(0.0, true));
        System.out.println(newTransaction);
        communicationService.broadcast(newBlock);
    }

    private void connect(String address) {

        System.out.println("The last block from 127.0.0.1:" + address + " is:");
        System.out.println(communicationService.queryLastBlock(Integer.parseInt(address)));
    }

    public static void main(String[] args) {

        Controller controller = new Controller();
        Scanner scanner = new Scanner(System.in);

        Thread serverThread = new Thread(){
            public synchronized void run(){
                CommunicationService communicationService = new CommunicationService();
                communicationService.startServer();
            }
        };
        serverThread.start();

        while(scanner.hasNextLine()) {
            String op = scanner.nextLine();
            switch (op) {
                case "getLastBlock":
                    controller.getLastBlock();
                    break;
                case "mining":
                    System.out.println("Input your data:");
                    String data = scanner.nextLine();
                    controller.mining(data);
                    break;
                case "connect":
                    System.out.println("Input Address(Port):");
                    String address = scanner.nextLine();
                    controller.connect(address);
                    break;
            }
        }

    }
}
