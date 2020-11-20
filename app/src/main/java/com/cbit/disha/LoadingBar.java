package com.cbit.disha;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.github.ybq.android.spinkit.style.RotatingPlane;
import com.github.ybq.android.spinkit.style.ThreeBounce;

public class LoadingBar {
    private Activity activity;
    private AlertDialog dialog;
    private ProgressBar progressBar;

    public LoadingBar(Activity activity)
    {
        this.activity = activity;
    }
    void showLoadingBar(int styleId)
    {
        View view = activity.getLayoutInflater().inflate(R.layout.loading_bar_layout, null);
        progressBar = view.findViewById(R.id.loading_bar);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity,R.style.CustomAlertDialog);
        builder.setView(view);
        builder.setCancelable(false);
        setStyle(styleId);
        dialog = builder.create();
        dialog.show();
    }
    void dismissLoadingBar()
    {
        try {
            dialog.cancel();
        }
        catch (Exception e)
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
                sprite = new RotatingPlane();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            case 4:
                sprite = new ThreeBounce();
                progressBar.setIndeterminateDrawable(sprite);
                break;
            default:
                break;
        }
    }
}
