package com.stackfoundry.spray

import javax.crypto.spec.SecretKeySpec
import javax.crypto.{Cipher, Mac}
import sun.misc.{BASE64Decoder, BASE64Encoder}

trait CookieRC4 {
   val cipherAlgo = "RC4"
}

trait CookieSHA1 {
   val macAlgo = "HmacSHA1"
}

/**
 * A basic class to handle cookie encryption and verification, with time expiration
 * as required.
 * I apologize for the imperative nature, but thats what you get for using JCE.
 */
trait CookieCrypter {


  val macAlgo : String
  val cipherAlgo : String

  protected val defaultExpires: Long = 60*60*24*5
  protected val cipherKeySpec: SecretKeySpec
  protected val hmacKeySpec: SecretKeySpec

  def verify(inp: String, extra: String = "") : Option[String] = {
    val decoder = new BASE64Decoder()
    val now = System.currentTimeMillis() / 1000


    val decoded = decoder.decodeBuffer(inp)
    val cipher = Cipher.getInstance(cipherAlgo)
    cipher.init(Cipher.DECRYPT_MODE, cipherKeySpec)
    val clear = cipher.doFinal(decoded)

    val (rest, msg_time_bytes) = clear.splitAt(clear.size - 4)
    val msg_time = (msg_time_bytes(0) << 24) + (msg_time_bytes(1) << 16) + (msg_time_bytes(2) << 8) + (msg_time_bytes(3))


    val mac = Mac.getInstance(macAlgo)
    mac.init(hmacKeySpec)
    val (hmac, message) = rest.splitAt(mac.getMacLength)
    val computed_hmac = mac.doFinal(message ++ extra.getBytes("UTF-8"))
    if (!computed_hmac.equals(hmac))
      None
    else if (msg_time < now)
      None
    else
      Some(new String(message, "UTF-8"))

  }

  def output(inp: String, extra: String = "", expires: Long = defaultExpires) : String = {

    val encoder = new BASE64Encoder()

    // First append the time to the message
    val now = expires + (System.currentTimeMillis() / 1000)
    val now_bytes = List(now >> 24, now >> 16, now >> 8, now).map(_.toByte).toArray
    val in_bytes = inp.getBytes("UTF-8") ++ now_bytes

    val mac = Mac.getInstance(macAlgo)
    mac.init(hmacKeySpec)
    val hmac = mac.doFinal(in_bytes ++ extra.getBytes("UTF-8"))


    val to_cipher = hmac ++ in_bytes

    val cipher = Cipher.getInstance(cipherAlgo)
    cipher.init(Cipher.ENCRYPT_MODE, cipherKeySpec)
    val ciphered = cipher.doFinal(to_cipher)

    encoder.encode(ciphered)
  }
}
