package com.example.mobidoc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.paperdb.Paper;

public class DoctorRegisterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private EditText emailET, passwordET, confirmPasswordET, fNameET, lNameET, qualificationsET, experienceET;
    private Spinner specializationSPN;
    private String specialization;
    private TextView showPasswordTW, showConfirmPasswordTW, haveAccountTW;
    private Button registerBTN;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_register);

        networkAvailabilityCheck();
        initializeActivity();
        specializationSpinnerSetUp();

        //Show / Hide Passwords
        showPasswordTW.setText(" ");
        showPasswordTW.setOnClickListener(v -> toggleShowPassword(passwordET, showPasswordTW));
        showConfirmPasswordTW.setText(" ");
        showConfirmPasswordTW.setOnClickListener(v -> toggleShowPassword(confirmPasswordET, showConfirmPasswordTW));


        //if user already has an account switch to login screen
        haveAccountTW.setOnClickListener(v -> {
            startActivity(new Intent(DoctorRegisterActivity.this, Login_activity.class));
            finish();
        });

        //validate details
        registerBTN.setOnClickListener(v -> {
            String email = emailET.getText().toString().trim();
            String password = passwordET.getText().toString();//should we trim here?
            String confirmPassword = confirmPasswordET.getText().toString();
            String fName = fNameET.getText().toString().trim();
            String lName = lNameET.getText().toString().trim();
            String qualifications = qualificationsET.getText().toString().trim();
            String experience = experienceET.getText().toString().trim();

            if (validateDetails(email, password, confirmPassword, fName, lName, qualifications, experience, true)) {
                // Check if user is signed in (non-null) and update UI accordingly.
                registerUser(new Doctor(fName, lName, "Doctor", email, qualifications, experience, specialization), password);
            }
        });

    }

    private void initializeActivity() {
        //set up action bar
        ActionBar actionBar = getSupportActionBar();
        Objects.requireNonNull(actionBar).setTitle("Doctor Account Registration");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        Paper.init(this); //    @ Dylan 2179115 added this Code to store information locally

        //initialize swing elements
        emailET = findViewById(R.id.emailET);
        passwordET = findViewById(R.id.passwordET);
        confirmPasswordET = findViewById(R.id.confirmPasswordET);
        fNameET = findViewById(R.id.fNameET);
        lNameET = findViewById(R.id.lNameET);
        qualificationsET = findViewById(R.id.qualificationsET);
        experienceET = findViewById(R.id.experienceET);
        specializationSPN = findViewById(R.id.specializationSPN);
        registerBTN = findViewById(R.id.registerBTN);
        showPasswordTW = findViewById(R.id.showPasswordTW);
        showConfirmPasswordTW = findViewById(R.id.showConfirmPasswordTW);
        haveAccountTW = findViewById(R.id.haveAccountTW);

        mAuth = FirebaseAuth.getInstance();//get connection to firebase

        //set up progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Doctor...");
    }

    private void networkAvailabilityCheck() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
            startActivity(new Intent(DoctorRegisterActivity.this, No_Internet.class));
        }
    }

    private void specializationSpinnerSetUp() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.doctorSpecializations, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        specializationSPN.setAdapter(adapter);
        specializationSPN.setOnItemSelectedListener(this);
    }
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        specialization = parent.getItemAtPosition(pos).toString();
    }
    public void onNothingSelected(AdapterView<?> parent) {
        specialization = "Other";
    }

    private void toggleShowPassword(EditText password, TextView showPassword) {
        if (showPassword.getText().equals(" ")) {
            showPassword.setText(".");
            password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            password.setSelection(password.length());
            Drawable d = getResources().getDrawable(R.drawable.show_password);
            showPassword.setBackground(d);
        } else {
            showPassword.setText(" ");
            password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            password.setSelection(password.length());
            Drawable d = getResources().getDrawable(R.drawable.hide_password);
            showPassword.setBackground(d);
        }
    }

    private boolean validateEmail(String email, boolean displayErrors) {//if email address is not in valid format, displays error
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (displayErrors) {
                emailET.setError("Invalid Email");
            }
            return false;
        }
        return true;
    }

    private boolean validatePassword(String password, boolean displayErrors) {//checks if password is in correct format, if not displays error message
        //defines patterns and matchers for password security
        Pattern lowerCase = Pattern.compile("\\p{Lower}");//all lowercase letters
        Pattern upperCase = Pattern.compile("\\p{Upper}");//all uppercase letters
        Pattern number = Pattern.compile("\\p{Digit}");//all numbers
        Pattern special = Pattern.compile("\\p{Punct}");//all special characters
        Matcher hasLowerCase = lowerCase.matcher(password);
        Matcher hasUpperCase = upperCase.matcher(password);
        Matcher hasNumber = number.matcher(password);
        Matcher hasSpecial = special.matcher(password);
        //if password has less than 8 or more than 20 characters, does not contain at least one lowercase letter, does not contain at least one uppercase letter,
        //does not contain at least one number or does not contain at least one special character, displays error
        if (password.length() < 8 || password.length() > 20 || !hasLowerCase.find() || !hasUpperCase.find() || !hasNumber.find() || !hasSpecial.find()) {
            if (displayErrors) {
                passwordET.setError("Password must be between 8-20 characters, and must include at least one lowercase letter, uppercase" +
                        " letter, number and special character");
            }
            return false;
        }
        return true;
    }

    private boolean validateConfirmPassword(String confirm_password, String password, boolean displayErrors) {
        if (confirm_password.equals(password)) {
            return true;
        }
        if (displayErrors) {
            confirmPasswordET.setError("Passwords must match");
        }
        return false;
    }

    private boolean validateFName(String fName, boolean displayErrors) {//checks if personal information fields are empty, if so displays the appropriate error(s)
        if (fName.isEmpty()) {
            if (displayErrors) {
                fNameET.setError("First name cannot be empty");
            }
            return false;
        }
        return true;
    }

    private boolean validateLName(String lName, boolean displayErrors) {//checks if personal information fields are empty, if so displays the appropriate error(s)
        if (lName.isEmpty()) {
            if (displayErrors) {
                lNameET.setError("Last name cannot be empty");
            }
            return false;
        }
        return true;
    }

    private boolean validateQualifications(String qualifications, boolean displayErrors) {//checks if personal information fields are empty, if so displays the appropriate error(s)
        if (qualifications.isEmpty()) {
            if (displayErrors) {
                qualificationsET.setError("Qualification(s) cannot be empty");
            }
            return false;
        }
        return true;
    }

    private boolean validateExperience(String experience, boolean displayErrors) {
        if (experience.isEmpty()) {
            if (displayErrors) {
                experienceET.setError("Experience cannot be empty");
            }
            return false;
        }
        Pattern lowerCase = Pattern.compile("\\p{Lower}");//all lowercase letters
        Pattern upperCase = Pattern.compile("\\p{Upper}");//all uppercase letters
        Pattern special = Pattern.compile("\\p{Punct}");//all special characters
        Matcher hasLowerCase = lowerCase.matcher(experience);
        Matcher hasUpperCase = upperCase.matcher(experience);
        Matcher hasSpecial = special.matcher(experience);
        if (hasLowerCase.find() || hasUpperCase.find() || hasSpecial.find()) {
            if (displayErrors) {
                experienceET.setError("Please use only numbers to indicate years of experience");
            }
            return false;
        }
        return true;
    }

    private boolean validateDetails(String email, String password, String confirm_password, String fName, String lName, String qualifications, String experience, boolean displayErrors) {
        boolean email_valid = validateEmail(email, displayErrors);
        boolean password_valid = validatePassword(password, displayErrors);
        boolean confirm_password_valid = validateConfirmPassword(confirm_password, password, displayErrors);
        boolean fName_valid = validateFName(fName, displayErrors);
        boolean lName_valid = validateLName(lName, displayErrors);
        boolean qualifications_valid = validateQualifications(qualifications, displayErrors);
        boolean experience_valid = validateExperience(experience, displayErrors);
        return email_valid && password_valid && confirm_password_valid && fName_valid && lName_valid && qualifications_valid && experience_valid;
    }

    private void registerUser(Doctor doc, String password) {
        progressDialog.show();
        mAuth.createUserWithEmailAndPassword(doc.getEmail(), password)
                .addOnCompleteListener(DoctorRegisterActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        progressDialog.dismiss();
                        FirebaseUser user = mAuth.getCurrentUser();

                        Objects.requireNonNull(user).sendEmailVerification()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(DoctorRegisterActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(DoctorRegisterActivity.this, "Verification email failed", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference(doc.getUser_type() + "s");
                        doc.setUid(user.getUid());
                        reference.child(doc.getUid()).setValue(doc);
                        Toast.makeText(DoctorRegisterActivity.this, "Registered...\n" + user.getEmail(), Toast.LENGTH_SHORT).show();

                        /* ---- Dylan 2179115 added this code ----*/
                        Paper.book().write(Utilities.Doctor, "Doc");
                        startActivity(new Intent(DoctorRegisterActivity.this, Doctor_Dashboard.class));
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Toast.makeText(DoctorRegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }

                }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(DoctorRegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

}