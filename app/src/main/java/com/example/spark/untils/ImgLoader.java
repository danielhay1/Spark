package com.example.spark.untils;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImgLoader {
    private static ImgLoader instance;
    private Context appContext;

    public interface KEYS {
        //Backgrounds
    }

    public static ImgLoader getInstance() {
        //Singleton design pattern
        return instance;
    }

    public ImgLoader(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static void Init(Context appContext) {
        if(instance == null) {
            Log.d("pttt", "Init: ImgLoader");
            instance = new ImgLoader(appContext);
        }
    }

    public int getImgIdentifier (String name) {
        return appContext.getResources().getIdentifier(name, "drawable", appContext.getPackageName());
    }

    public void loadImg(String name, ImageView img) {
        /*
         * Use to load img.
         * */
        img.setImageResource(getImgIdentifier(name));
    }

    public void loadImgByGlide(String name, ImageView img) {
        /*
         * Use to load img using glide.
         * */
        Glide.with(appContext)
                .load(this.getImgIdentifier(name))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(img);
    }
}
