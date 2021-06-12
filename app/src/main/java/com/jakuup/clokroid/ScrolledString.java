package com.jakuup.clokroid;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

public class ScrolledString extends AppCompatActivity {
    public static final int RESULT_EXIT = 1001;
    public static final int RESULT_CLEAR = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolled_string);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment, ExitClearFragment.class, null);
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.commit();

        Bundle params = getIntent().getExtras();
        TextView textScrolledString = findViewById(R.id.textScrolledString);
        textScrolledString.setText(params.getString("textData", "(empty)"));
        textScrolledString.setMovementMethod(new ScrollingMovementMethod());
//        if (params.getBoolean("clearEnabled", false)) {
//        }
    }

    public void returnExit() {
        Intent resultIntent = new Intent();
        setResult(RESULT_EXIT, resultIntent);
        finish();
    }

    public void returnClear() {
        Intent resultIntent = new Intent();
        setResult(RESULT_CLEAR, resultIntent);
        finish();
    }
}
