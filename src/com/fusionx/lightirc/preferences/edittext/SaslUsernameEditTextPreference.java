package com.fusionx.lightirc.preferences.edittext;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

import org.apache.commons.lang3.StringUtils;

public class SaslUsernameEditTextPreference extends EditTextPreference {
    public SaslUsernameEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(final String text) {
        super.setText(text);

        setSummary(StringUtils.isEmpty(text) ? "SASL will be used if supported by the server" :
                text);
    }
}