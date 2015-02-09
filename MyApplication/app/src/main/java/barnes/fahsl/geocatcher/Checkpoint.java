package barnes.fahsl.geocatcher;

import android.graphics.Bitmap;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class Checkpoint {
    private Location location;
    private Clue clue;
    private boolean reached;
    private int checkNo;
    private long checkId;

    public Checkpoint(Location location, int no) {
        this.checkNo = no;
        this.location = location;

        reached = false;
        setClue("", null, null, null);
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
    public void setReached(Boolean bool){
        reached = bool;
    }

    public void setClue(String text, String imageURL, String soundURL, String videoURL){
        this.clue = new Clue(text,imageURL,soundURL,videoURL);
    }

    public int getCheckNo() {
        return checkNo;
    }

    public void setCheckNo(int checkNo) {
        this.checkNo = checkNo;
    }

    public void setCheckId(long checkId) {
        this.checkId = checkId;
    }

    public long getCheckId() {
        return this.checkId;
    }


    /**
     * Created by fahslaj on 2/7/2015.
     */
    class Clue {
        private String text;
        private String imageURL;
        private String soundURL;
        private String videoURL;
        private Bitmap image;

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

        public Bitmap getImage() {
            return image;
        }

        public void setImage(Bitmap bmp) {
            this.image = bmp;
            this.imageURL = null;
        }
    }
}

