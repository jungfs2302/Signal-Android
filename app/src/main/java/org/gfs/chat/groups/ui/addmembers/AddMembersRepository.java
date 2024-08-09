package org.gfs.chat.groups.ui.addmembers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.gfs.chat.contacts.SelectedContact;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.groups.GroupId;
import org.gfs.chat.recipients.RecipientId;

final class AddMembersRepository {

  private final Context context;
  private final GroupId groupId;

  AddMembersRepository(@NonNull GroupId groupId) {
    this.groupId = groupId;
    this.context = AppDependencies.getApplication();
  }

  @WorkerThread
  RecipientId getOrCreateRecipientId(@NonNull SelectedContact selectedContact) {
    return selectedContact.getOrCreateRecipientId(context);
  }

  @WorkerThread
  String getGroupTitle() {
    return SignalDatabase.groups().requireGroup(groupId).getTitle();
  }
}
