package clients.chat;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toolbar;
import android.widget.VideoView;
import android.content.Intent;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ConstructionApp.R;

public class WelcomeBookingPage extends AppCompatActivity {

    String otherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_booking_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        otherId = getIntent().getStringExtra("otherId");

        Button btnProceed = findViewById(R.id.btnProceed);
        ImageView back = findViewById(R.id.btnBack);

        back.setOnClickListener(v -> {
            finish();
        });

        btnProceed.setOnClickListener(v -> {
            Intent in = new Intent(this, Permission.class);
            in.putExtra("otherId", otherId);
            startActivity(in);
        });

        VideoView videoView = findViewById(R.id.videoBg);
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.bg_video);

        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
        });

        videoView.start();

    }
}