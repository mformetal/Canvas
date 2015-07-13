package milespeele.canvas.view;

import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.Stack;

import milespeele.canvas.util.Logger;

/**
 * Created by Miles Peele on 7/13/2015.
 */
public class PaintStack extends Stack<PaintPath> implements Parcelable {

    public PaintStack() {}

    public PaintStack(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(toArray());
    }

    public void readFromParcel(Parcel in) {
        PaintPath[] array = (PaintPath[]) in.readArray(PaintPath.class.getClassLoader());
        for (PaintPath path: array) {
            Logger.log("READ FROM PARCEL COLOR: " + path.getColor());
            push(path);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PaintStack createFromParcel(Parcel in) {return new PaintStack(in);}

        @Override
        public PaintPath[] newArray(int size) { return new PaintPath[size]; }
    };

}
