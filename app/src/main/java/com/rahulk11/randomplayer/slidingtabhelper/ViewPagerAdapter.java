package com.rahulk11.randomplayer.slidingtabhelper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.rahulk11.randomplayer.AllSongs;
import com.rahulk11.randomplayer.PlayLists;

/**
 * Created by rahul on 6/22/2017.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    CharSequence tabTitles[];
    int numOfTabs;

    public ViewPagerAdapter(FragmentManager fm, CharSequence tabTitles[], int numOfTabs) {
        super(fm);
        this.tabTitles = tabTitles;
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        if(position == 0){
            AllSongs allSongs = new AllSongs();
            return allSongs;
        } else {
            PlayLists playLists = new PlayLists();
            return playLists;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
