package milespeele.canvas.activity;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.MainApp;
import milespeele.canvas.R;
import milespeele.canvas.adapter.MasterpieceAdapter;
import milespeele.canvas.fragment.FragmentMasterpiece;
import milespeele.canvas.parse.Masterpiece;
import milespeele.canvas.parse.ParseUtils;
import milespeele.canvas.util.Logger;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Miles Peele on 7/12/2015.
 */
public class ActivityGallery extends AppCompatActivity {

    @InjectView(R.id.activity_view_gallery_xml) ViewPager gallery;
    @InjectView(R.id.activity_gallery_none) TextView empty;

    @Inject ParseUtils parseUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        ButterKnife.inject(this);
        ((MainApp) getApplicationContext()).getApplicationComponent().inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getMasterpieces();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMasterpieces() {
        try {
            parseUtils.getSavedMasterpieces()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.trampoline())
                    .subscribe(this::onGetMasterpieces, this::onError);
        } catch (ParseException e) {
            empty.setVisibility(View.VISIBLE);
            parseUtils.handleParseError(e);
        }
    }

    public void onGetMasterpieces(List<Masterpiece> masterpieces) {
        if (masterpieces != null && !masterpieces.isEmpty()) {
            MasterpieceAdapter adapter = new MasterpieceAdapter(getFragmentManager(), masterpieces);
            gallery.setAdapter(adapter);
        } else {
            empty.setVisibility(View.VISIBLE);
        }
    }

    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }
}
