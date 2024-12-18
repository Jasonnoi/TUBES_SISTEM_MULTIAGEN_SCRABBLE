package Scrabble;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class RefereeAgent extends Agent {
    protected void setup() {
        System.out.println("RefereeAgent " + getLocalName() + " started.");
        initializeBoard(); // Langsung panggil metode untuk menginisialisasi papan
    }

    private void initializeBoard() {
        // Kirim pesan ke BoardAgent untuk menginisialisasi papan
        ACLMessage initMsg = new ACLMessage(ACLMessage.REQUEST);
        initMsg.addReceiver(new jade.core.AID("BoardAgent", jade.core.AID.ISLOCALNAME));
        initMsg.setContent("INITIALIZE_BOARD");
        System.out.println("Sending INITIALIZE_BOARD message to BoardAgent.");
        send(initMsg);
    }
}
