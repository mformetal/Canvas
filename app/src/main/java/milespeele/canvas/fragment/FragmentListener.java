package milespeele.canvas.fragment;

/**
 * Created by milespeele on 7/3/15.
 */
public interface FragmentListener {

    void onColorChosen(int color, String whichColor);

    void showColorPicker(int viewId);

    void showShapePicker();

    void showBrushPicker(float currentWidth);

    void onBrushSizeChosen(float size);

    void onFilenameChosen(String fileName);
}
