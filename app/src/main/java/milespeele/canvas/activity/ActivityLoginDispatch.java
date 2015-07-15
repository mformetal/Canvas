package milespeele.canvas.activity;

import com.parse.ui.ParseLoginDispatchActivity;

/**
 * Created by milespeele on 7/14/15.
 */
public class ActivityLoginDispatch extends ParseLoginDispatchActivity {

    @Override
    protected Class<?> getTargetClass() {
        return ActivityHome.class;
    }
}
