package com.fusionx.lightirc.ui;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;
import com.fusionx.lightirc.R;
import com.fusionx.lightirc.adapters.IRCPagerAdapter;
import com.fusionx.lightirc.communication.MessageSender;
import com.fusionx.lightirc.constants.FragmentTypeEnum;
import com.fusionx.lightirc.irc.ChannelUser;
import com.fusionx.lightirc.irc.Server;
import com.fusionx.lightirc.irc.event.ConnectedEvent;
import com.fusionx.lightirc.irc.event.JoinEvent;
import com.fusionx.lightirc.irc.event.KickEvent;
import com.fusionx.lightirc.irc.event.NickInUseEvent;
import com.fusionx.lightirc.irc.event.PartEvent;
import com.fusionx.lightirc.irc.event.PrivateMessageEvent;
import com.fusionx.lightirc.irc.event.SwitchToServerEvent;
import com.squareup.otto.Subscribe;

import java.util.List;

public class IRCPagerFragment extends Fragment implements ServerFragment.ServerFragmentCallback,
        ChannelFragment.ChannelFragmentCallback, UserFragment.UserFragmentCallback {
    private ViewPager mViewPager = null;
    private IRCPagerInterface mCallback = null;
    private IRCPagerAdapter mAdapter = null;

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (IRCPagerInterface) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException(activity.toString() + " must implement IRCPagerInterface");
        }
    }

    /**
     * Since the fragment is retained, when the activity detaches, a new activity is created so
     * null the callback when the old activity detaches
     */
    @Override
    public void onDetach() {
        super.onDetach();

        mCallback = null;
    }

    /**
     * Retain the fragment through config changes
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        MessageSender.getSender(mCallback.getServerTitle()).getBus().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MessageSender.getSender(mCallback.getServerTitle()).getBus().unregister(this);
    }

    /**
     * Create the view by inflating a generic view pager
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_pager, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mAdapter == null) {
            mAdapter = new IRCPagerAdapter(getChildFragmentManager());
        }

        final TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]
                {android.R.attr.windowBackground});
        final int background = a.getResourceId(0, 0);
        mViewPager = (ViewPager) getView().findViewById(R.id.pager);
        mViewPager.setBackgroundResource(background);
        mViewPager.setAdapter(mAdapter);
    }

    /**
     * Creates the ServerFragment object
     */
    public void createServerFragment(final String serverTitle) {
        if (mAdapter.getCount() == 0) {
            mAdapter.addServerFragment(serverTitle);
        }
    }

    /**
     * Get the currently displayed fragment
     *
     * @return - returns the currently displayed fragment
     */
    private IRCFragment getCurrentItem() {
        return mAdapter.getItem(mViewPager.getCurrentItem());
    }

    /**
     * Creates a UserFragment with the specified nick
     *
     * @param userNick - the nick of the user we are PMing
     */
    public void createPMFragment(final String userNick) {
        final UserFragment userFragment = new UserFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", userNick);
        userFragment.setArguments(bundle);

        final int position = mAdapter.addFragment(userFragment);

        mViewPager.setCurrentItem(position, true);
    }

    /**
     * Selects the ServerFragment regardless of what is currently selected in the ViewPager
     */
    public void switchToServerFragment() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0, true);
        }
    }

    /**
     * If the currently displayed fragment is the one being removed then switch
     * to one tab back. Then remove the fragment regardless.
     *
     * @param fragmentTitle - name of the fragment to be removed
     */
    public void switchFragmentAndRemove(final String fragmentTitle) {
        final int index = mAdapter.getIndexFromTitle(fragmentTitle);
        if (fragmentTitle.equals(getCurrentTitle())) {
            mViewPager.setCurrentItem(index - 1, true);
        }
        mAdapter.removeFragment(index);
    }

    public void switchToServerAndRemove(final String fragmentTitle) {
        switchToServerFragment();
        mAdapter.removeFragment(mAdapter.getIndexFromTitle(fragmentTitle));
    }

    @Override
    public boolean isConnectedToServer() {
        return mCallback.isConnectedToServer();
    }

    /**
     * Method called when a new ChannelFragment is to be created
     *
     * @param channelName - name of the channel joined
     * @param forceSwitch - whether the channel should be forcibly switched to
     */
    public void createChannelFragment(final String channelName, final boolean forceSwitch) {
        final boolean switchToTab = channelName.equals(getActivity().getIntent().getStringExtra
                ("mention")) || forceSwitch;

        final ChannelFragment channel = new ChannelFragment();
        final Bundle bundle = new Bundle();
        bundle.putString("title", channelName);
        channel.setArguments(bundle);

        final int position = mAdapter.addFragment(channel);

        if (switchToTab) {
            mViewPager.setCurrentItem(position, true);
        }
    }

    public void onMentionRequested(final List<ChannelUser> users) {
        if (getCurrentType().equals(FragmentTypeEnum.Channel)) {
            final ChannelFragment channel = (ChannelFragment) getCurrentItem();
            channel.onUserMention(users);
        }
    }

    public void onUnexpectedDisconnect() {
        mViewPager.setCurrentItem(0, true);

        mAdapter.removeAllButServer();
        mAdapter.disableAllEditTexts();
    }

    public String getCurrentTitle() {
        return getCurrentItem().getTitle();
    }

    public FragmentTypeEnum getCurrentType() {
        return getCurrentItem().getType();
    }

    public void setTabStrip(PagerSlidingTabStrip tabs) {
        tabs.setViewPager(mViewPager);
        mAdapter.setmTabStrip(tabs);
    }

    @Override
    public Server getServer() {
        return mCallback.getServer();
    }

    public void connectedToServer() {
        final ServerFragment fragment = (ServerFragment) mAdapter.getItem(0);
        fragment.onConnectedToServer();
    }

    public interface IRCPagerInterface {
        public Server getServer();

        public boolean isConnectedToServer();

        public String getServerTitle();
    }

    /*
     * Events start here
     */
    @Subscribe
    public void onChannelPart(final PartEvent event) {
        switchFragmentAndRemove(event.channelName);
    }

    @Subscribe
    public void onChannelJoin(final JoinEvent event) {
        createChannelFragment(event.channelToJoin, true);
    }

    @Subscribe
    public void onKicked(final KickEvent event) {
        switchToServerAndRemove(event.channelName);
    }

    @Subscribe
    public void onNewPrivateMessage(final PrivateMessageEvent event) {
        createPMFragment(event.nick);
    }

    @Subscribe
    public void onSwitchToServer(final SwitchToServerEvent event) {
        switchToServerFragment();
    }

    @Subscribe
    public void onServerConnected(final ConnectedEvent event) {
        connectedToServer();
    }

    @Subscribe
    public void onNickInUse(final NickInUseEvent event) {
        switchToServerFragment();
    }
}