package com.example.shubzz.securitycheck_version1;

/**
 * Created by shubzz on 21/4/18.
 */

import android.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter
{

    public SimpleFragmentPagerAdapter(android.support.v4.app.FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position)
    {
        if (position == 0)
        {
            return new Fragment_Home();
        }

        else
        {
            return new Fragment_Map();
        }
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    //for title of tabs
    @Override
    public CharSequence getPageTitle(int position)
    {
        if (position == 0)
        {
            return "Home";
        }
        else
        {
            return "Map";
        }
    }
}