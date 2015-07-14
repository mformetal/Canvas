package milespeele.canvas.view;

import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by milespeele on 7/9/15.
 */
public class PaintPath extends Path implements Parcelable {

    private Paint paint;
    private int color;

    public PaintPath(Parcel in) {
        readFromParcel(in);
    }

    public PaintPath(PaintPath path) { set(path); }

    public PaintPath(Paint paint) {
        this.paint = paint;
        this.color = paint.getColor();
    }

    public Paint getPaint() { return paint; }

    public int getColor() { return color; }

    public void setColor(int color) { paint.setColor(color); }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this, flags);
    }

    public void readFromParcel(Parcel in) {
        set(in.readParcelable(PaintPath.class.getClassLoader()));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public PaintPath createFromParcel(Parcel in) {return new PaintPath(in);}

        public PaintPath[] newArray(int size) { return new PaintPath[size]; }
    };
}