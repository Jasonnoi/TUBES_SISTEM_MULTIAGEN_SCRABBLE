package Scrabble;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import javax.swing.*;
import java.awt.datatransfer.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PlayerAgent extends Agent {
    private static final int BOARD_SIZE = 15; // Ukuran papan Scrabble (15x15)
    private ScrabbleBoardUI boardUI;
    private boolean isMyTurn = false; // Status giliran

    protected void setup() {
        System.out.println("PlayerAgent " + getLocalName() + " started.");
        registerWithDF();
        action();
        addBehaviour(new TurnListenerBehaviour()); // Menambahkan listener untuk giliran
    }

    private void registerWithDF() {
        try {
            // Buat deskripsi agen untuk didaftarkan ke DF
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            // Tambahkan deskripsi layanan
            ServiceDescription sd = new ServiceDescription();
            sd.setType("PlayerAgent"); // ServiceType yang digunakan oleh RefereeAgent
            sd.setName(getLocalName()); // Nama layanan sesuai dengan nama agen
            dfd.addServices(sd);

            // Daftarkan ke DF
            DFService.register(this, dfd);
            System.out.println(getLocalName() + " registered with DF as PlayerAgent.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public void action() {
        SwingUtilities.invokeLater(() -> {
            // Mendapatkan nama lokal dari agent dan mengirimkan ke konstruktor
            // ScrabbleBoardUI
            boardUI = new ScrabbleBoardUI(getLocalName());
            boardUI.setVisible(true);
            boardUI.updateTurnStatus(false); // Perbarui UI

        });
    }

    private class TurnListenerBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = blockingReceive(MessageTemplate.MatchContent("YOUR_TURN"));
            if (msg != null) {
                isMyTurn = true; // Tukar status giliran
                boardUI.updateTurnStatus(isMyTurn); // Perbarui UI
                System.out.println(isMyTurn ? "It's your turn!" : "Waiting for your turn...");
            }
        }
    }

    class BoardState {
        private String[][] boardState; // Status papan Scrabble
        private JButton[][] buttonState; // Status tombol di papan

        public BoardState(String[][] boardState, JButton[][] buttonState) {
            this.boardState = boardState;
            this.buttonState = buttonState;
        }

        public String[][] getBoardState() {
            return boardState;
        }

        public JButton[][] getButtonState() {
            return buttonState;
        }
    }

    // UI untuk menampilkan papan Scrabble
    class ScrabbleBoardUI extends JFrame {
        private JPanel boardPanel;
        private JPanel tilePanel;
        private JButton reshuffleButton;
        private JButton undoButton;
        private JButton submitButton;
        private String name;
        private JLabel turnStatusLabel;
        private boolean isMyTurn;

        // Deklarasikan buttons dan scrabbleBoard sebagai variabel anggota kelas
        private JButton[][] buttons = new JButton[BOARD_SIZE][BOARD_SIZE]; // Inisialisasi array tombol
        private String[][] scrabbleBoard = {
                { "TW", "", "", "TL", "", "", "", "TW", "", "", "", "TL", "", "", "TW" },
                { "", "DW", "", "", "", "DL", "", "", "", "DL", "", "", "", "DW", "" },
                { "", "", "DW", "", "", "", "TL", "", "TL", "", "", "", "DW", "", "" },
                { "TL", "", "", "DW", "", "", "", "TL", "", "", "", "DW", "", "", "TL" },
                { "", "", "", "", "DW", "", "", "", "", "", "DW", "", "", "", "" },
                { "", "DL", "", "", "", "DL", "", "", "", "DL", "", "", "", "DL", "" },
                { "", "", "TL", "", "", "", "TL", "", "TL", "", "", "", "TL", "", "" },
                { "TW", "", "", "TL", "", "", "", "★", "", "", "", "TL", "", "", "TW" },
                { "", "", "TL", "", "", "", "TL", "", "TL", "", "", "", "TL", "", "" },
                { "", "DL", "", "", "", "DL", "", "", "", "DL", "", "", "", "DL", "" },
                { "", "", "", "", "DW", "", "", "", "", "", "DW", "", "", "", "" },
                { "TL", "", "", "DW", "", "", "", "TL", "", "", "", "DW", "", "", "TL" },
                { "", "", "DW", "", "", "", "TL", "", "TL", "", "", "", "DW", "", "" },
                { "", "DW", "", "", "", "DL", "", "", "", "DL", "", "", "", "DW", "" },
                { "TW", "", "", "TL", "", "", "", "TW", "", "", "", "TL", "", "", "TW" }
        };
        Stack<BoardState> undoStack = new Stack<>();

        // Menempatkan kata secara vertikal di kolom tengah papan
        private void placeWordVertically(String word) {
            int startRow = (BOARD_SIZE / 2) - (word.length() / 2); // Menghitung posisi mulai
            for (int i = 0; i < word.length(); i++) {
                buttons[startRow + i][BOARD_SIZE / 2].setText(String.valueOf(word.charAt(i))); // Menempatkan huruf
                buttons[startRow + i][BOARD_SIZE / 2].setBackground(new Color(200, 255, 200)); // Hijau muda
            }
        }

        public ScrabbleBoardUI(String userName) {
            this.name = userName;

            setTitle("Board Milik - " + this.name);
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
                        case "TW":
                            button.setBackground(new Color(255, 102, 102)); // Triple word (merah terang)
                            button.setText("TW");
                            break;
                        case "DW":
                            button.setBackground(new Color(255, 178, 178)); // Double word (merah muda)
                            button.setText("DW");
                            break;
                        case "TL":
                            button.setBackground(new Color(102, 178, 255)); // Triple letter (biru terang)
                            button.setText("TL");
                            break;
                        case "DL":
                            button.setBackground(new Color(178, 224, 255)); // Double letter (biru muda)
                            button.setText("DL");
                            break;
                        case "★":
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
                    // Menampilkan tile pada tombol

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
                                saveStateForUndo();

                                // Ambil teks tile yang di-drag
                                String tile = (String) support.getTransferable()
                                        .getTransferData(DataFlavor.stringFlavor);
                                String[] validTiles = { "", "TW", "DW", "TL", "DL" };

                                // Validasi jika posisi kosong atau memiliki kotak khusus
                                if (Arrays.asList(validTiles).contains(buttons[finalRow][finalCol].getText())) {

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
            undoButton = new JButton("Undo"); // Tombol Undo
            controlPanel.add(undoButton); // Menambahkan tombol Undo
            controlPanel.add(reshuffleButton);
            controlPanel.add(submitButton);
            add(controlPanel, BorderLayout.EAST);
            String[] wordList = { "CREATE", "AGENTSYSTEM", "COMPUTER", "PROGRAMMING", "JAVA" };
            String selectedWord = wordList[new Random().nextInt(wordList.length)];
            // Panel status giliran
            turnStatusLabel = new JLabel("Waiting for your turn...");
            turnStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            turnStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
            turnStatusLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            add(turnStatusLabel, BorderLayout.NORTH);

            placeWordVertically(selectedWord);
            if (this.isMyTurn) {
                // Generate tiles pertama kali
                generateRandomTiles();
                // Listener untuk tombol
                reshuffleButton.addActionListener(e -> generateRandomTiles());
                submitButton.addActionListener(e -> System.out.println("Submit clicked!"));
                undoButton.addActionListener(e -> undoMove());
            }

        }

        public void updateTurnStatus(boolean isMyTurn) {
            this.isMyTurn = isMyTurn;
            if (isMyTurn) {
                turnStatusLabel.setText("It's your turn!");
            } else {
                turnStatusLabel.setText("Waiting for your turn...");
            }
        }

        private void saveStateForUndo() {
            // Simpan informasi papan dan status tombol
            String[][] boardStateCopy = new String[BOARD_SIZE][BOARD_SIZE];
            JButton[][] buttonStateCopy = new JButton[BOARD_SIZE][BOARD_SIZE];

            // Menyalin status scrabbleBoard dan status tombol
            for (int i = 0; i < BOARD_SIZE; i++) {
                System.arraycopy(scrabbleBoard[i], 0, boardStateCopy[i], 0, BOARD_SIZE);
                for (int j = 0; j < BOARD_SIZE; j++) {
                    JButton button = buttons[i][j];
                    // Buat salinan tombol dengan status yang relevan (warna, teks)
                    JButton buttonCopy = new JButton();
                    buttonCopy.setText(button.getText());
                    buttonCopy.setBackground(button.getBackground());
                    buttonStateCopy[i][j] = buttonCopy;
                }
            }

            // Simpan state ke stack (sekarang menggunakan BoardState)
            undoStack.push(new BoardState(boardStateCopy, buttonStateCopy));
        }

        private void undoMove() {
            if (!undoStack.isEmpty()) {
                BoardState previousState = undoStack.pop();
                String[][] previousBoardState = previousState.getBoardState();
                JButton[][] previousButtonState = previousState.getButtonState();

                // Kembalikan scrabbleBoard ke kondisi sebelumnya
                for (int i = 0; i < BOARD_SIZE; i++) {
                    System.arraycopy(previousBoardState[i], 0, scrabbleBoard[i], 0, BOARD_SIZE);
                }

                // Kembalikan status tombol (teks dan warna) sesuai dengan status sebelumnya
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        JButton button = buttons[i][j];
                        JButton previousButton = previousButtonState[i][j];

                        // Kembalikan warna dan teks tombol
                        button.setText(previousButton.getText());
                        button.setBackground(previousButton.getBackground());
                    }
                }
            }
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
