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
import milespeele.canvas.service.ServiceBitmapUtils;
import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.Observer;
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
        ServiceBitmapUtils.compressBitmapAsObservable(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    final ParseFile photoFile =
                            new ParseFile(ParseUser.getCurrentUser().getUsername(), bytes);
                    photoFile.saveInBackground((ParseException e) -> {
                        if (e == null) {
                            final Masterpiece art = new Masterpiece();
                            art.setImage(photoFile);
                            art.setTitle(filename);
                            art.saveEventually(e1 -> {
                                if (e1 == null) {
                                    ParseUser.getCurrentUser().getRelation("Masterpieces").add(art);
                                    ParseUser.getCurrentUser().saveEventually(e2 -> {
                                        if (e2 == null) {
                                            ActivityHome activityHome = weakCxt.get();
                                            if (activityHome != null && !activityHome.isFinishing()) {
                                                activityHome.showSavedImageSnackbar(art);
                                            }
                                        } else {
                                            handleError(e2);
                                        }
                                    });
                                } else {
                                    handleError(e1);
                                }
                            });
                        } else {
                            handleError(e);
                        }
                    });
                }, this::handleError);
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
