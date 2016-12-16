package de.volzo.tapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import de.volzo.tapper.GestureDetector.GestureType;


public class Main2Activity extends Activity {

    private SharedPreferences prefs;
    private String prefName = "spinner";
    Integer id = 0;

    private String action;
    private GestureType[] gestureTypes = GestureType.getAllPublicGestureTypes();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main2);

        action = getIntent().getStringExtra("Action");
        ActionTriggers.ActionType type = ActionTriggers.ActionType.valueOf(action);

        prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        id = prefs.getInt(action, -1);

        setActionDescription(type);

        configureSpinner();

    }

    private void configureSpinner() {
        final Spinner sp = (Spinner) findViewById(R.id.spinner2);

        final String[] gestureNames = new String[gestureTypes.length+1];
        gestureNames[0] = "";
        for (int i = 0; i < gestureTypes.length; i++) {
            gestureNames[i+1] = GestureType.getDisplayName(gestureTypes[i]);
        }
        sp.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, gestureNames));

        sp.setSelection(id+1);
        setGestureDescription(id);

        sp.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int pos, long arg3) {
                // TODO Auto-generated method stub
                saveActionPreference(pos-1, sp);
                setGestureDescription(pos-1);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
    }

    @NonNull
    private void setGestureDescription(int pos) {
        final TextView gestureDescription = (TextView) findViewById(R.id.gestureDescription);
        if (pos != -1) {
            gestureDescription.setText(GestureType.getDescription(gestureTypes[pos]));
        } else {
            gestureDescription.setText("");
        }
    }



    private void setActionDescription(ActionTriggers.ActionType type) {
        TextView actionDescription1 = (TextView) findViewById(R.id.actionDescription);
        actionDescription1.setText(ActionTriggers.ActionType.getDescription(type));
    }

    private void saveActionPreference(int pos, Spinner sp) {

        System.out.println("Save preferences");

        prefs = getSharedPreferences(prefName, MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        int previousGestureType = prefs.getInt(action, -1);

        if (pos != -1) {
            String gestureType = gestureTypes[pos].name();
            String previousAction = prefs.getString(gestureType, null);

            //--remove previously saved connected gesture---
            editor.remove(previousAction);


            //-- Add a Toast to say which action was connected to this gesture--
            Toast toast = Toast.makeText(getApplicationContext(),previousAction + " was connected to this gesture",Toast.LENGTH_SHORT);
            toast.show();


            //---save the values in the EditText view to preferences---
            editor.putInt(action, pos);
            editor.putString(gestureType,action);

        } else {
            //--remove previously saved connected gesture---
            editor.remove(action);

            //---save the values in the EditText view to preferences---
            editor.putInt(action, pos);
            if (previousGestureType != -1) {
                editor.remove(gestureTypes[previousGestureType].name());
            }
        }

        //---saves the values---
        editor.commit();
    }

}