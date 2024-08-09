package org.gfs.chat.components.webrtc.participantslist;

import org.gfs.chat.R;
import org.gfs.chat.util.adapter.mapping.LayoutFactory;
import org.gfs.chat.util.adapter.mapping.MappingAdapter;

public class CallParticipantsListAdapter extends MappingAdapter {

  CallParticipantsListAdapter() {
    registerFactory(CallParticipantsListHeader.class, new LayoutFactory<>(CallParticipantsListHeaderViewHolder::new, R.layout.call_participants_list_header));
    registerFactory(CallParticipantViewState.class, new LayoutFactory<>(CallParticipantViewHolder::new, R.layout.call_participants_list_item));
  }

}
