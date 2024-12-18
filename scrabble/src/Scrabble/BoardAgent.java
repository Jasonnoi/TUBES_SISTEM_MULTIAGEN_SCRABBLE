package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import java.awt.*;

public class BoardAgent extends Agent {
    private static final int BOARD_SIZE = 15; // Ukuran papan Scrabble (15x15)
    private ScrabbleBoardUI boardUI;

    protected void setup() {
        System.out.println("BoardAgent " + getLocalName() + " started.");
        addBehaviour(new InitializeBoardBehaviour());
    }

    private class InitializeBoardBehaviour extends CyclicBehaviour {
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                if (msg.getContent().equals("INITIALIZE_BOARD")) {
                    System.out.println("Initializing Scrabble Board...");
                    if (boardUI == null) {
                        SwingUtilities.invokeLater(() -> {
                            boardUI = new ScrabbleBoardUI();
                            boardUI.setVisible(true);
                        });
                    }
                }
            } else {
                block();
            }
        }
    }

    // UI untuk menampilkan papan Scrabble
    class ScrabbleBoardUI extends JFrame {
        private JPanel boardPanel;

        // Representasi papan Scrabble: W = triple word, w = double word,
        // L = triple letter, l = double letter, * = center, _ = biasa
        private final String[][] scrabbleBoard = {
                { "W", "_", "_", "L", "_", "_", "_", "W", "_", "_", "_", "L", "_", "_", "W" },
                { "_", "w", "_", "_", "_", "l", "_", "_", "_", "l", "_", "_", "_", "w", "_" },
                { "_", "_", "w", "_", "_", "_", "L", "_", "L", "_", "_", "_", "w", "_", "_" },
                { "L", "_", "_", "w", "_", "_", "_", "L", "_", "_", "_", "w", "_", "_", "L" },
                { "_", "_", "_", "_", "w", "_", "_", "_", "_", "_", "w", "_", "_", "_", "_" },
                { "_", "l", "_", "_", "_", "l", "_", "_", "_", "l", "_", "_", "_", "l", "_" },
                { "_", "_", "L", "_", "_", "_", "L", "_", "L", "_", "_", "_", "L", "_", "_" },
                { "W", "_", "_", "L", "_", "_", "_", "*", "_", "_", "_", "L", "_", "_", "W" },
                { "_", "_", "L", "_", "_", "_", "L", "_", "L", "_", "_", "_", "L", "_", "_" },
                { "_", "l", "_", "_", "_", "l", "_", "_", "_", "l", "_", "_", "_", "l", "_" },
                { "_", "_", "_", "_", "w", "_", "_", "_", "_", "_", "w", "_", "_", "_", "_" },
                { "L", "_", "_", "w", "_", "_", "_", "L", "_", "_", "_", "w", "_", "_", "L" },
                { "_", "_", "w", "_", "_", "_", "L", "_", "L", "_", "_", "_", "w", "_", "_" },
                { "_", "w", "_", "_", "_", "l", "_", "_", "_", "l", "_", "_", "_", "w", "_" },
                { "W", "_", "_", "L", "_", "_", "_", "W", "_", "_", "_", "L", "_", "_", "W" }
        };

        public ScrabbleBoardUI() {
            setTitle("Scrabble Game Board");
            setSize(800, 800);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Panel utama untuk papan
            boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
            add(boardPanel);

            // Inisialisasi setiap kotak papan Scrabble
            for (int row = 0; row < BOARD_SIZE; row++) {
                for (int col = 0; col < BOARD_SIZE; col++) {
                    JButton button = new JButton();
                    button.setOpaque(true);
                    button.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    // Tetapkan warna berdasarkan tipe kotak
                    String cell = scrabbleBoard[row][col];
                    switch (cell) {
                        case "W":
                            button.setBackground(new Color(255, 102, 102)); // Triple word (merah terang)
                            button.setText("TW");
                            break;
                        case "w":
                            button.setBackground(new Color(255, 178, 178)); // Double word (merah muda)
                            button.setText("DW");
                            break;
                        case "L":
                            button.setBackground(new Color(102, 178, 255)); // Triple letter (biru terang)
                            button.setText("TL");
                            break;
                        case "l":
                            button.setBackground(new Color(178, 224, 255)); // Double letter (biru muda)
                            button.setText("DL");
                            break;
                        case "*":
                            button.setBackground(Color.YELLOW); // Center
                            button.setText("★");
                            break;
                        default:
                            button.setBackground(Color.WHITE); // Default
                            break;
                    }

                    boardPanel.add(button);
                }
            }
        }
    }
}
