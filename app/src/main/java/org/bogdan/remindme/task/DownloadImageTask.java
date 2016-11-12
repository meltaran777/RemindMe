package org.bogdan.remindme.task;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import org.bogdan.remindme.MainActivity;
import org.bogdan.remindme.fragment.BirhtdayFragment;
import org.bogdan.remindme.fragment.ExampleFragment;

import java.io.InputStream;

/**
 * Created by Bodia on 12.11.2016.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    NotificationCompat.Builder builder;
    Context context;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    public DownloadImageTask(NotificationCompat.Builder builder,Context context) {
        this.builder=builder;
        this.context=context;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {

        if(bmImage != null) bmImage.setImageBitmap(result);

        if(builder != null && context != null) {
            float multiplier = getImageFactor(context.getResources());
            Log.i("multip","multip = "+multiplier);
            Log.i("multip","resW*mul = "+(int)(result.getWidth()*multiplier));
            Log.i("multip","resH*mul = "+(int)(result.getHeight()*multiplier));
            //result = Bitmap.createScaledBitmap(result, (int)(result.getWidth()*multiplier), (int)(result.getHeight()*multiplier), false);
            result = Bitmap.createScaledBitmap(result, 48, 48, false);
            builder.setLargeIcon(result);


        }

    }

    public static float getImageFactor(Resources r){
        DisplayMetrics metrics = r.getDisplayMetrics();
        float multiplier=metrics.density/3f;
        return multiplier;
    }
}
