package com.cbit.disha;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.github.ybq.android.spinkit.style.FoldingCube;
import com.github.ybq.android.spinkit.style.ThreeBounce;

class CustomAlertDialog {
    private Activity activity;
    private AlertDialog dialog;
    private ProgressBar progressBar;
    CustomAlertDialog(Activity activity)
    {
        this.activity = activity;
    }
    void showAlertDialog(String msg,int styleId)
    {
        View view = activity.getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
        TextView message = view.findViewById(R.id.alert_dialog_message);
        progressBar = view.findViewById(R.id.alert_dialog_bar);
        message.setText(msg);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setCancelable(false);
        setStyle(styleId);
        dialog = builder.create();
        dialog.show();
    }
    void dismissAlertDialog()
    {
        try {
            dialog.cancel();
        }
        catch (Exception ignored)
        {
        }
    }
    void setStyle(int styleId)
    {
        switch (styleId)
        {
            case 1:
                Sprite sprite = new FadingCircle();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            case 2:
                sprite = new Circle();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            case 3:
                sprite = new ThreeBounce();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            case 4:
                sprite = new FoldingCube();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            default:
                break;
        }
    }
}
