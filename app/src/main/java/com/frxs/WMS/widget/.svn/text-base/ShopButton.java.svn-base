package com.frxs.WMS.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.frxs.WMS.R;

/**
 * <pre>
 *     author : ewu
 *     e-mail : xxx@xx
 *     time   : 2018/03/14
 *     desc   : xxxx描述
 *     version: 1.0
 * </pre>
 */
public class ShopButton extends LinearLayout {

    private Context context;
    private Animation animation = null;
    private Animator animator;
    private TextView countTv;
    private View placeholderView;
    private int count = 0;

    public ShopButton(Context context) {
        super(context);
        this.context = context;
    }

    public ShopButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        FrameLayout view = (FrameLayout) LayoutInflater.from(getContext()).inflate(R.layout.view_shop_btn, null);
        this.addView(view);
        countTv = (TextView) view.findViewById(R.id.count_tv);
        placeholderView = view.findViewById(R.id.placeholder_view);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)view.getLayoutParams();
        params.height = ((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics()));
        view.setLayoutParams(params);
    }

    public void addCount(int addCount) {
        if (count > 0) {
            count += addCount;
            countTv.setText(String.valueOf(count));
        } else {
            count = addCount;
            placeholderView.setVisibility(View.VISIBLE);
            countTv.setVisibility(View.VISIBLE);

            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(countTv, "scaleX", 0.5f, 1.2f, 1f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(countTv, "scaleY", 0.5f, 1.2f, 1f);
            AnimatorSet set = new AnimatorSet();
            set.play(scaleXAnimator).with(scaleYAnimator);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.setDuration(1000);
            set.start();
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    countTv.setText(String.valueOf(count));
                }
            });

//
//
//            animation = AnimationUtils.loadAnimation(context, R.anim.anim_scale);
//            countTv.setAnimation(animation);
//            animation.start();
        }
    }
}
