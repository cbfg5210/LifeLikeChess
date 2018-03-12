package com.ue.library.util;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static android.R.attr.path;

/**
 * Created by hawk on 2017/10/24.
 */

public class ImageLoader {
    public static void displayImage(Context context, String path, ImageView imageView) {
        Picasso.with(context)
                .load(path)
                .into(imageView);
    }

    public static void displayImage(Context context, int imgRes, ImageView imageView) {
        Picasso.with(context)
                .load(imgRes)
                .into(imageView);
    }
}
