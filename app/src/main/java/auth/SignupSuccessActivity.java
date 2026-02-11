package auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ConstructionApp.R;

import app.MainActivity;

public class SignupSuccessActivity extends AppCompatActivity {

    private FrameLayout circleContainer;
    private ImageView imgCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_success);

        circleContainer = findViewById(R.id.circleContainer);
        imgCheck = findViewById(R.id.imgCheck);

        startAnimation();
    }

    private void startAnimation() {

        // Animate circle scale (X & Y axis)
        circleContainer.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(600)
                .setInterpolator(new OvershootInterpolator())
                .withEndAction(() -> {

                    // Animate check pop
                    imgCheck.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(400)
                            .setInterpolator(new OvershootInterpolator())
                            .start();

                })
                .start();

        // Redirect after 2.2 seconds
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(
                    SignupSuccessActivity.this,
                    MainActivity.class // change if needed
            );

            startActivity(intent);
            finish();

        }, 2200);
    }
}
