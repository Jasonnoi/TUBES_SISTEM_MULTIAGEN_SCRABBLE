package Scrabble;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class PlayerAgent extends Agent {
    private boolean myTurn = false; // Menentukan apakah ini giliran pemain

    protected void setup() {
        System.out.println(getLocalName() + " is ready.");
        addBehaviour(new PlayerTurn());
    }

    private class PlayerTurn extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                if (content.equals("TURN")) {
                    myTurn = true;
                    System.out.println(getLocalName() + ": It's my turn!");
                } else if (content.equals("END_TURN")) {
                    myTurn = false;
                } else if (myTurn && content.startsWith("WORD")) {
                    // Kirim kata ke ValidatorAgent
                    ACLMessage wordMsg = new ACLMessage(ACLMessage.REQUEST);
                    wordMsg.addReceiver(
                            new jade.core.AID("ValidatorAgent", jade.core.AID.ISLOCALNAME));
                    wordMsg.setContent(content);
                    send(wordMsg);

                    // Selesai bermain, beri tahu Referee
                    ACLMessage endTurnMsg = new ACLMessage(ACLMessage.INFORM);
                    endTurnMsg.addReceiver(
                            new jade.core.AID("RefereeAgent", jade.core.AID.ISLOCALNAME));
                    endTurnMsg.setContent("END_TURN");
                    send(endTurnMsg);

                    myTurn = false;
                }
            } else {
                block();
            }
        }
    }
}
