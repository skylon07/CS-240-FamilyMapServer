package utils;

import dataAccess.Database;
import models.User;

public class FamilyTreeUtils extends GenericUtility {
    static final int NUM_GENERATIONS = 4;
    
    public FamilyTreeUtils(Database database) {
        super(database);
    }

    /**
     * Generates a fake family tree by generating/tying people together for a user
     * 
     * @param user is the user whose family should be generated
     * @param numGenerations is the number of generations to generate
     */
    public GenerationAttempt generateFamilyTree(User user, int numGenerations) {
        // TODO
        GenerationAttempt attempt = new GenerationAttempt();
        attempt.setSuccess(false);
        return attempt;
    }
    
    /**
     * Generates a fake family tree with a default number of generations
     * 
     * @param user is the user whose family should be generated
     */
    public GenerationAttempt generateFamilyTree(User user) {
        return this.generateFamilyTree(user, FamilyTreeUtils.NUM_GENERATIONS);
    }

    public class GenerationAttempt {
        private boolean success;
        private int numPersonsCreated;
        private int numEventsCreated;

        public boolean getSuccess() {
            return this.success;
        }
        
        protected void setSuccess(boolean success) {
            this.success = success;
        }

        public int getNumPersonsCreated() {
            return this.numPersonsCreated;
        }

        protected void setNumPersonsCreated(int numPersonsCreated) {
            this.numPersonsCreated = numPersonsCreated;
        }

        public int getNumEventsCreated() {
            return this.numEventsCreated;
        }

        protected void setNumEventsCreated(int numEventsCreated) {
            this.numEventsCreated = numEventsCreated;
        }
    }
}
