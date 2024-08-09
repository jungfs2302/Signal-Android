package org.gfs.chat.keyboard.emoji

import org.gfs.chat.components.emoji.EmojiPageModel
import org.gfs.chat.components.emoji.EmojiPageViewGridAdapter
import org.gfs.chat.components.emoji.RecentEmojiPageModel
import org.gfs.chat.components.emoji.parsing.EmojiTree
import org.gfs.chat.emoji.EmojiCategory
import org.gfs.chat.emoji.EmojiSource
import org.gfs.chat.util.adapter.mapping.MappingModel

fun EmojiPageModel.toMappingModels(): List<MappingModel<*>> {
  val emojiTree: EmojiTree = EmojiSource.latest.emojiTree

  return displayEmoji.map {
    val isTextEmoji = EmojiCategory.EMOTICONS.key == key || (RecentEmojiPageModel.KEY == key && emojiTree.getEmoji(it.value, 0, it.value.length) == null)

    if (isTextEmoji) {
      EmojiPageViewGridAdapter.EmojiTextModel(key, it)
    } else {
      EmojiPageViewGridAdapter.EmojiModel(key, it)
    }
  }
}
