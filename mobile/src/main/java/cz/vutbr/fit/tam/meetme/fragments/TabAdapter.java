package cz.vutbr.fit.tam.meetme.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;


/**
 * Created by Gabriel Lehocky on 15/10/10.
 *
 * Custom PagerAdapter that holds the fragments in an Activity
 */
public class TabAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;

    ArrayList<MeetMeFragment> fragments;

    public TabAdapter(FragmentManager fm) {
        super(fm);
        mNumOfTabs = 0;

        fragments = new ArrayList<>();
    }

    public void addFragment(MeetMeFragment f){
        fragments.add(f);
        mNumOfTabs += 1;
    }

    public void removeFragment(int position){
        fragments.remove(position);
    }

    public void clearAdapter() {
        fragments.clear();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}
