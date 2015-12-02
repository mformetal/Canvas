package milespeele.canvas.util;

import android.graphics.Paint;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

/**
 * Created by mbpeele on 12/1/15.
 */
public class SerializablePaint extends Paint implements Serializable {

    public SerializablePaint() {

    }

    public SerializablePaint(Paint paint) {
        super(paint);
    }
}
