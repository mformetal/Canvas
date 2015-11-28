package milespeele.canvas.util;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by mbpeele on 11/27/15.
 */
public class NetworkUtils {

    public static boolean hasInternet(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
