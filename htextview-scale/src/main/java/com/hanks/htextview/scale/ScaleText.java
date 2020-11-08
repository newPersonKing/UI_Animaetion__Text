package com.hanks.htextview.scale;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.hanks.htextview.base.CharacterDiffResult;
import com.hanks.htextview.base.CharacterUtils;
import com.hanks.htextview.base.DefaultAnimatorListener;
import com.hanks.htextview.base.HText;
import com.hanks.htextview.base.HTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Scale
 * Created by hanks on 2017/3/15.
 */

public class ScaleText extends HText {

    float mostCount = 20;
    float charTime = 400;
    private List<CharacterDiffResult> differentList = new ArrayList<>();
    private long duration;
    private ValueAnimator animator;

    @Override
    public void init(HTextView hTextView, AttributeSet attrs, int defStyle) {
        super.init(hTextView, attrs, defStyle);
        animator = new ValueAnimator();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addListener(new DefaultAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationListener != null) {
                    animationListener.onAnimationEnd(mHTextView);
                }
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                mHTextView.invalidate();
            }
        });
        int n = mText.length();
        n = n <= 0 ? 1 : n;
        duration = (long) (charTime + charTime / mostCount * (n - 1));
    }


    @Override
    public void animateText(final CharSequence text) {
        if (mHTextView == null || mHTextView.getLayout() == null)
            return;

        mHTextView.post(new Runnable() {
            @Override
            public void run() {
                if (mHTextView == null || mHTextView.getLayout() == null) {
                    return;
                }
                oldStartX = mHTextView.getLayout().getLineLeft(0);
                ScaleText.super.animateText(text);
            }
        });
    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void animatePrepare(CharSequence text) {
        differentList.clear();
        differentList.addAll(CharacterUtils.diff(mOldText, mText));
    }

    @Override
    protected void animateStart(CharSequence text) {
        int n = mText.length();
        n = n <= 0 ? 1 : n;
        duration = (long) (charTime + charTime / mostCount * (n - 1));
        animator.cancel();
        animator.setFloatValues(0, 1);
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    public void drawFrame(Canvas canvas) {
        float startX = mHTextView.getLayout().getLineLeft(0);
        float startY = mHTextView.getBaseline();
        float offset = startX;
        float oldOffset = oldStartX;
        int maxLength = Math.max(mText.length(), mOldText.length());
        for (int i = 0; i < maxLength; i++) {
            // draw old text
            if (i < mOldText.length()) {
                int move = CharacterUtils.needMove(i, differentList);
                if (move != -1) {
                    mOldPaint.setTextSize(mTextSize);
                    mOldPaint.setAlpha(255);
                    float p = progress * 2f;
                    p = p > 1 ? 1 : p;
                    float distX = CharacterUtils.getOffset(i, move, p, startX, oldStartX, gapList, oldGapList);
                    canvas.drawText(mOldText.charAt(i) + "", 0, 1, distX, startY, mOldPaint);
                } else {
                    mOldPaint.setAlpha((int) ((1 - progress) * 255));
                    mOldPaint.setTextSize(mTextSize * (1 - progress));
                    float width = mOldPaint.measureText(mOldText.charAt(i) + "");
                    canvas.drawText(mOldText.charAt(i) + "", 0, 1, oldOffset + (oldGapList.get(i) - width) / 2, startY, mOldPaint);
                }
                oldOffset += oldGapList.get(i);
            }

            // draw new text
            if (i < mText.length()) {
                if (!CharacterUtils.stayHere(i, differentList)) {
                    int alpha = (int) (255f / charTime * (progress * duration - charTime * i / mostCount));
                    if (alpha > 255) alpha = 255;
                    if (alpha < 0) alpha = 0;

                    float size = mTextSize * 1f / charTime * (progress * duration - charTime * i / mostCount);
                    if (size > mTextSize) size = mTextSize;
                    if (size < 0) size = 0;

                    mPaint.setAlpha(alpha);
                    mPaint.setTextSize(size);

                    float width = mPaint.measureText(mText.charAt(i) + "");
                    canvas.drawText(mText.charAt(i) + "", 0, 1, offset + (gapList.get(i) - width) / 2, startY, mPaint);
                }
                offset += gapList.get(i);
            }
        }
    }

}
