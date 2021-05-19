package com.example.mobidoc.ui.dashboards;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.mobidoc.R;
import com.example.mobidoc.ui.Appointment.DoctorViewAcceptedAppointmentsActivity;
import com.example.mobidoc.ui.Appointment.DoctorViewPendingAppointmentsActivity;
import com.example.mobidoc.ui.Appointment.ViewCompletedAppointmentsActivity;
import com.example.mobidoc.ui.MainActivity;
import com.example.mobidoc.ui.profiles.Doctor_ProfileActivity;
import com.example.mobidoc.utils.Utilities;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import io.paperdb.Paper;

public class Doctor_Dashboard extends AppCompatActivity {
    // LinearLayout doctor_profile,doctor_pending_appointments,doctor_completed_appointments,doctor_accepted_appointments;
    BottomNavigationView home_nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor__dashboard);

        ClickNavBar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                Paper.book().delete(Utilities.USER_KEY);
                Paper.book().delete(Utilities.Doctor);
                startActivity(new Intent(Doctor_Dashboard.this, MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void ClickNavBar() {
        home_nav = findViewById(R.id.bottom_navigation2);
        home_nav.setSelectedItemId(R.id.nav_home2);
        home_nav.setOnNavigationItemSelectedListener(item -> {
            Intent activity;
            switch (item.getItemId()) {

                case R.id.nav_home2:
                    return true;
                case R.id.nav_patientrecords:
                    return true;

                case R.id.nav_profile2:
                    activity = new Intent(Doctor_Dashboard.this, Doctor_ProfileActivity.class);
                    startActivity(activity);
                    return true;

                case R.id.nav_pendingappointments:
                    activity = new Intent(Doctor_Dashboard.this, DoctorViewPendingAppointmentsActivity.class);
                    startActivity(activity);
                    return true;

                case R.id.nav_accpetedappointments:

                    activity = new Intent(Doctor_Dashboard.this, DoctorViewAcceptedAppointmentsActivity.class);

                    //activity.putExtra("userType", "Doctor");
                    startActivity(activity);

                    //  activity = new Intent(Doctor_Dashboard.this, DoctorViewAcceptedAppointmentsActivity.class);
                    // startActivity(activity);
                    return true;

            }
            return true;

        });
    }
}


