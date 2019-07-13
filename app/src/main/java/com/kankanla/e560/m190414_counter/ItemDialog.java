package com.kankanla.e560.m190414_counter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class ItemDialog extends DialogFragment implements View.OnClickListener {
    private final String T = "### - ItemDialog";
    public static String itemNO;
    private AlertDialog.Builder builder;
    private EditText editText, editTextx, editTextStep;
    private TextView textView;

    private Switch switch1;
    protected Button buttonExit;
    private ImageView[] imageViews;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.i(T, "onCreateDialog");
        imageViews = new ImageView[12];
        if (builder == null) {
            MySurfaceView.isMenuOpen = true;
            builder = new AlertDialog.Builder(getActivity());
            builder.setView(getLayoutInflater().inflate(R.layout.itemdialog, null));
            return builder.create();
        }
        return null;
    }

    @Override
    public void onStart() {
        Log.i(T, "onStart");
        super.onStart();
        sharedPreferences = getActivity().getSharedPreferences(itemNO, MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getDialog().setTitle(itemNO);
        editTextx = getDialog().findViewById(R.id.cont1);
        editTextx.getText();
        editTextx.setText(String.valueOf(sharedPreferences.getInt("No", 0)));
        switch1 = getDialog().findViewById(R.id.switch1);
        textView = getDialog().findViewById(R.id.kankanla);

        /*モード設定スイッチ*/
        switch1.setChecked(sharedPreferences.getBoolean("mode", false));
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean("mode", isChecked);
                    switch1.setChecked(isChecked);
                } else {
                    editor.putBoolean("mode", isChecked);
                    switch1.setChecked(isChecked);
                }
            }
        });
        editor.apply();
        getDialog().show();


        textView.setText(Html.fromHtml("<u>" + getString(R.string.kankanla) + "<u>"));
        Pattern pattern = Pattern.compile("");
        String url = getString(R.string.PlayURL);
        Linkify.addLinks(textView, pattern, url, null, null);//
    }

    @Override
    public void onResume() {
        super.onResume();
        icon_init2();
        editText = getDialog().findViewById(R.id.ComentEdit);
        editText.setHint(getString(R.string.comment));
        editText.setText(sharedPreferences.getString("Comment", ""));
        editTextStep = getDialog().findViewById(R.id.step1);
        editTextStep.setText(String.valueOf(sharedPreferences.getInt("step", 1)));
        buttonExit = getDialog().findViewById(R.id.dialogExit);
        buttonExit.setText(getString(R.string.save));

        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("Comment", editText.getText().toString());
                if (String.valueOf(editTextx.getText()).equals("")) {
                    editor.putInt("No", 0);
                } else {
                    if (Integer.valueOf(String.valueOf(editTextx.getText())) > 9999999) {
                        editor.putInt("No", 9999999);
                    } else {
                        editor.putInt("No", Integer.valueOf(String.valueOf(editTextx.getText())));
                    }
                }

                if (String.valueOf(editTextStep.getText()).equals("")) {
                    editor.putInt("step", 1);
                } else {
                    editor.putInt("step", Integer.valueOf(String.valueOf(editTextStep.getText())));
                }

                editor.apply();
                dismiss();
            }
        });
    }

    int[] xmlid;

    private void icon_init2() {
        xmlid = new int[]{R.id.icon0, R.id.icon1, R.id.icon2, R.id.icon3, R.id.icon4, R.id.icon5, R.id.icon6,
                R.id.icon7, R.id.icon8, R.id.icon9, R.id.icon10, R.id.icon11};
        for (int i = 0; i < imageViews.length; i++) {
            imageViews[i] = getDialog().findViewById(xmlid[i]);
            imageViews[i].setImageResource(MySurfaceView.iconid[i]);
            imageViews[i].setAlpha(0.3f);
            imageViews[i].setOnClickListener(this);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        Log.i(T, "onCancel");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(T, "onDestroy");
        MySurfaceView.isMenuOpen = false;
        dismiss();
    }

    @Override
    public void onClick(View v) {
        for (ImageView i : imageViews) {
            i.setAlpha(0.3f);
        }

        for (int i = 0; i < MySurfaceView.iconid.length; i++) {
            if (v.getId() == xmlid[i]) {
                editor.putInt("iConIndex", i);
                imageViews[i].setAlpha(1f);
            }
        }
        editor.apply();
    }
}