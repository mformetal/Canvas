package milespeele.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.drawing.DrawingCurve;
import milespeele.canvas.drawing.DrawingThread;
import milespeele.canvas.fragment.FragmentBrushPicker;
import milespeele.canvas.fragment.FragmentColorPicker;
import milespeele.canvas.fragment.FragmentFilename;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.view.ViewCanvasSurface;
import milespeele.canvas.view.ViewDashboard;
import milespeele.canvas.view.ViewDashboardButton;
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
    void inject(ViewDashboard viewDashboard);
    void inject(ViewDashboardButton viewDashboardButton);

    void inject(FragmentColorPicker fragmentColorPicker);
    void inject(FragmentBrushPicker fragmentBrushPicker);
    void inject(FragmentFilename fragmentFilename);

}
