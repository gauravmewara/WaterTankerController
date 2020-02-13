package com.example.watertankercontroller.CustomViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.example.watertankercontroller.R;

public class RobotoBoldTextView extends AppCompatTextView {
    private Context context;
    public RobotoBoldTextView(Context context) {
        super(context);
        this.context = context;
        Typeface tfs = ResourcesCompat.getFont(context, R.font.roboto_bold);
        setTypeface(tfs);
    }

    public RobotoBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        Typeface tfs = ResourcesCompat.getFont(context, R.font.roboto_bold);
        setTypeface(tfs);
    }

    public RobotoBoldTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        Typeface tfs = ResourcesCompat.getFont(context, R.font.roboto_bold);
        setTypeface(tfs);
    }
}
