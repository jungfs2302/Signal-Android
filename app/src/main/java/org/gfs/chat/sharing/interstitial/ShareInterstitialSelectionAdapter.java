package org.gfs.chat.sharing.interstitial;

import org.gfs.chat.R;
import org.gfs.chat.util.adapter.mapping.MappingAdapter;
import org.gfs.chat.util.viewholders.RecipientViewHolder;

class ShareInterstitialSelectionAdapter extends MappingAdapter {
  ShareInterstitialSelectionAdapter() {
    registerFactory(ShareInterstitialMappingModel.class, RecipientViewHolder.createFactory(R.layout.share_contact_selection_item, null));
  }
}
