package org.gfs.chat.database;

interface ThreadIdDatabaseReference {
  void remapThread(long fromId, long toId);
}
