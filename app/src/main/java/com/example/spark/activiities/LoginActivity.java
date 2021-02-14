package com.example.spark.activiities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.spark.R;
import com.example.spark.untils.MyFireBaseServices;
import com.example.spark.untils.MySignal;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private enum  LOGIN_STATE {
        ENTERING_NUMBER,
        ENTERING_CODE,
    }

    private TextInputLayout login_EDT;
    private MaterialButton login_BTN_continue;
    private LOGIN_STATE login_state = LOGIN_STATE.ENTERING_NUMBER;
    private String phoneInput = "";
    private boolean isFailed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseUser user = MyFireBaseServices.getInstance().getFirebaseAuth().getCurrentUser();
        findViews();
        initViews();
        updateUI();
    }

    private void findViews() {
        login_EDT = findViewById(R.id.login_EDT);
        login_BTN_continue = findViewById(R.id.login_BTN_continue);
    }

    private void initViews() {
        login_BTN_continue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueClicked();
            }
        });
    }

    private void continueClicked() {
        if (login_state == LOGIN_STATE.ENTERING_NUMBER)
        {
            startLoginProcess();
        } else if(login_state == LOGIN_STATE.ENTERING_CODE) {
            codeEnteredProcess();
        }
    }

    private void codeEnteredProcess() {
        String verficationCode = login_EDT.getEditText().getText().toString();
        if(!verficationCode.equalsIgnoreCase("")) {
            Log.d("pttt", "verficationCode= "+verficationCode);
            FirebaseAuth firebaseAuth = MyFireBaseServices.getInstance().getFirebaseAuth();
            FirebaseAuthSettings firebaseAuthSettings = firebaseAuth.getFirebaseAuthSettings();
            firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber(phoneInput, verficationCode);

            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneInput)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(onVerificationStateChangedCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } else {
            MySignal.getInstance().toast("No verfication code inserted");
        }
    }

    private void startLoginProcess() {
        phoneInput = login_EDT.getEditText().getText().toString();
        if(!phoneInput.equalsIgnoreCase("")) {
            Log.d("pttt", "phoneInput= "+phoneInput);
            FirebaseAuth firebaseAuth = MyFireBaseServices.getInstance().getFirebaseAuth();
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneInput)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(onVerificationStateChangedCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(options);
        } else {
            MySignal.getInstance().toast("No phone number inserted");
        }
    }

    private void loginWithCredential(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth firebaseAuth =  MyFireBaseServices.getInstance().getFirebaseAuth();
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("pttt", "signInWithCredential:success");
                            isFailed = false;
                            FirebaseUser user = task.getResult().getUser();
                            userSignedIn();
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("pttt", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                isFailed = true;
                                MySignal.getInstance().toast("Wrong Code");
                                updateUI();
                            }
                        }
                    }
                });
    }

    private void userSignedIn() {
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
        finish();
    }

    private void updateUI() {
        if(login_state == LOGIN_STATE.ENTERING_NUMBER) {
            login_EDT.getEditText().setText("");
            login_EDT.setHint(getString(R.string.phone_number));
            login_EDT.setPlaceholderText("+972-50-0000000");
            login_BTN_continue.setText(R.string.continue_);
            if (isFailed) {
                login_EDT.setError("Wrong number.");
            }
            else {
                login_EDT.setError(null);
            }
        } else {
            login_EDT.getEditText().setText("");
            login_EDT.setHint(getString(R.string.enter_code));
            login_EDT.setPlaceholderText("******");
            login_BTN_continue.setText(R.string.login);
            if (isFailed) {
                login_EDT.setError("Wrong code.");
            }
            else {
                login_EDT.setError(null);
            }
        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks onVerificationStateChangedCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            Log.d("pttt", "onCodeSent: verificationId = "+verificationId);
            login_state = LOGIN_STATE.ENTERING_CODE;
            isFailed = false;
            updateUI();
        }

        @Override
        public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
            //Sign in failed - Time out
            Log.d("pttt", "onCodeAutoRetrievalTimeOut " + s);
            super.onCodeAutoRetrievalTimeOut(s);
            isFailed = false;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // Sign in with the credential
            Log.d("pttt", "onVerificationCompleted: ");
            loginWithCredential(phoneAuthCredential);
            isFailed = false;
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // Sign in failed
            Log.d("pttt", "onVerificationFailed! "+e.getMessage());
            e.printStackTrace();
            MySignal.getInstance().toast("Verification Failed! "+e.getMessage());
            login_state=LOGIN_STATE.ENTERING_NUMBER;
            isFailed = true;
            updateUI();
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (login_state == LOGIN_STATE.ENTERING_NUMBER) {
            super.onBackPressed();
        } else if (login_state == LOGIN_STATE.ENTERING_CODE) {
            login_state = LOGIN_STATE.ENTERING_NUMBER;
            updateUI();

        }
    }
}