package org.gfs.chat.components.emoji.parsing

import org.gfs.chat.emoji.EmojiPage

data class EmojiDrawInfo(val page: EmojiPage, val index: Int, val emoji: String, val rawEmoji: String?, val jumboSheet: String?)
