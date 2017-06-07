package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamDetailAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Role;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

/**
 * Splash screen
 * <p>
 * Created by Shemanigans on 6/1/17.
 */

public class TeamDetailFragment extends MainActivityFragment
        implements
        View.OnClickListener {

    private static final String ARG_TEAM = "team";
    private static final String ARG_EDITABLE = "editable";

    private Team team;
    private List<Role> roles = new ArrayList<>();

    private RecyclerView recyclerView;

    public static TeamDetailFragment newInstance(Team team, boolean isEditable) {
        TeamDetailFragment fragment = new TeamDetailFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        args.putBoolean(ARG_EDITABLE, isEditable);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = getArguments().getParcelable(ARG_TEAM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_detail, container, false);
        recyclerView = rootView.findViewById(R.id.team_detail);

        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new TeamDetailAdapter(team, roles, isEditable));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (Math.abs(dy) < 3) return;
                toggleFab(dy < 0);
            }
        });

        recyclerView.requestFocus();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean isEditable = getArguments().getBoolean(ARG_EDITABLE, false);
        FloatingActionButton fab = getFab();
        fab.setOnClickListener(this);
        fab.setImageResource(isEditable ? R.drawable.ic_check_white_24dp : R.drawable.ic_group_add_white_24dp);
        setToolbarTitle(getString(isEditable ? R.string.create_team : R.string.join_team));
        toggleFab(true);

        disposables.add(roleViewModel.getRoles().subscribe((currentRoles) -> {
                    System.out.println("Received roles: " + currentRoles);
                    roles.clear();
                    roles.addAll(currentRoles);
                })
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView = null;
        toggleFab(false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                break;
        }
    }
}
