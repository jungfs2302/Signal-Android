package org.gfs.chat.stickers;

import androidx.annotation.NonNull;

import org.gfs.chat.database.model.StickerRecord;

public interface StickerEventListener {
  void onStickerSelected(@NonNull StickerRecord sticker);

  void onStickerManagementClicked();
}
