package org.gfs.chat.keyboard.emoji

import org.gfs.chat.components.emoji.EmojiEventListener
import org.gfs.chat.keyboard.emoji.search.EmojiSearchFragment

interface EmojiKeyboardCallback :
  EmojiEventListener,
  EmojiKeyboardPageFragment.Callback,
  EmojiSearchFragment.Callback
