package miles.scribble.data.model;

import io.realm.RealmObject;

/**
 * Created by mbpeele on 1/14/16.
 */
public class Profile extends RealmObject {

    private String name;
    private byte[] photo;
    private int sketchCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public int getSketchCount() {
        return sketchCount;
    }

    public void setSketchCount(int sketchCount) {
        this.sketchCount = sketchCount;
    }
}
