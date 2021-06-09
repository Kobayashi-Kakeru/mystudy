package android.example.bleperipheral;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button)findViewById(R.id.startButton);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == button.getId()){
            Intent intent = new Intent(this, peripheralActivity.class);
            startActivity(intent);
        }
    }
}