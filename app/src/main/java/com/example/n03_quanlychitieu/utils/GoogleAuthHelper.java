package com.example.n03_quanlychitieu.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleAuthHelper {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private Context mContext;
    private GoogleSignInListener mListener;

    public interface GoogleSignInListener {
        void onSuccess(GoogleSignInAccount account);

        void onFailure(String errorMessage);

        void onFirebaseAuthSuccess(String uid, GoogleSignInAccount account);
    }

    public GoogleAuthHelper(Context context, String clientId, GoogleSignInListener listener) {
        mContext = context;
        mListener = listener;
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public Intent getSignInIntent() {
        return mGoogleSignInClient.getSignInIntent();
    }

    public void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                mListener.onSuccess(account);
                firebaseAuthWithGoogle(account.getIdToken(), account);
            }
        } catch (ApiException e) {
            String errorMessage = getErrorMessage(e.getStatusCode());
            mListener.onFailure(errorMessage);
            Log.e("GoogleSignIn", "Error code: " + e.getStatusCode(), e);
        }
    }
    // Trong GoogleAuthHelper.java

    private void firebaseAuthWithGoogle(String idToken, GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        mListener.onFirebaseAuthSuccess(uid, account);
                    } else {
                        mListener.onFailure("Firebase authentication failed");
                    }
                });
    }

    public void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            mAuth.signOut();
        });
    }

    private String getErrorMessage(int statusCode) {
        switch (statusCode) {
            case 10:
                return "Lỗi cấu hình. Kiểm tra SHA-1 và package name";
            case 12501:
                return "Đăng nhập bị hủy";
            default:
                return "Lỗi đăng nhập (" + statusCode + ")";
        }
    }
}