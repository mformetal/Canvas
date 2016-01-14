package miles.canvas.dagger;

import javax.inject.Singleton;

import dagger.Component;
import miles.canvas.ui.activity.BaseActivity;
import miles.canvas.ui.drawing.DrawingCurve;
import miles.canvas.ui.fragment.BaseFragment;
import miles.canvas.ui.widget.CanvasLayout;
import miles.canvas.ui.widget.FabMenu;


/**
 * Created by milespeele on 7/5/15.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(DrawingCurve drawingCurve);

    void inject(BaseActivity baseActivity);

    void inject(FabMenu viewFabMenu);

    void inject(CanvasLayout canvasLayout);

    void inject(BaseFragment fragment);
}
