package com.mainstreetcode.teammate.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamAdapter;
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammate.model.Identifiable;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.ScrollManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Searches for teams
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private View createTeam;
    private final List<Identifiable> teams = new ArrayList<>();

    public static TeamSearchFragment newInstance() {
        TeamSearchFragment fragment = new TeamSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_search, container, false);
        createTeam = rootView.findViewById(R.id.create_team);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamAdapter(teams, this))
                .withStaggeredGridLayoutManager(2)
                .build();

        createTeam.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchItem.expandActionView();
        if (searchView != null) searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        postSearch("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        createTeam = null;
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setToolbarTitle(getString(R.string.team_search));
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    public void onTeamClicked(Team team) {
        showFragment(JoinRequestFragment.joinInstance(team, userViewModel.getCurrentUser()));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_team) showFragment(TeamEditFragment.newCreateInstance());
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        if (getView() == null || TextUtils.isEmpty(queryText)) return true;
        teamViewModel.postSearch(queryText);
        return true;
    }

    private void postSearch(String queryText) {
        if (teamViewModel.postSearch(queryText)) return;
        disposables.add(teamViewModel.findTeams()
                .doOnSubscribe(subscription -> postSearch(queryText))
                .subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    private void onTeamsUpdated(List<Team> teams) {
        ViewGroup parent = (ViewGroup) createTeam.getParent();
        TransitionManager.beginDelayedTransition(parent, new AutoTransition().excludeChildren(RecyclerView.class, true));
        createTeam.setVisibility(View.VISIBLE);

        this.teams.clear();
        this.teams.addAll(teams);
        scrollManager.notifyDataSetChanged();
    }
}