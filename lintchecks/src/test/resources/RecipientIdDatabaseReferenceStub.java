package org.gfs.chat.database;

interface RecipientIdDatabaseReference {
  void remapRecipient(RecipientId fromId, RecipientId toId);
}
