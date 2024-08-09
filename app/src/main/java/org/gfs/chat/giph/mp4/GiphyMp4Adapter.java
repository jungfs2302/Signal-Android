package org.gfs.chat.giph.mp4;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.gfs.chat.R;
import org.gfs.chat.giph.model.GiphyImage;
import org.gfs.chat.util.adapter.mapping.LayoutFactory;
import org.gfs.chat.util.adapter.mapping.PagingMappingAdapter;

/**
 * Maintains and displays a list of GiphyImage objects. This Adapter always displays gifs
 * as MP4 videos.
 */
final class GiphyMp4Adapter extends PagingMappingAdapter<String> {
  public GiphyMp4Adapter(@Nullable Callback listener) {
    registerFactory(GiphyImage.class, new LayoutFactory<>(v -> new GiphyMp4ViewHolder(v, listener), R.layout.giphy_mp4));
  }

  interface Callback {
    void onClick(@NonNull GiphyImage giphyImage);
  }
}