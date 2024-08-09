package org.gfs.chat.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.gfs.chat.dependencies.AppDependencies;
import org.gfs.chat.jobs.MessageFetchJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    AppDependencies.getJobManager().add(new MessageFetchJob());
  }
}
