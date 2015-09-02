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
import milespeele.canvas.bitmap.BitmapUtils;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.FontUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject EventBus bus;

    private final static String PINNED_IMAGE = "pinned";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void saveImageToServer(String filename, final WeakReference<ActivityHome> weakCxt, Bitmap bitmap) {
        BitmapUtils.compressBitmap(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> {
                    final ParseFile photoFile =
                            new ParseFile(filename, bytes);
                    photoFile.saveInBackground((ParseException e) -> {
                        if (e == null) {
                            final Masterpiece art = new Masterpiece();
                            art.setImage(photoFile);
                            art.setName(filename);
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
                                            handleParseError(e2);
                                        }
                                    });
                                } else {
                                    handleParseError(e1);
                                }
                            });
                        } else {
                            handleParseError(e);
                        }
                    });
                }, this::handleRxError);
    }

    public void handleParseError(ParseException e) {
        e.printStackTrace();
        Logg.log("PARSE ERROR CODE: "+ e.getCode());
        bus.post(new EventParseError(e));
    }

    public void handleRxError(Throwable throwable) {
        if (throwable instanceof ParseException) {
            ParseException e = (ParseException) throwable;
            e.printStackTrace();
            Logg.log("PARSE ERROR CODE: "+ e.getCode());
            bus.post(new EventParseError(e));
        } else {
            bus.post(new EventParseError(throwable));
        }
    }
}
