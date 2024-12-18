package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class BoardAgent extends Agent {
    private char[][] board = new char[15][15];

    protected void setup() {
        System.out.println("BoardAgent " + getLocalName() + " started.");
        initializeBoard();
        addBehaviour(new ManageBoard());
    }

    private void initializeBoard() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = '.';
            }
        }
    }

    private class ManageBoard extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] parts = msg.getContent().split(" ");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);
                char letter = parts[2].charAt(0);
                board[row][col] = letter;
                System.out.println("Updated board at (" + row + ", " + col + ") with " + letter);
            } else {
                block();
            }
        }
    }
}
