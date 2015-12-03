package milespeele.canvas.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by mbpeele on 12/1/15.
 */
public class PaintSerializer extends Serializer<SerializablePaint> {

    @Override
    public void write(Kryo kryo, Output output, SerializablePaint object) {
        Logg.log("WRITE PAINT");
    }

    @Override
    public SerializablePaint read(Kryo kryo, Input input, Class<SerializablePaint> type) {
        Logg.log("READ PAINT");
        return new SerializablePaint();
    }
}
