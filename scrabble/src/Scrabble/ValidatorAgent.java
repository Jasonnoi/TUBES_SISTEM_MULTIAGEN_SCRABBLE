package Scrabble;


import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.HashSet;

public class ValidatorAgent extends Agent {
    private HashSet<String> dictionary;
    private char[][] board = new char[15][15]; // Tambahkan variabel board

    protected void setup() {
        System.out.println("ValidatorAgent " + getLocalName() + " started.");
        dictionary = loadDictionary();
        initializeBoard(); // Inisialisasi papan kosong
        addBehaviour(new ValidateWord());
    }

    private HashSet<String> loadDictionary() {
        HashSet<String> words = new HashSet<>();
        words.add("HELLO");
        words.add("WORLD");
        words.add("JAVA");
        // Add more words
        return words;
    }

    private void initializeBoard() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j] = '.'; // '.' Menandakan sel kosong
            }
        }
    }

    private class ValidateWord extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                String[] parts = msg.getContent().split(" ");
                String word = parts[0];
                int row = Integer.parseInt(parts[1]);
                int col = Integer.parseInt(parts[2]);

                ACLMessage reply = msg.createReply();
                if (dictionary.contains(word) && isValidPlacement(word, row, col)) {
                    reply.setContent("VALID");
                    updateBoard(word, row, col); // Perbarui papan jika valid
                } else {
                    reply.setContent("INVALID");
                }
                send(reply);
            } else {
                block();
            }
        }
    }

    private boolean isValidPlacement(String word, int row, int col) {
        if (row < 0 || col < 0 || col + word.length() > 15) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (board[row][col + i] != '.') {
                return false; // Sel sudah terisi
            }
        }
        return true;
    }

    private void updateBoard(String word, int row, int col) {
        for (int i = 0; i < word.length(); i++) {
            board[row][col + i] = word.charAt(i);
        }
        System.out.println(
                "Board updated with word: " + word + " at position (" + row + "," + col + ")");
    }
}
