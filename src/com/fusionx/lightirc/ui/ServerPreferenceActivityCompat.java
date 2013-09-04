package com.fusionx.lightirc.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.fusionx.lightirc.R;
import com.fusionx.lightirc.constants.PreferenceConstants;
import com.fusionx.lightirc.ui.preferences.NickPreference;
import com.fusionx.lightirc.ui.preferences.ServerTitleEditTextPreference;
import com.fusionx.lightirc.ui.widget.MustBeCompleteView;

import org.apache.commons.lang3.StringUtils;

import static com.fusionx.lightirc.constants.PreferenceConstants.Title;
import static com.fusionx.lightirc.constants.PreferenceConstants.URL;

@SuppressWarnings("deprecation")
public class ServerPreferenceActivityCompat extends ServerPreferenceActivityBase implements
        Preference.OnPreferenceChangeListener {
    // View which notifies user that some fields must be complete
    private MustBeCompleteView mCompleteView = null;
    private ServerTitleEditTextPreference mTitle;
    private EditTextPreference mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesMode(MODE_MULTI_PROCESS);
        getPreferenceManager().setSharedPreferencesName(mFileName);

        addPreferencesFromResource(R.xml.activty_server_settings_prefs);

        mTitle = (ServerTitleEditTextPreference) findPreference(Title);
        mTitle.setOnPreferenceChangeListener(this);
        mTitle.setListOfExistingServers(getIntent().getStringArrayListExtra("list"));

        // URL of server
        mCompleteView = (MustBeCompleteView) findPreference("must_be_complete");

        // URL of server
        mUrl = (EditTextPreference) findPreference(URL);
        mUrl.setOnPreferenceChangeListener(this);

        Preference preference = findPreference("pref_autojoin_intent");
        assert preference != null;
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                final Intent intent = new Intent(ServerPreferenceActivityCompat.this,
                        ChannelListActivity.class);
                intent.putExtra("filename", getFileName());
                startActivity(intent);
                return false;
            }
        });

        if (!canSaveChanges()) {
            setupNewServer();
            mCompleteView.setInitialText(mTitle.getTitle().toString());
        } else {
            getPreferenceScreen().removePreference(mCompleteView);
        }
    }

    private void setupNewServer() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences
                (this);
        final String firstNick = preferences.getString(PreferenceConstants.DefaultFirstNick, "holoirc");
        final String secondNick = preferences.getString(PreferenceConstants.DefaultSecondNick, "");
        final String thirdNick = preferences.getString(PreferenceConstants.DefaultThirdNick, "");

        final String realName = preferences.getString(PreferenceConstants.DefaultRealName,
                "HoloIRCUser");
        final boolean autoNick = preferences.getBoolean(PreferenceConstants.DefaultAutoNickChange, true);

        final NickPreference nickPreference = (NickPreference) getPreferenceManager()
                .findPreference("pref_nick_storage");
        nickPreference.setFirstChoice(firstNick);
        nickPreference.setSecondChoice(secondNick);
        nickPreference.setThirdChoice(thirdNick);

        final EditTextPreference realNamePref = (EditTextPreference) getPreferenceManager()
                .findPreference(PreferenceConstants.RealName);
        realNamePref.setText(realName);
        final CheckBoxPreference autoNickPref = (CheckBoxPreference) getPreferenceManager()
                .findPreference(PreferenceConstants.AutoNickChange);
        autoNickPref.setChecked(autoNick);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!canSaveChanges()) {
            if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mTitle.getTitle().toString());
                setCanSaveChanges(false);
            } else if (StringUtils.isEmpty(mTitle.getText())) {
                mCompleteView.setInitialText(mUrl.getTitle().toString());
                setCanSaveChanges(false);
            } else {
                setCanSaveChanges(true);
                getPreferenceScreen().removePreference(mCompleteView);
            }
        }
        return true;
    }
}