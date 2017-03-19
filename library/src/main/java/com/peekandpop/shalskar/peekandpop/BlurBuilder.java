package com.peekandpop.shalskar.peekandpop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

public class BlurBuilder {
    private static final float BITMAP_SCALE = 0.2f;
    private static final float BLUR_RADIUS = 6.0f;

    public static Bitmap blur(View v) {
        return blur(v.getContext(), getScreenshot(v));
    }

    public static Bitmap blur(Context ctx, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = null;
        Allocation tmpIn = null;
        Allocation tmpOut = null;
        ScriptIntrinsicBlur intristicBlur = null;

        try {
            rs = RenderScript.create(ctx);
            intristicBlur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
            tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
            tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
            intristicBlur.setRadius(BLUR_RADIUS);
            intristicBlur.setInput(tmpIn);
            intristicBlur.forEach(tmpOut);
            tmpOut.copyTo(outputBitmap);
        } finally {
            if(rs != null) {
                rs.destroy();
            }

            if(tmpIn != null) {
                tmpIn.destroy();
            }

            if(tmpOut != null) {
                tmpOut.destroy();
            }

            if(intristicBlur != null) {
                intristicBlur.destroy();
            }
        }

        return darkenBitmap(outputBitmap);
    }

    private static Bitmap getScreenshot(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }

    private static Bitmap darkenBitmap(Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Color.BLACK);
        ColorFilter filter = new LightingColorFilter(0xAAAAAA, 0x000000);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        return bitmap;
    }
}