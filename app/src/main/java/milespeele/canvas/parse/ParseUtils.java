package milespeele.canvas.parse;

import android.app.Application;
import android.graphics.Bitmap;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject EventBus bus;

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void saveImageToServer(String filename, final WeakReference<ActivityHome> weakCxt, Bitmap bitmap) {

    }

    public void handleError(Throwable throwable) {
        throwable.printStackTrace();
        if (throwable instanceof ParseException) {
            ParseException e = (ParseException) throwable;
            Logg.log("PARSE ERROR CODE: " + e.getCode());
            Logg.log("PARSE ERROR MESSAGE: " + e.getMessage());
            bus.post(new EventParseError(e));
        } else {
            bus.post(new EventParseError(throwable));
        }
    }
}
