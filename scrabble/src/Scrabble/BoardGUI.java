package Scrabble;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BoardGUI extends JFrame {
    private JButton[][] buttons = new JButton[15][15];
    private JTextField wordField;
    private JTextField positionField;
    private JButton submitButton;
    private JLabel messageLabel;

    private ContainerController jadeContainer; // Referensi ke JADE Container
    private String validatorAgentName = "ValidatorAgent";
    private String boardAgentName = "BoardAgent";

    public BoardGUI(ContainerController container) {
        this.jadeContainer = container;
        setTitle("Scrabble Board");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(15, 15));
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                buttons[i][j] = new JButton(".");
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 12));
                buttons[i][j].setEnabled(false);
                boardPanel.add(buttons[i][j]);
            }
        }

        JPanel controlPanel = new JPanel(new GridLayout(3, 2));
        wordField = new JTextField();
        positionField = new JTextField();
        submitButton = new JButton("Submit Word");
        messageLabel = new JLabel("Enter word and position (row,col)");

        controlPanel.add(new JLabel("Word:"));
        controlPanel.add(wordField);
        controlPanel.add(new JLabel("Position (row,col):"));
        controlPanel.add(positionField);
        controlPanel.add(submitButton);

        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(messageLabel, BorderLayout.NORTH);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = wordField.getText().toUpperCase();
                String position = positionField.getText();

                if (word.isEmpty() || position.isEmpty()) {
                    messageLabel.setText("Please enter a word and position.");
                    return;
                }

                String[] pos = position.split(",");
                try {
                    int row = Integer.parseInt(pos[0].trim());
                    int col = Integer.parseInt(pos[1].trim());

                    // Kirim ACLMessage ke ValidatorAgent
                    ACLMessage validateMsg = new ACLMessage(ACLMessage.REQUEST);
                    validateMsg.addReceiver(new AID(validatorAgentName, AID.ISLOCALNAME));
                    validateMsg.setContent(word + " " + row + " " + col);
                    jadeContainer.getAgent("BoardAgent").putO2AObject(validateMsg, false);

                    messageLabel.setText("Word submitted for validation: " + word);
                } catch (Exception ex) {
                    messageLabel.setText("Invalid position format. Use row,col.");
                }
            }
        });

        setSize(800, 800);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            jade.core.Runtime rt = jade.core.Runtime.instance();
            jade.core.ProfileImpl p = new jade.core.ProfileImpl();
            ContainerController container = rt.createMainContainer(p);

            // Jalankan agen
            AgentController boardAgent =
                    container.createNewAgent("BoardAgent", "Scrabble.BoardAgent", null);
            boardAgent.start();

            AgentController validatorAgent =
                    container.createNewAgent("ValidatorAgent", "Scrabble.ValidatorAgent", null);
            validatorAgent.start();

            AgentController refereeAgent =
                    container.createNewAgent("RefereeAgent", "Scrabble.RefereeAgent", null);
            refereeAgent.start();

            // Hanya buat 2 PlayerAgent
            AgentController playerAgent1 =
                    container.createNewAgent("Player1", "Scrabble.PlayerAgent", null);
            playerAgent1.start();

            AgentController playerAgent2 =
                    container.createNewAgent("Player2", "Scrabble.PlayerAgent", null);
            playerAgent2.start();

            // Mulai GUI
            new BoardGUI(container);

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
