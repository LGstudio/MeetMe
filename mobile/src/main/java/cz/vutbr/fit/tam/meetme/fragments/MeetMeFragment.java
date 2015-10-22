package cz.vutbr.fit.tam.meetme.fragments;


import android.support.v4.app.Fragment;

import java.util.ArrayList;

import cz.vutbr.fit.tam.meetme.schema.GroupInfo;

/**
 * @author Gabriel Lehocky
 *
 * Should be extended by other fragments that requre data of groups
 */
public class MeetMeFragment extends Fragment {

    protected ArrayList<GroupInfo> groups;

    public void putGroups(ArrayList<GroupInfo> g){
        groups = g;
    }

}
