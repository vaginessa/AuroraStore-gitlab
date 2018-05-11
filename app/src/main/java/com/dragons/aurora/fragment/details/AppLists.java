package com.dragons.aurora.fragment.details;

import android.app.SearchManager;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import com.dragons.aurora.R;
import com.dragons.aurora.activities.ClusterActivity;
import com.dragons.aurora.activities.SearchActivity;
import com.dragons.aurora.fragment.DetailsFragment;
import com.dragons.aurora.model.App;
import com.percolate.caffeine.ViewUtils;

public class AppLists extends AbstractHelper {

    public AppLists(DetailsFragment fragment, App app) {
        super(fragment, app);
    }

    @Override
    public void draw() {
        if (null == app.getRelatedLinks())
            return;

        int i = 0;
        for (String label : app.getRelatedLinks().keySet()) {
            if (i == 0) addAppsByThisDeveloper();
            if (i == 1) addAppsSimilar(app.getRelatedLinks().get(label), label);
            if (i == 2) addAppsRecommended(app.getRelatedLinks().get(label), label);
            i++;
        }
    }

    private void addAppsSimilar(String URL, String Label) {
        ViewUtils.findViewById(fragment.getActivity(), R.id.apps_recommended_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = fragment.getActivity().findViewById(R.id.apps_similar);
        imageView.setOnClickListener(v -> ClusterActivity.start(fragment.getActivity(), URL, Label));
    }

    private void addAppsRecommended(String URL, String Label) {
        ViewUtils.findViewById(fragment.getActivity(), R.id.apps_similar_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = fragment.getActivity().findViewById(R.id.apps_recommended);
        imageView.setOnClickListener(v -> ClusterActivity.start(fragment.getActivity(), URL, Label));
    }

    private void addAppsByThisDeveloper() {
        ViewUtils.findViewById(fragment.getActivity(), R.id.apps_by_same_developer_cnt).setVisibility(View.VISIBLE);
        ImageView imageView = fragment.getActivity().findViewById(R.id.apps_by_same_developer);
        imageView.setVisibility(View.VISIBLE);
        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(fragment.getActivity(), SearchActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, SearchActivity.PUB_PREFIX + app.getDeveloperName());
            fragment.getActivity().startActivity(intent);
        });
    }
}