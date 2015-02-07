package barnes.fahsl.geocatcher;

import java.util.ArrayList;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class ScavengerHunt {
    private ArrayList<Checkpoint> checkpoints;
    private boolean completed = false;

    public boolean checkLocation(Location location) {
        return false;
    }

    public ArrayList<Checkpoint> getRevealedCheckpoints() {
        return null;
    }

    public Clue getNextClue() {
        return null;
    }

    public ArrayList<Clue> getCompletedClues() {
        return null;
    }

    public boolean getCompleted() {
        return completed;
    }
}
