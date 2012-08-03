package com.stackfoundry.spray_nozzle

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Cipher, Mac}
import sun.misc.{BASE64Decoder, BASE64Encoder}


trait CookieSHA1 {
	val macAlgo = "HmacSHA1"
}

trait CookieMD5 {
	val macAlgo = "HmacMD5"
}

trait CookieAES {
	val cipherAlgo = "AES/CBC/PKCS5Padding"
	val keyType = "AES"
}

trait CookieBlowfish {
	val cipherAlgo = "Blowfish/CBC/PKCS5Padding"
	val keyType = "Blowfish"
}

/**
 * A basic class to handle cookie encryption and verification, with time expiration
 * as required.
 * I apologize for the imperative nature, but thats what you get for using JCE.
 */
trait CookieCrypter {


	val macAlgo : String
	val cipherAlgo : String
	val keyType : String

	protected val defaultExpires: Long = 60*60*24*5

	protected val cipherKey: Array[Byte]
	protected val hmacKey: Array[Byte]

	protected lazy val cipherKeySpec = new SecretKeySpec(cipherKey, keyType)
	protected lazy val hmacKeySpec = new SecretKeySpec(hmacKey, macAlgo)

	def verify(inp: String, extra: String = "") : Option[String] = {
		val decoder = new BASE64Decoder()
		val now = System.currentTimeMillis() / 1000

		val cipher = Cipher.getInstance(cipherAlgo)

		val decoded = decoder.decodeBuffer(inp)
		val (iv, ciphertext) = decoded.splitAt(cipher.getBlockSize)

		cipher.init(Cipher.DECRYPT_MODE, cipherKeySpec, new IvParameterSpec(iv))
		val clear = cipher.doFinal(ciphertext)

		val (rest, msg_time_bytes) = clear.splitAt(clear.size - 4)
		val msg_time = ((msg_time_bytes(0) << 24) & 0xFF000000) | ((msg_time_bytes(1) << 16) & 0xFF0000) | ((msg_time_bytes(2) << 8) & 0xFF00) | (msg_time_bytes(3) & 0xFF)

		val mac = Mac.getInstance(macAlgo)
		mac.init(hmacKeySpec)
		val (hmac, message) = rest.splitAt(mac.getMacLength)
		val computed_hmac = mac.doFinal(message ++ msg_time_bytes ++ extra.getBytes("UTF-8"))

		if (!computed_hmac.sameElements(hmac))
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
		val now_bytes = List((now >> 24) & 0xFF, (now >> 16) & 0xFF, (now >> 8) & 0xFF, now & 0xFF).map(_.toByte).toArray
		val in_bytes = inp.getBytes("UTF-8") ++ now_bytes

		val mac = Mac.getInstance(macAlgo)
		mac.init(hmacKeySpec)
		val hmac = mac.doFinal(in_bytes ++ extra.getBytes("UTF-8"))

		val to_cipher = hmac ++ in_bytes
		val cipher = Cipher.getInstance(cipherAlgo)

		// Generate a random IV
		val iv = Array.fill(cipher.getBlockSize){0.toByte}
		new java.security.SecureRandom().nextBytes(iv)
		val ivspec = new IvParameterSpec(iv)

		cipher.init(Cipher.ENCRYPT_MODE, cipherKeySpec, ivspec)
		val ciphered = cipher.doFinal(to_cipher)

		encoder.encode(iv ++ ciphered)
	}
}
