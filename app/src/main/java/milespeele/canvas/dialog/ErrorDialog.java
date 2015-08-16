package milespeele.canvas.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;

/**
 * Created by Miles Peele on 8/16/2015.
 */
public class ErrorDialog extends Dialog implements View.OnClickListener {

    @Bind(R.id.error_dialog_title) TextView title;
    @Bind(R.id.error_dialog_body) TextView body;
    @Bind(R.id.error_dialog_pos_button) Button posButton;

    private String titleText;
    private String bodyText;

    protected ErrorDialog(Context context,  String titleText, String bodyText, int theme) {
        super(context, theme);
        this.titleText = titleText;
        this.bodyText = bodyText;
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.error_dialog);
        ButterKnife.bind(this);
        title.setText(titleText);
        body.setText(bodyText);
    }

    @Override
    @OnClick(R.id.error_dialog_pos_button)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.error_dialog_pos_button:
                dismiss();
                break;
        }
    }

    public static ErrorDialog createDialogFromCode(Context context, int code) {
        switch (code) {
            case ParseException.CONNECTION_FAILED:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_no_internet_title),
                        context.getResources().getString(R.string.error_no_internet_body),
                        R.style.DialogTheme);
            default:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_dialog_general_title),
                        context.getResources().getString(R.string.error_dialog_general_body),
                        R.style.DialogTheme);
        }
    }
}
