/*
    HoloIRC - an IRC client for Android

    Copyright 2013 Lalit Maganti

    This file is part of HoloIRC.

    HoloIRC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    HoloIRC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with HoloIRC. If not, see <http://www.gnu.org/licenses/>.
 */

package com.fusionx.lightirc.ui;

import com.fusionx.bus.Subscribe;
import com.fusionx.bus.ThreadType;
import com.fusionx.lightirc.misc.FragmentType;
import co.fusionx.relay.QueryUser;
import co.fusionx.relay.event.query.QueryEvent;
import co.fusionx.relay.parser.UserInputParser;

import java.util.List;

public class UserFragment extends IRCFragment<QueryEvent> {

    public QueryUser getPrivateMessageUser() {
        return (QueryUser) mConversation;
    }

    // Subscription methods
    @Subscribe(threadType = ThreadType.MAIN)
    public void onEventMainThread(final QueryEvent event) {
        if (event.user.getNick().equals(getPrivateMessageUser().getNick())) {
            mMessageAdapter.add(event);
        }
    }

    @Override
    public void onSendMessage(final String message) {
        UserInputParser.onParseUserMessage(mConversation.getServer(), mTitle, message);
    }

    @Override
    public boolean isValid() {
        return mConversation.isValid();
    }

    @Override
    public FragmentType getType() {
        return FragmentType.USER;
    }

    @Override
    protected List<QueryEvent> getAdapterData() {
        return getPrivateMessageUser().getBuffer();
    }
}