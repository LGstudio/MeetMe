package cz.vutbr.fit.tam.meetme.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cz.vutbr.fit.tam.meetme.R;

/**
 * @author Gabriel Lehocky
 */
public class ArrowView extends LinearLayout{

    public TextView distance;
    public ImageView arrow;
    public TextView contactName;

    public ArrowView(Context context) {
        super(context);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init(){
        inflate(getContext(), R.layout.single_arrow, this);
        setWeightSum(1.0f);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setOrientation(VERTICAL);

        setLayoutParams(new ArrowView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        arrow = (ImageView) findViewById(R.id.arrow);

    }
}
