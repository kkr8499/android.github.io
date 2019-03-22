package com.example.rudramki.imageviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class ImageViewMainActivity extends AppCompatActivity {
    private static final int REQUEST_OPEN_RESULT_CODE = 0;
    ImageView mImageView;
    PinchZoomImageView mPinchZoomImageView;
    private Uri mImageUri;
    private Animator mCurrentAnimator;
    private int mLongAnimationDuration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view_main);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mPinchZoomImageView = (PinchZoomImageView) findViewById(R.id.pinchZoomImageView);
        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "image view long press", Toast.LENGTH_SHORT).show();
               // zoomImageFromThumb();
                return true;
            }
        });
        mLongAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_OPEN_RESULT_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if(requestCode == REQUEST_OPEN_RESULT_CODE) {
            Uri uri = null;
            if(resultData != null) {
               // mImageUri = resultData.getData();
               /* try {
                    URL url = new URL("https://www.google.com/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&ved=2ahUKEwiR1vSu4uLcAhVU_GEKHUFSAkYQjRx6BAgBEAU&url=https%3A%2F%2Fen.wikiquote.org%2Fwiki%2FAlbert_Einstein&psig=AOvVaw2j4YA-bomCAovwUXw5UGnx&ust=1534000243789224");
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    mImageView.setImageBitmap(bmp);
                } catch (MalformedURLException mfe) {

                } catch (IOException ioe){

                }*/

                try {
                    Bitmap bitmap = getBitmapFromUri(mImageUri);
                    mImageView.setImageBitmap(bitmap);
                } catch (IOException e){
                    e.printStackTrace();
                }
               // Glide.with(this).load(mImageUri).into(mImageView);
               // Glide.with(this).load(mImageView);
               // Glide.with(this).load(mImageUri).into(mPinchZoomImageView);

            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri,"r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return bitmap;
    }

    private void zoomImageFromThumb() {
        if(mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }
        Glide.with(this).load(mImageUri).into(mPinchZoomImageView);

        Rect startBounds = new Rect();
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();
        mImageView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.container).getGlobalVisibleRect(finalBounds,globalOffset);
        startBounds.offset(globalOffset.x, globalOffset.y);
        finalBounds.offset(globalOffset.x, globalOffset.y);

        float startScale;
        if((float) finalBounds.width()/finalBounds.height() > (float) startBounds.width()/startBounds.height()) {
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }
            mImageView.setAlpha(0f);
            mPinchZoomImageView.setVisibility(View.VISIBLE);

            mPinchZoomImageView.setPivotX(0f);
            mPinchZoomImageView.setPivotY(0f);

        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(mPinchZoomImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(mPinchZoomImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mLongAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animator) {
                super.onAnimationCancel(animator);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

    }
}
