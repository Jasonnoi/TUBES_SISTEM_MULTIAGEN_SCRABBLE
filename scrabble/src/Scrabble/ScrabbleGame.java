package Scrabble;

import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class ScrabbleGame {
    public static void main(String[] args) {
        try {
            // Setup JADE container
            jade.core.Runtime rt = jade.core.Runtime.instance();
            ProfileImpl p = new ProfileImpl();
            ContainerController container = rt.createMainContainer(p);
            // Create and start BoardAgent
            AgentController boardAgent = container.createNewAgent("BoardAgent", "Scrabble.BoardAgent", null);
            boardAgent.start();

            // Create and start agents
            AgentController refereeAgent = container.createNewAgent("RefereeAgent", "Scrabble.RefereeAgent", null);
            refereeAgent.start();

            // Create ValidatorAgent
            AgentController validatorAgent = container.createNewAgent("ValidatorAgent", "Scrabble.ValidatorAgent",
                    null);
            validatorAgent.start();

            // Create Player agents
            AgentController playerAgent1 = container.createNewAgent("Player1", "Scrabble.PlayerAgent", null);
            playerAgent1.start();
            AgentController playerAgent2 = container.createNewAgent("Player2", "Scrabble.PlayerAgent", null);
            playerAgent2.start();

            // Print the initialization message
            System.out.println("Initializing Scrabble Game...");

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
