package barnes.fahsl.geocatcher;

/**
 * Created by fahslaj on 2/7/2015.
 */
public class Clue {
    private String text;
    private Object image;
    private Object sound;
    private Object video;

    public Clue(String text, Object image, Object sound, Object video) {
        this.text = text;
        this.image = image;
        this.sound = sound;
        this.video = video;
    }

    public String getText() {
        return text;
    }

    public Object getImage() {
        return image;
    }

    public Object getSound() {
        return sound;
    }

    public Object getVideo() {
        return video;
    }
}
