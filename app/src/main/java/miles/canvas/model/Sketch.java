package miles.canvas.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by mbpeele on 1/11/16.
 */
public class Sketch extends RealmObject {

    @PrimaryKey
    private String id;
    private String title;
    private String description;
    private byte[] bytes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
