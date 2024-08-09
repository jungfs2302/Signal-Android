package org.gfs.chat.database.documents;

import java.util.Set;

public interface Document<T> {
  int size();
  Set<T> getItems();
}
