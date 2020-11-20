package com.cbit.disha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout mobileNumberLayout,verifyOTPLayout;
    private Button loginButton, verifyButton;
    private EditText mobileNumber, enterOTP;
    private String phoneNumber, otpNumber, verificationCode;
    private CustomAlertDialog customAlertDialog;
    private LoadingBar loadingBar;
    private FirebaseAuth myAuth;
    private long timeRemaining;
    private TextView resendOtpTextView,resendOtpTime, editNumber, showNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBacks;
    private DatabaseReference errorRef;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initializeFields();
        editNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileNumberLayout.setVisibility(View.VISIBLE);
                verifyOTPLayout.setVisibility(View.INVISIBLE);
                countDownTimer.cancel();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phoneNumber = mobileNumber.getText().toString().trim();
                if (phoneNumber.isEmpty())
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.enter_mobile_number), Toast.LENGTH_SHORT).show();
                else if(phoneNumber.length() != 10)
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.enter_valid_mobile_number), Toast.LENGTH_SHORT).show();
                else {
                    phoneNumber = "+91" + " " + phoneNumber;
                    showNumber.setText(phoneNumber);
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(getResources().getString(R.string.you_entered) + " " +phoneNumber + "\n\n" + getResources().getString(R.string.is_this_ok));
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i){
                            customAlertDialog.showAlertDialog(getResources().getString(R.string.authenticating),1);
                            PhoneAuthProvider.verifyPhoneNumber(PhoneAuthOptions
                                    .newBuilder(FirebaseAuth.getInstance())
                                    .setActivity(LoginActivity.this)
                                    .setPhoneNumber(phoneNumber)
                                    .setTimeout(60L, TimeUnit.SECONDS)
                                    .setCallbacks(mCallBacks)
                                    .build());
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.edit), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                    builder.show();
                }
            }
        });
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                otpNumber = enterOTP.getText().toString();
                if(otpNumber.isEmpty())
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.enter_otp), Toast.LENGTH_SHORT).show();
                else
                {
                    customAlertDialog.showAlertDialog(getResources().getString(R.string.verifying_otp),3);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode,otpNumber);
                    signInWithPhoneCredential(credential);
                }
            }
        });
        mCallBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                customAlertDialog.dismissAlertDialog();
                customAlertDialog.showAlertDialog(getResources().getString(R.string.verifying_mobile_number),3);
                signInWithPhoneCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                customAlertDialog.dismissAlertDialog();
                if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.too_may_attempts), Toast.LENGTH_SHORT).show();
                    resendOtpTextView.setText(getResources().getString(R.string.try_again_later));
                    loadingBar.dismissLoadingBar();
                    resendOtpTime.setText("");
                } else if (e instanceof FirebaseAuthInvalidCredentialsException)
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.enter_valid_mobile_number), Toast.LENGTH_SHORT).show();
                else if (e instanceof FirebaseApiNotAvailableException)
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.install_latest_google_play), Toast.LENGTH_SHORT).show();
                else if (e instanceof FirebaseNetworkException)
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                else {
                    errorRef.child("onVerificationFailed").setValue(e.toString());
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_try_after_some_time), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull final PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.otp_sent_to_mobile), Toast.LENGTH_SHORT).show();
                verificationCode = s;
                customAlertDialog.dismissAlertDialog();
                loadingBar.dismissLoadingBar();
                mobileNumberLayout.setVisibility(View.INVISIBLE);
                verifyOTPLayout.setVisibility(View.VISIBLE);

                timeRemaining = 60000;
                countDownTimer = new CountDownTimer(timeRemaining,1000) {
                    @Override
                    public void onTick(long l) {
                        timeRemaining = l;
                        updateTimer();
                    }
                    @Override
                    public void onFinish() {
                        resendOtpTextView.setTextColor(Color.BLUE);
                        resendOtpTextView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(resendOtpTextView.getCurrentTextColor() == Color.BLUE) {
                                    loadingBar.showLoadingBar(1);
                                    PhoneAuthProvider.verifyPhoneNumber(
                                            PhoneAuthOptions
                                                    .newBuilder(FirebaseAuth.getInstance())
                                                    .setActivity(LoginActivity.this)
                                                    .setPhoneNumber(phoneNumber)
                                                    .setTimeout(60L, TimeUnit.SECONDS)
                                                    .setCallbacks(mCallBacks)
                                                    .setForceResendingToken(forceResendingToken)
                                                    .build());
                                    resendOtpTextView.setTextColor(Color.BLACK);
                                }
                            }
                        });
                    }
                }.start();
                enterOTP.requestFocus();
            }
        };
    }
    private void updateTimer() {
        int minutes = (int) timeRemaining / 60000;
        int seconds = (int) timeRemaining % 60000 / 1000;
        String timeLeft = "0" + minutes + ":";
        if(seconds < 10)
            timeLeft += "0";
        timeLeft += seconds;
        resendOtpTime.setText(timeLeft);
    }

    private void signInWithPhoneCredential(PhoneAuthCredential phoneAuthCredential) {
        myAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                customAlertDialog.dismissAlertDialog();
                if(task.isSuccessful()) {
                    sendUserToMainActivity();
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.welcome_text), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Exception e = task.getException();
                    if (e instanceof FirebaseAuthInvalidCredentialsException)
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.invalid_otp), Toast.LENGTH_SHORT).show();
                    else if(e instanceof FirebaseNetworkException)
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    else {
                        errorRef.child("signInWithCredential").setValue(e.toString());
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_try_after_some_time), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    private void sendUserToMainActivity() {
        Intent homeIntent = new Intent(LoginActivity.this,MainActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(homeIntent);
    }

    private void initializeFields() {
        myAuth = FirebaseAuth.getInstance();
        customAlertDialog = new CustomAlertDialog(LoginActivity.this);
        mobileNumberLayout = findViewById(R.id.mobile_number_layout);
        verifyOTPLayout = findViewById(R.id.verify_otp_layout);
        mobileNumber = findViewById(R.id.mobile_number_edit_text);
        loginButton = findViewById(R.id.login_button);
        editNumber = findViewById(R.id.edit_number);
        enterOTP = findViewById(R.id.enter_otp);
        showNumber = findViewById(R.id.show_mobile_number);
        resendOtpTextView = findViewById(R.id.resend_otp_text_view);
        resendOtpTime = findViewById(R.id.resend_otp_time);
        verifyButton = findViewById(R.id.verify_button);
        loadingBar = new LoadingBar(LoginActivity.this);
        errorRef = FirebaseDatabase.getInstance().getReference().child("Errors").child("LoginActivity");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(myAuth.getCurrentUser() != null) {
            sendUserToMainActivity();
        }
    }
}