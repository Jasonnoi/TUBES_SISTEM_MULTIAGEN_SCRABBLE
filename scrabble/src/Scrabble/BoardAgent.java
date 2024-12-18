package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.*;

import java.awt.*;
import java.util.Random;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
        private JPanel tilePanel;
        private JButton reshuffleButton;
        private JButton submitButton;

        // Representasi papan Scrabble
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
            setSize(1000, 800);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            // Layout utama
            setLayout(new BorderLayout());

            // Panel papan
            boardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
            add(boardPanel, BorderLayout.CENTER);

            // Inisialisasi papan
            JButton[][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE];
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

                    buttons[row][col] = button;
                    boardPanel.add(button);

                    // Drag-and-drop listener untuk tiles
                    button.setTransferHandler(new TransferHandler("text"));
                    int finalRow = row;
                    int finalCol = col;
                    button.addActionListener(e -> {
                        String tile = button.getText();
                        System.out.printf("Tile placed at [%d, %d]: %s%n", finalRow, finalCol, tile);
                    });
                }
            }

            // Panel untuk tiles
            tilePanel = new JPanel();
            tilePanel.setLayout(new GridLayout(1, 5));
            add(tilePanel, BorderLayout.SOUTH);

            // Tombol kontrol
            JPanel controlPanel = new JPanel();
            reshuffleButton = new JButton("Reshuffle");
            submitButton = new JButton("Submit");
            controlPanel.add(reshuffleButton);
            controlPanel.add(submitButton);
            add(controlPanel, BorderLayout.EAST);

            // Generate tiles pertama kali
            generateRandomTiles();

            // Listener untuk tombol
            reshuffleButton.addActionListener(e -> generateRandomTiles());
            submitButton.addActionListener(e -> System.out.println("Submit clicked!"));
        }

        // Generate tiles huruf acak
        private void generateRandomTiles() {
            tilePanel.removeAll();
            String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Random random = new Random();
            for (int i = 0; i < 5; i++) {
                JLabel tile = new JLabel(String.valueOf(alphabet.charAt(random.nextInt(alphabet.length()))),
                        SwingConstants.CENTER);
                tile.setOpaque(true);
                tile.setBackground(new Color(240, 240, 240));
                tile.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                tile.setTransferHandler(new TransferHandler("text"));

                // Drag-and-drop listener
                // Drag-and-drop listener untuk tile
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        // Mengambil sumber komponen yang dipilih
                        JComponent comp = (JComponent) e.getSource();
                        TransferHandler handler = comp.getTransferHandler();

                        // Memulai drag and drop dengan metode COPY
                        handler.exportAsDrag(comp, e, TransferHandler.COPY);
                    }
                });

                tilePanel.add(tile);
            }
            tilePanel.revalidate();
            tilePanel.repaint();
        }
    }
}
