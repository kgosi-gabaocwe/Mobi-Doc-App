package com.example.mobidoc.ui.dashboards;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobidoc.R;
import com.example.mobidoc.ui.Appointment.Doctor_List;
import com.example.mobidoc.ui.MainActivity;
import com.example.mobidoc.utils.Utilities;

import io.paperdb.Paper;


public class Patient_Dashboard extends AppCompatActivity {
    LinearLayout BookAppointment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        BookAppointment = findViewById(R.id.ll_consultation);

        BookAppointment.setOnClickListener(v -> {
            startActivity(new Intent(Patient_Dashboard.this, Doctor_List.class));
            finish();
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.log_out:
                Paper.book().delete(Utilities.USER_KEY);
                Paper.book().delete(Utilities.Doctor);
                startActivity(new Intent(Patient_Dashboard.this , MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}