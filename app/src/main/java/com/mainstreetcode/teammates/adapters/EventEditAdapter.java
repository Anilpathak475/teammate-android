package com.mainstreetcode.teammates.adapters;

import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.viewholders.BaseItemViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.DateViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.GuestViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.HeaderedImageViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.InputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.MapInputViewHolder;
import com.mainstreetcode.teammates.adapters.viewholders.TeamViewHolder;
import com.mainstreetcode.teammates.fragments.headless.ImageWorkerFragment;
import com.mainstreetcode.teammates.model.Guest;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Item;
import com.mainstreetcode.teammates.model.Team;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseViewHolder;

import java.util.List;

import static com.mainstreetcode.teammates.util.ViewHolderUtil.getItemView;

/**
 * Adapter for {@link com.mainstreetcode.teammates.model.Event  }
 */

public class EventEditAdapter extends BaseRecyclerViewAdapter<BaseViewHolder, EventEditAdapter.EventEditAdapterListener> {

    private static final int GUEST = 12;
    private static final int TEAM = 13;

    private final List<Identifiable> items;

    public EventEditAdapter(List<Identifiable> items, EventEditAdapterListener listener) {
        super(listener);
        setHasStableIds(true);
        this.items = items;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case Item.INPUT:
                return new InputViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.DATE:
                return new DateViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup), adapterListener::canEditEvent);
            case Item.IMAGE:
                return new HeaderedImageViewHolder(getItemView(R.layout.viewholder_item_image, viewGroup), adapterListener);
            case Item.LOCATION:
                return new MapInputViewHolder(getItemView(R.layout.viewholder_location_input, viewGroup), adapterListener);
            case GUEST:
                return new GuestViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), adapterListener);
            case TEAM:
                return new TeamViewHolder(getItemView(R.layout.viewholder_list_item, viewGroup), item -> adapterListener.selectTeam());
            default:
                return new BaseItemViewHolder(getItemView(R.layout.viewholder_simple_input, viewGroup));
        }
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHolder, int i) {
        Object item = items.get(i);

        if (item instanceof Item) ((BaseItemViewHolder) viewHolder).bind((Item) item);
        else if (item instanceof Team) ((TeamViewHolder) viewHolder).bind((Team) item);
        else if (item instanceof Guest) ((GuestViewHolder) viewHolder).bind((Guest) item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId().hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        Object thing = items.get(position);
        return thing instanceof Item
                ? ((Item) thing).getItemType()
                : thing instanceof Team
                ? TEAM
                : GUEST;
    }

    public interface EventEditAdapterListener extends
            TeamAdapter.TeamAdapterListener,
            ImageWorkerFragment.ImagePickerListener {

        void selectTeam();

        void onLocationClicked();

        void rsvpToEvent(Guest guest);

        boolean canEditEvent();
    }

}
