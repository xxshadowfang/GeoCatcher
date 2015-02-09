package barnes.fahsl.geocatcher;

import java.util.ArrayList;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class ScavengerHunt {
    private ArrayList<Checkpoint> checkpoints;
    private boolean completed = false;
    private String name;
    private long id;

    public ScavengerHunt(String name) {
        this(name, new ArrayList<Checkpoint>());
    }
    public ScavengerHunt(String name, ArrayList<Checkpoint> checkpoint){this.name = name; this.checkpoints = checkpoint;}

    public boolean checkLocation(Location location) {
        return false;
    }

    public ArrayList<Checkpoint> getRevealedCheckpoints() {
        ArrayList<Checkpoint> reachedPoints  = new ArrayList<Checkpoint>();
        for(Checkpoint check:checkpoints){
            if(check.hasBeenReached())
                reachedPoints.add(check);
        }
        return reachedPoints;
    }

    public Checkpoint getNextCheckpoint() {


        return checkpoints.get(getRevealedCheckpoints().size());

    }

    public boolean getCompleted() {
        return completed;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Checkpoint> getCheckpoints() {
        return checkpoints;
    }
}
