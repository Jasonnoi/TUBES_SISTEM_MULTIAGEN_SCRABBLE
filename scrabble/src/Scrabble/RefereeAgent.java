package Scrabble;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class RefereeAgent extends Agent {
    private List<AID> playerAgents = new ArrayList<>(); // Daftar PlayerAgent
    private int currentPlayerIndex = 0; // Indeks pemain saat ini

    @Override
    protected void setup() {
        System.out.println("RefereeAgent " + getLocalName() + " started.");

        // Cari PlayerAgent yang terdaftar di DF
        searchForPlayerAgents();

        if (playerAgents.isEmpty()) {
            System.out.println("No PlayerAgents found. RefereeAgent terminating.");
            doDelete();
            return;
        }

        // Kirim giliran pertama
        giveTurnToPlayer();

        // Tambahkan behavior untuk menerima pesan
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Proses pesan yang diterima
                    if ("TURN_COMPLETED".equals(msg.getContent())) {
                        System.out.println("Received TURN_COMPLETED from " + msg.getSender().getLocalName());
                        giveTurnToPlayer(); // Berikan giliran ke pemain berikutnya
                    }
                } else {
                    block(); // Tunggu pesan baru
                }
            }
        });
    }

    /**
     * Cari PlayerAgent yang terdaftar di DF.
     */
    private void searchForPlayerAgents() {
        try {
            // Deskripsi layanan untuk mencari agen dengan serviceType "PlayerAgent"
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("PlayerAgent");
            template.addServices(sd);

            // Cari di DF
            DFAgentDescription[] result = DFService.search(this, template);
            System.out.println("Found " + result.length + " PlayerAgents.");
            for (DFAgentDescription desc : result) {
                playerAgents.add(desc.getName()); // Tambahkan AID PlayerAgent ke daftar
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Berikan giliran ke PlayerAgent saat ini.
     */
    private void giveTurnToPlayer() {
        if (playerAgents.isEmpty()) {
            System.out.println("No PlayerAgents available.");
            return;
        }

        // Ambil player berdasarkan indeks giliran
        AID selectedPlayer = playerAgents.get(currentPlayerIndex);

        // Kirim pesan ke PlayerAgent terpilih
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(selectedPlayer);
        msg.setContent("YOUR_TURN"); // Isi pesan yang memberi giliran
        System.out.println("Sending turn message to " + selectedPlayer.getLocalName());
        send(msg);

        // Perbarui indeks untuk pemain berikutnya
        currentPlayerIndex = (currentPlayerIndex + 1) % playerAgents.size();
    }

    @Override
    protected void takeDown() {
        System.out.println("RefereeAgent " + getLocalName() + " terminating.");
    }
}
