package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class RefereeAgent extends Agent {
    private int currentPlayer = 0; // Giliran pemain (0: Player1, 1: Player2)
    private int[] scores = {0, 0}; // Skor dua pemain
    private int turnCount = 0; // Hitung jumlah giliran
    private final int MAX_TURNS = 50; // Batas permainan

    protected void setup() {
        System.out.println("RefereeAgent " + getLocalName() + " started.");
        addBehaviour(new ManageTurns());
    }

    private class ManageTurns extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String content = msg.getContent();
                if (content.startsWith("SCORE")) {
                    String[] parts = content.split(" ");
                    int points = Integer.parseInt(parts[1]);
                    scores[currentPlayer] += points;
                    System.out
                            .println("Player " + currentPlayer + " scored " + points + " points.");
                } else if (content.equals("END_TURN")) {
                    // Pindahkan giliran ke pemain berikutnya
                    currentPlayer = (currentPlayer + 1) % 2;
                    turnCount++;

                    // Periksa apakah permainan selesai
                    if (turnCount >= MAX_TURNS) {
                        announceWinner();
                        doDelete();
                    } else {
                        // Kirim notifikasi giliran ke pemain berikutnya
                        ACLMessage nextTurn = new ACLMessage(ACLMessage.INFORM);
                        nextTurn.setContent("TURN");
                        String nextPlayer = currentPlayer == 0 ? "Player1" : "Player2";
                        nextTurn.addReceiver(
                                new jade.core.AID(nextPlayer, jade.core.AID.ISLOCALNAME));
                        send(nextTurn);
                    }
                }
            } else {
                block();
            }
        }

        private void announceWinner() {
            System.out.println("Game Over!");
            if (scores[0] > scores[1]) {
                System.out.println("Player1 wins with " + scores[0] + " points!");
            } else if (scores[1] > scores[0]) {
                System.out.println("Player2 wins with " + scores[1] + " points!");
            } else {
                System.out.println("It's a tie!");
            }
        }
    }
}
