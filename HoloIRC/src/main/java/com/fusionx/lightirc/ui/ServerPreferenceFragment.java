package com.fusionx.lightirc.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.interfaces.IServerSettings;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ServerPreferenceFragment extends PreferenceFragment {
    private IServerSettings mCallback = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (IServerSettings) activity;
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getPreferenceManager().setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        getPreferenceManager().setSharedPreferencesName(mCallback.getFileName());

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);

        mCallback.setupPreferences(getPreferenceScreen(), getActivity());
    }
}