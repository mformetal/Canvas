package milespeele.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentDrawer;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.view.ViewCanvas;
import milespeele.canvas.view.ViewFabMenu;

/**
 * Created by milespeele on 7/5/15.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(ParseUtils utils);
    void inject(DrawingCurve drawingCurve);

    void inject(ActivityHome activityHome);

    void inject(ViewFabMenu viewFabMenu);
    void inject(ViewCanvas viewCanvas);

    void inject(FragmentDrawer fragmentDrawer);
    void inject(FragmentColorPicker fragmentColorPicker);
    void inject(FragmentBrushPicker fragmentBrushPicker);
    void inject(FragmentFilename fragmentFilename);
}
