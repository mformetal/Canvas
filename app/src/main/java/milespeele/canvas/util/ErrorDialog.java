package milespeele.canvas.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import milespeele.canvas.R;

public class ErrorDialog extends Dialog implements View.OnClickListener, DialogInterface.OnDismissListener {

    public final static int NO_INTERNET = 404;
    public final static int GENERAL = -2;
    public final static int NO_CAMERA = -3;

    @Bind(R.id.error_dialog_title) TextView title;
    @Bind(R.id.error_dialog_body) TextView body;
    @Bind(R.id.error_dialog_pos_button) Button posButton;

    private String titleText;
    private String bodyText;

    protected ErrorDialog(Context context, String titleText, String bodyText) {
        super(context, R.style.DialogTheme);
        this.titleText = titleText;
        this.bodyText = bodyText;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_error);
        ButterKnife.bind(this);
        title.setText(titleText);
        body.setText(bodyText);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ButterKnife.unbind(this);
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
            case ParseException.OBJECT_NOT_FOUND:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_object_not_found_title),
                        context.getResources().getString(R.string.error_object_not_found_body)
                );
            case NO_INTERNET:
            case ParseException.CONNECTION_FAILED:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_no_internet_title),
                        context.getResources().getString(R.string.error_no_internet_body)
                );
            case GENERAL:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_dialog_general_title),
                        context.getResources().getString(R.string.error_dialog_general_body)
                );
            case NO_CAMERA:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_dialog_no_camera_title),
                        context.getResources().getString(R.string.error_dialog_no_camera_body)
                );
            default:
                return new ErrorDialog(context,
                        context.getResources().getString(R.string.error_dialog_general_title),
                        context.getResources().getString(R.string.error_dialog_general_body)
                );
        }
    }
}
