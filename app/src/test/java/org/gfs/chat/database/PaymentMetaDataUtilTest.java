package org.gfs.chat.database;

import org.junit.Test;
import org.gfs.chat.payments.proto.PaymentMetaData;
import org.gfs.chat.util.Util;

import java.util.Collections;

import okio.ByteString;

import static org.junit.Assert.assertArrayEquals;

public final class PaymentMetaDataUtilTest {

  @Test
  public void extract_single_public_key() {
    byte[] random = Util.getSecretBytes(32);
    byte[] bytes  = PaymentMetaDataUtil.receiptPublic(new PaymentMetaData.Builder().mobileCoinTxoIdentification(new PaymentMetaData.MobileCoinTxoIdentification.Builder().publicKey(Collections.singletonList(ByteString.of(random))).build()).build());

    assertArrayEquals(random, bytes);
  }
}
