package miles.canvas.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import de.hdodenhof.circleimageview.CircleImageView;
import miles.canvas.R;
import miles.canvas.data.model.Profile;
import miles.canvas.ui.widget.TypefaceTextView;

/**
 * Created by mbpeele on 1/14/16.
 */
public class ProfileActivity extends BaseActivity {
    @Bind(R.id.activity_profile_photo) CircleImageView circleImageView;
    @Bind(R.id.activity_profile_recycler) RecyclerView recyclerView;
    @Bind(R.id.activity_profile_sketch_count) TypefaceTextView sketchCount;

    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profile = realm.where(Profile.class).findFirst();

        if (profile == null) {
            circleImageView.setImageResource(R.drawable.ic_no_profile_photo);
            sketchCount.setText("0 Sketches");
        } else {
            Glide.with(this)
                    .fromBytes()
                    .load(profile.getPhoto())
                    .into(circleImageView);
        }
    }
}
