package milespeele.canvas.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import milespeele.canvas.R;
import milespeele.canvas.view.DrawingView;


public class ActivityHome extends AppCompatActivity {

    @InjectView(R.id.activity_home_drawing_view) DrawingView drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
    }

}
