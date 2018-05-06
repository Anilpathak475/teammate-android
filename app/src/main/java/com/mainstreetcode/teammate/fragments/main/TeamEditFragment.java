package com.mainstreetcode.teammate.fragments.main;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.mainstreetcode.teammate.R;
import com.mainstreetcode.teammate.adapters.TeamEditAdapter;
import com.mainstreetcode.teammate.baseclasses.HeaderedFragment;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.gofers.Gofer;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamGofer;
import com.mainstreetcode.teammate.viewmodel.gofers.TeamHostingGofer;

import static android.app.Activity.RESULT_OK;

/**
 * Creates, edits or lets a {@link com.mainstreetcode.teammate.model.User} join a {@link Team}
 */

public class TeamEditFragment extends HeaderedFragment<Team>
        implements
        TeamEditAdapter.TeamEditAdapterListener {

    private static final String ARG_TEAM = "team";

    private Team team;
    private TeamGofer gofer;

    public static TeamEditFragment newCreateInstance() {return newInstance(Team.empty());}

    public static TeamEditFragment newEditInstance(Team team) {return newInstance(team);}

    private static TeamEditFragment newInstance(Team team) {
        TeamEditFragment fragment = new TeamEditFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_TEAM, team);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public String getStableTag() {
        return Gofer.tag(super.getStableTag(), getArguments().getParcelable(ARG_TEAM));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = getArguments().getParcelable(ARG_TEAM);
        gofer = teamViewModel.gofer(team);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_headered, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.model_list))
                .withAdapter(new TeamEditAdapter(gofer.getItems(), this))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withLinearLayoutManager()
                .build();

        scrollManager.getRecyclerView().requestFocus();
        return rootView;
    }

    @Override
    public void togglePersistentUi() {
        super.togglePersistentUi();
        setFabClickListener(this);
        setFabIcon(R.drawable.ic_check_white_24dp);
        onRoleUpdated();
    }

    @Override
    public boolean[] insetState() {return VERTICAL;}

    @Override
    public boolean showsFab() {
        return gofer.showsFab();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() != R.id.fab) return;

        toggleProgress(true);
        disposables.add(gofer.save()
                .subscribe(result -> {
                    showSnackbar(gofer.getModelUpdateMessage(this));
                    onModelUpdated(result);
                }, defaultErrorHandler));
    }

    @Override
    protected Team getHeaderedModel() {return team;}

    @Override
    protected TeamHostingGofer<Team> gofer() { return gofer; }

    @Override
    protected void onModelUpdated(DiffUtil.DiffResult result) {
        viewHolder.bind(getHeaderedModel());
        scrollManager.onDiff(result);
        toggleProgress(false);
    }

    @Override
    public void onAddressClicked() {
        if (getActivity() == null) return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);}
        catch (Exception e) {Logger.log(getStableTag(), "Unable to start places api", e);}
    }

    @Override
    public boolean isPrivileged() {
        return gofer.hasPrivilegedRole();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PLACE_PICKER_REQUEST) return;
        if (resultCode != RESULT_OK) return;
        Context context = getContext();
        if (context == null) return;

        toggleProgress(true);
        Place place = PlacePicker.getPlace(context, data);
        disposables.add(locationViewModel.fromPlace(place)
                .subscribe(this::onAddressFound, defaultErrorHandler));
    }

    private void onRoleUpdated() {
        scrollManager.notifyDataSetChanged();
        toggleFab(showsFab());
        setToolbarTitle(gofer.getToolbarTitle(this));
    }

    private void onAddressFound(Address address) {
        team.setAddress(address);
        scrollManager.notifyDataSetChanged();
        toggleProgress(false);
    }
}