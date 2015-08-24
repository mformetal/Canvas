package milespeele.canvas.parse;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.activity.ActivityHome;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.Datastore;
import milespeele.canvas.util.Logg;
import milespeele.canvas.util.Util;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    @Inject Datastore datastore;
    @Inject EventBus bus;

    private final static String PINNED_IMAGE = "pinned";

    public ParseUtils(Application mApplication) {
        ((MainApp) mApplication).getApplicationComponent().inject(this);
    }

    public void saveImageToServer(String filename, final WeakReference<ActivityHome> weakCxt, Bitmap bitmap) {
        Util.compressBitmap(bitmap)
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
        }
    }
}
