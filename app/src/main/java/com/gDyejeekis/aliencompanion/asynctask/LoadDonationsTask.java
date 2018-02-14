package com.gDyejeekis.aliencompanion.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gDyejeekis.aliencompanion.models.Donation;
import com.gDyejeekis.aliencompanion.utils.GeneralUtils;
import com.gDyejeekis.aliencompanion.views.adapters.DonationListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by George on 1/30/2018.
 */

public class LoadDonationsTask extends AsyncTask<Void, Void, List<Donation>> {
    private Context context;
    private LinearLayout layout;
    private ListView listView;

    public LoadDonationsTask(Context context, LinearLayout layout, ListView listView) {
        this.context = context;
        this.layout = layout;
        this.listView = listView;
    }

    @Override
    protected List<Donation> doInBackground(Void... voids) {
        List<Donation> donations;
        //donations = retrievePastDonations();
        donations = generateSampleDonations();
        sortDonationsByLatest(donations);
        return donations;
    }

    private List<Donation> retrievePastDonations() {
        List<Donation> donations = null;
        // TODO: 1/27/2018
        return donations;
    }

    private List<Donation> generateSampleDonations() {
        List<Donation> donations = new ArrayList<>();
        donations.add(new Donation(null, null, 0.99f, true));
        donations.add(new Donation(null, null, 0.99f, true));
        donations.add(new Donation(null, null, 0.99f, true));
        donations.add(new Donation(null, null, 0.99f, true));
        donations.add(new Donation(null, null, 4.99f, true));
        donations.add(new Donation(null, null, 2.99f, true));
        donations.add(new Donation(null, "Cool beans", 7.99f, true));
        donations.add(new Donation("test donator", "test donation", 1.99f, true));
        donations.add(new Donation("joe", null, 2.99f, true));
        return donations;
    }

    private void sortDonationsByLatest(List<Donation> donations) {
        Collections.sort(donations, new Comparator<Donation>() {
            @Override
            public int compare(Donation d1, Donation d2) {
                if (d1.getCreatedAt() == d2.getCreatedAt())
                    return 0;
                return d1.getCreatedAt() > d2.getCreatedAt() ? -1 : 1;
            }
        });
    }

    @Override
    protected void onPostExecute(List<Donation> donations) {
        if (donations == null || donations.isEmpty()) {
            layout.setVisibility(View.GONE);
        } else {
            layout.setVisibility(View.VISIBLE);
            listView.setAdapter(new DonationListAdapter(context, donations));
            //GeneralUtils.setListViewHeightBasedOnChildren(listView);
        }
    }
}