package milespeele.canvas.parse;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.activity.ActivityBase;
import milespeele.canvas.event.EventParseError;
import milespeele.canvas.util.ErrorDialog;
import milespeele.canvas.util.FileUtils;
import milespeele.canvas.util.Logg;
import rx.Observable;
import rx.functions.Func1;
import rx.parse.ParseObservable;

/**
 * Created by milespeele on 7/4/15.
 */
public class ParseUtils {

    public final static String MASTERPIECE_RELATION = "Masterpieces";

    public ParseUtils() {}

    public Observable saveImageToServer(final String filename, final Bitmap bitmap) {
        return FileUtils.compress(bitmap)
                .flatMap(bytes -> {
                    Logg.log("COMPRESSED");
                    final ParseFile photoFile = new ParseFile(
                            ParseUser.getCurrentUser().getUsername(), bytes);
                    return ParseObservable.save(photoFile);
                })
                .flatMap(parseFile -> {
                    Logg.log("SAVING PARSEFILE");
                    final Masterpiece art = new Masterpiece();
                    art.setImage(parseFile);
                    art.setTitle(filename);
                    return ParseObservable.saveEventually(art);
                })
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        Logg.log("SAVING IN RELATIOn");
                        ParseUser.getCurrentUser().getRelation(MASTERPIECE_RELATION).add((Masterpiece) o);
                        return ParseObservable.saveEventually(ParseUser.getCurrentUser());
                    }
                });
    }

    public static void handleError(ParseException e, ActivityBase activityBase) {
        handleError(null, activityBase);
    }

    public static void handleError(ParseException e, View view, ActivityBase activityBase) {
        switch (e.getCode()) {
            case ParseException.INVALID_SESSION_TOKEN:
                activityBase
                        .showSnackbar(view,
                                R.string.parse_error_invalid_sesion_token,
                                Snackbar.LENGTH_INDEFINITE,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        activityBase.startLoginActivity();
                                    }
                                });
                break;
            default:
                activityBase
                        .showSnackbar(view, R.string.parse_error_general,
                                Snackbar.LENGTH_SHORT,
                                null);
                break;
        }
    }
}
