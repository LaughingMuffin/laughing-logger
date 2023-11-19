package org.laughing.logger.util;

import android.content.Context;

/**
 * Util
 */
public class Util {
    public static int convertDpToPx(Context context, float dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density
                + 0.5f);
    }
}
