package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import javax.swing.*;
import java.awt.datatransfer.*;
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

        // Deklarasikan buttons dan scrabbleBoard sebagai variabel anggota kelas
        private JButton[][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE]; // Inisialisasi array tombol
        private String[][] scrabbleBoard = {
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

        // Menempatkan kata secara vertikal di kolom tengah papan
        private void placeWordVertically(String word) {
            int startRow = (BOARD_SIZE / 2) - (word.length() / 2); // Menghitung posisi mulai
            for (int i = 0; i < word.length(); i++) {
                buttons[startRow + i][BOARD_SIZE / 2].setText(String.valueOf(word.charAt(i))); // Menempatkan huruf
                buttons[startRow + i][BOARD_SIZE / 2].setBackground(new Color(200, 255, 200)); // Hijau muda
            }
        }

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

                    buttons[row][col] = button; // Simpan tombol dalam array buttons
                    boardPanel.add(button);

                    // Drag-and-drop listener untuk tiles
                    int finalRow = row;
                    int finalCol = col;

                    button.setTransferHandler(new TransferHandler("text") {
                        @Override
                        public boolean canImport(TransferSupport support) {
                            // Memastikan bahwa data yang diterima sesuai dengan DataFlavor.stringFlavor
                            if (support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        @Override
                        public boolean importData(TransferSupport support) {
                            try {
                                // Ambil teks tile yang di-drag
                                String tile = (String) support.getTransferable()
                                        .getTransferData(DataFlavor.stringFlavor);

                                // Validasi jika posisi kosong atau memiliki kotak khusus
                                if (scrabbleBoard[finalRow][finalCol].equals("_")
                                        || scrabbleBoard[finalRow][finalCol].equals("TW")
                                        || scrabbleBoard[finalRow][finalCol].equals("DW")
                                        || scrabbleBoard[finalRow][finalCol].equals("TL")
                                        || scrabbleBoard[finalRow][finalCol].equals("DL")) {

                                    // Tempatkan tile di posisi baru pada papan
                                    scrabbleBoard[finalRow][finalCol] = tile;
                                    button.setText(tile); // Menampilkan tile pada tombol

                                    // Mengosongkan tile yang di-drag setelah berhasil di drop
                                    // Jika label yang di-drag, kosongkan teksnya
                                    // Anda harus mengidentifikasi dari mana tile tersebut di-drag
                                    for (Component comp : tilePanel.getComponents()) {
                                        if (comp instanceof JLabel) {
                                            JLabel draggedTile = (JLabel) comp;
                                            if (draggedTile.getText().equals(tile)) {
                                                draggedTile.setText(""); // Mengosongkan label yang di-drag
                                                break; // Hentikan pencarian setelah menemukan label yang cocok
                                            }
                                        }
                                    }

                                    JOptionPane.showMessageDialog(null,
                                            "Success! Tile placed at [" + finalRow + ", " + finalCol + "]",
                                            "Tile Placed",
                                            JOptionPane.INFORMATION_MESSAGE);

                                    return true;
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            "Error: Tile already placed at [" + finalRow + ", " + finalCol + "]!",
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(null,
                                        "Error: Could not import data.", "Error", JOptionPane.ERROR_MESSAGE);
                                ex.printStackTrace();
                            }
                            return false;
                        }
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
            String[] wordList = { "CREATE", "AGENTSYSTEM", "COMPUTER", "PROGRAMMING", "JAVA" };
            String selectedWord = wordList[new Random().nextInt(wordList.length)];

            placeWordVertically(selectedWord);

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

                // Ubah ukuran font agar lebih besar
                tile.setFont(new Font("Arial", Font.PLAIN, 30));

                // Mengatur TransferHandler untuk memindahkan teks
                tile.setTransferHandler(new TransferHandler("text") {
                    @Override
                    protected Transferable createTransferable(JComponent c) {
                        // Mengambil teks dari JLabel saat drag dimulai
                        String tileText = ((JLabel) c).getText();
                        System.out.println("Dragging tile: " + tileText); // Debugging
                        return new StringSelection(tileText); // Membungkus teks dalam StringSelection
                    }
                });

                // Drag handling
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        JComponent comp = (JComponent) e.getSource();
                        TransferHandler handler = comp.getTransferHandler();
                        handler.exportAsDrag(comp, e, TransferHandler.COPY); // Memulai drag
                        System.out.println("Dragging tile: " + ((JLabel) comp).getText()); // Debugging
                    }
                });

                tilePanel.add(tile);
            }

            tilePanel.revalidate();
            tilePanel.repaint();
        }
    }
}
