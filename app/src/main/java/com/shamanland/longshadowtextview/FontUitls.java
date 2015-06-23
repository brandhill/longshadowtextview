
package com.shamanland.longshadowtextview;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

import java.util.Hashtable;

public class FontUitls {

    private static Hashtable<String, Typeface> sFontCache = new Hashtable<String, Typeface>();

    public static Typeface getFont(Context context, String name) {
        Typeface tf = null;

        tf = sFontCache.get(name);
        if (tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/" + name);
            } catch (Exception e) {
                return null;
            }
            if (tf != null) {
                sFontCache.put(name, tf);
            }
        }

        if (tf == null) {
            tf = Typeface.DEFAULT;
        }
        return tf;
    }
}
