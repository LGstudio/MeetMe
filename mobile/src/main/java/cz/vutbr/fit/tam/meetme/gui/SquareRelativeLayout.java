package cz.vutbr.fit.tam.meetme.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author Gabriel Lehocky
 */
public class SquareRelativeLayout extends RelativeLayout {

    public SquareRelativeLayout(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = width > height ? height : width;
        setMeasuredDimension(size, size);
    }

}