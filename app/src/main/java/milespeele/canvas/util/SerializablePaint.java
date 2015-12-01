package milespeele.canvas.util;

import android.graphics.Paint;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * Created by mbpeele on 11/30/15.
 */
public class SerializablePaint extends Paint implements Externalizable {

    public SerializablePaint() {
    }

    public SerializablePaint(Paint paint) {
        super(paint);
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {

    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {

    }
}
