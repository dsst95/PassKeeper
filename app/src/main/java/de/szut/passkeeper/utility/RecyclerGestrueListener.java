package de.szut.passkeeper.utility;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.szut.passkeeper.R;
import de.szut.passkeeper.interfaces.IRecyclerActivity;

/**
 * Created by redtiger on 03.05.15.
 */
public class RecyclerGestrueListener extends GestureDetector.SimpleOnGestureListener {
    private IRecyclerActivity iRecyclerActivity;
    private int recyclerPosition;
    private Context context;
    private RecyclerView view;
    private RecyclerViewAdapter.ViewHolder actualViewHolder;
    private boolean swipingEnabled = true;

    public RecyclerGestrueListener(Context context, IRecyclerActivity iRecyclerActivity, RecyclerView view) {
        this.iRecyclerActivity = iRecyclerActivity;
        this.context = context;
        this.view = view;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        if(actualViewHolder == null || !swipingEnabled) { return false; }
        swipingEnabled = true;
        iRecyclerActivity.onRecyclerItemClick(recyclerPosition);
        return super.onSingleTapUp(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int minSwipeDistance = 30;
        if(actualViewHolder == null || !swipingEnabled) { return false; }
        if (e1.getX() - e2.getX() > minSwipeDistance) { // Right to left swipe
            int distance = (int) (e2.getX() - e1.getX());
            actualViewHolder.deleteAnimView.findViewById(R.id.delete_image_left).setVisibility(View.GONE);
            actualViewHolder.deleteAnimView.findViewById(R.id.delete_image_right).setVisibility(View.VISIBLE);
            View animationView = actualViewHolder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        } else if (e2.getX() - e1.getX() > minSwipeDistance) { // Left to right
            int distance = (int) (e1.getX() - e2.getX());
            actualViewHolder.deleteAnimView.findViewById(R.id.delete_image_left).setVisibility(View.VISIBLE);
            actualViewHolder.deleteAnimView.findViewById(R.id.delete_image_right).setVisibility(View.GONE);
            View animationView = actualViewHolder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = distance;
            params.leftMargin = -distance;
            animationView.setLayoutParams(params);
        }
        return false;
    }

    @Override
    public boolean onFling(final MotionEvent e1, final MotionEvent e2, float velocityX, float velocityY) {
        final int distanceX = (int) (e2.getX() - e1.getX());
        if(actualViewHolder == null || !swipingEnabled) { return false; }
        swipingEnabled = false;
        if (e1.getX() - e2.getX() > actualViewHolder.mainView.getWidth() * 0.80) { // Right to Left
            ValueAnimator animator = ValueAnimator.ofInt(Math.abs(distanceX), actualViewHolder.mainView.getWidth());
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    View animationView = actualViewHolder.mainView;
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                    params.rightMargin = (int) animation.getAnimatedValue();
                    params.leftMargin = -(int) animation.getAnimatedValue();
                    animationView.setLayoutParams(params);
                }
            });
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    actualViewHolder.deleteAnimView.setVisibility(View.GONE);
                    actualViewHolder.delteConfirmationView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else if (e2.getX() - e1.getX() > actualViewHolder.mainView.getWidth() * 0.80) { // Left to Right
            ValueAnimator animator = ValueAnimator.ofInt(Math.abs(distanceX), actualViewHolder.mainView.getWidth());
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    View animationView = actualViewHolder.mainView;
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                    params.rightMargin = -(int) animation.getAnimatedValue();
                    params.leftMargin = (int) animation.getAnimatedValue();
                    animationView.setLayoutParams(params);
                }
            });
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    actualViewHolder.deleteAnimView.setVisibility(View.GONE);
                    actualViewHolder.delteConfirmation.setVisibility(View.VISIBLE);
                    if(actualViewHolder.delteConfirmationView instanceof EditText) {
                        InputMethodManager imm = (InputMethodManager)context.getSystemService(
                                Context.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                        actualViewHolder.delteConfirmationView.requestFocus();
                    }
                    ((TextView)actualViewHolder.delteConfirmation.findViewById(R.id.deltition_yes)).setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (iRecyclerActivity.confirmRemove(actualViewHolder.delteConfirmationView instanceof EditText ? ((EditText)actualViewHolder.delteConfirmationView).getText().toString() : null, recyclerPosition)) {
                               iRecyclerActivity.removeItem(recyclerPosition);
                            } else {
                                actualViewHolder.delteConfirmation.setVisibility(View.GONE);
                                actualViewHolder.deleteAnimView.setVisibility(View.VISIBLE);
                                View animationView = actualViewHolder.mainView;
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                                params.rightMargin = 0;
                                params.leftMargin = 0;
                                animationView.setLayoutParams(params);
                                iRecyclerActivity.onRemoveConfirmationFailed();
                                if(actualViewHolder.delteConfirmationView instanceof EditText) {
                                    ((EditText) actualViewHolder.delteConfirmationView).setText(null);
                                    InputMethodManager imm = (InputMethodManager)context.getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(actualViewHolder.delteConfirmationView.getWindowToken(), 0);
                                }
                            }
                            swipingEnabled = true;
                            return false;
                        }
                    });
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        } else {
            ValueAnimator animator = ValueAnimator.ofInt(Math.abs(distanceX), 0);
            animator.setDuration(500);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    View animationView = actualViewHolder.mainView;
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
                    params.rightMargin = distanceX > 0 ? -(int) animation.getAnimatedValue() : (int) animation.getAnimatedValue();
                    params.leftMargin = distanceX > 0 ? (int) animation.getAnimatedValue() : -(int) animation.getAnimatedValue();
                    animationView.setLayoutParams(params);
                }
            });
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    actualViewHolder = null;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && swipingEnabled) {
            actualViewHolder = (RecyclerViewAdapter.ViewHolder) view.getChildViewHolder(childView);
            recyclerPosition = view.getChildAdapterPosition(childView);
        }
        return true;
    }
}