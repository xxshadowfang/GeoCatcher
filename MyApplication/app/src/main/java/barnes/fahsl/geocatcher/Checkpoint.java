package barnes.fahsl.geocatcher;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class Checkpoint {
    private Location location;
    private Clue clue;
    private boolean reached = false;

    public Checkpoint(Location location, Clue clue) {
        this.location = location;
        this.clue = clue;
    }

    public Location getLocation() {
        return location;
    }

    public Clue getClue() {
        return clue;
    }

    public boolean hasBeenReached() {
        return reached;
    }
}
