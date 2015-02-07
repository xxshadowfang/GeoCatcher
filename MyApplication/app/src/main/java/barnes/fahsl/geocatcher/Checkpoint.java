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

    /**
     * Created by fahslaj on 2/7/2015.
     */
    class Clue {
        private String text;
        private String imageURL;
        private String soundURL;
        private String videoURL;

        public Clue(String text, String imageURL, String soundURL, String videoURL) {
            this.text = text;
            this.imageURL = imageURL;
            this.soundURL = soundURL;
            this.videoURL = videoURL;
        }

        public String getText() {
            return text;
        }

        public String getImageURL() {
            return imageURL;
        }

        public String getSoundURL() {
            return soundURL;
        }

        public String getVideoURL() {
            return videoURL;
        }
    }
}

