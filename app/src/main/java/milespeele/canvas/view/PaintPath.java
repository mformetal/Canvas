package milespeele.canvas.view;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path implements Parcelable {

    private int color;

    public PaintPath(int color) {
        this.color = color;
    }

    public PaintPath(Parcel in) {
        readFromParcel(in);
    }

    public PaintPath(PaintPath path) { set(path); }

    public void setColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this, flags);
        dest.writeInt(color);
    }

    public void readFromParcel(Parcel in) {
        set(in.readParcelable(PaintPath.class.getClassLoader()));
        color = in.readInt();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PaintPath createFromParcel(Parcel in) {return new PaintPath(in);}

        public PaintPath[] newArray(int size) { return new PaintPath[size]; }
    };
}