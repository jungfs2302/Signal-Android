package org.gfs.chat.jobs;

import androidx.annotation.NonNull;

import com.annimon.stream.Stream;

import org.signal.core.util.logging.Log;
import org.gfs.chat.BuildConfig;
import org.gfs.chat.TextSecureExpiredException;
import org.gfs.chat.attachments.Attachment;
import org.gfs.chat.attachments.DatabaseAttachment;
import org.gfs.chat.contactshare.Contact;
import org.gfs.chat.database.AttachmentTable;
import org.gfs.chat.database.SignalDatabase;
import org.gfs.chat.jobmanager.Job;
import org.gfs.chat.keyvalue.SignalStore;
import org.gfs.chat.mms.OutgoingMessage;
import org.gfs.chat.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SendJob extends BaseJob {

  @SuppressWarnings("unused")
  private final static String TAG = Log.tag(SendJob.class);

  public SendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public final void onRun() throws Exception {
    if (SignalStore.misc().isClientDeprecated()) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP,
                                                         System.currentTimeMillis()));
    }

    Log.i(TAG, "Starting message send attempt");
    onSend();
    Log.i(TAG, "Message send completed");
  }

  protected abstract void onSend() throws Exception;

  protected static void markAttachmentsUploaded(long messageId, @NonNull OutgoingMessage message) {
    List<Attachment> attachments = new LinkedList<>();

    attachments.addAll(message.getAttachments());
    attachments.addAll(Stream.of(message.getLinkPreviews()).map(lp -> lp.getThumbnail().orElse(null)).withoutNulls().toList());
    attachments.addAll(Stream.of(message.getSharedContacts()).map(Contact::getAvatarAttachment).withoutNulls().toList());

    if (message.getOutgoingQuote() != null) {
      attachments.addAll(message.getOutgoingQuote().getAttachments());
    }

    AttachmentTable database = SignalDatabase.attachments();

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }

  protected String buildAttachmentString(@NonNull List<Attachment> attachments) {
    List<String> strings = attachments.stream().map(attachment -> {
      if (attachment instanceof DatabaseAttachment) {
        return ((DatabaseAttachment) attachment).attachmentId.toString();
      } else if (attachment.getUri() != null) {
        return attachment.getUri().toString();
      } else {
        return attachment.toString();
      }
    }).collect(Collectors.toList());

    return Util.join(strings, ", ");
  }
}
