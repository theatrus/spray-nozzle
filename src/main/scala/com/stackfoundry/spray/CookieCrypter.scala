package com.stackfoundry.spray

import javax.crypto.spec.SecretKeySpec
import javax.crypto.{Cipher, Mac}
import sun.misc.{BASE64Decoder, BASE64Encoder}

object CookieCrypter {
  private[this] val hmacKeySpec = new SecretKeySpec(
    "qnscAdgRlkIhAdPY44oiexBKtQbGY0orf7OV1I50".getBytes(),
    "HmacSHA1")

  private[this] val rc4KeySpec = new SecretKeySpec(
    "qnscAdgRlkIhAUPY4qoiexBKtQbGY0orf7OV1I50".getBytes(),
    "RC4"
  )

  val allowTimeSkew = 60*60*24*5 // 5 days

  def verify(inp: String, extra: String = "") : Option[String] = {
    val decoder = new BASE64Decoder()
    val now = System.currentTimeMillis() / 1000


    val decoded = decoder.decodeBuffer(inp)
    val cipher = Cipher.getInstance("RC4")
    cipher.init(Cipher.DECRYPT_MODE, rc4KeySpec)
    val clear = cipher.doFinal(decoded)

    val (rest, msg_time_bytes) = clear.splitAt(clear.size - 4)
    val msg_time = (msg_time_bytes(0) << 24) + (msg_time_bytes(1) << 16) + (msg_time_bytes(2) << 8) + (msg_time_bytes(3))


    val mac = Mac.getInstance("HmacSHA1")
    val (hmac, message) = rest.splitAt(mac.getMacLength)
    val computed_hmac = mac.doFinal(message ++ extra.getBytes("UTF-8"))
    if (!computed_hmac.equals(hmac))
      None
    else if (msg_time + allowTimeSkew < now)
      None
    else
      Some(new String(message, "UTF-8"))

  }

  def output(inp: String, extra: String = "") : String = {

    val encoder = new BASE64Encoder()

    // First append the time to the message
    val now = System.currentTimeMillis() / 1000
    val now_bytes = List(now >> 24, now >> 16, now >> 8, now).map(_.toByte).toArray
    val in_bytes = inp.getBytes("UTF-8") ++ now_bytes

    val mac = Mac.getInstance("HmacSHA1")
    mac.init(hmacKeySpec)
    val hmac = mac.doFinal(in_bytes ++ extra.getBytes("UTF-8"))


    val to_cipher = hmac ++ in_bytes

    val cipher = Cipher.getInstance("RC4")
    cipher.init(Cipher.ENCRYPT_MODE, rc4KeySpec)
    val ciphered = cipher.doFinal(to_cipher)

    encoder.encode(ciphered)
  }
}
