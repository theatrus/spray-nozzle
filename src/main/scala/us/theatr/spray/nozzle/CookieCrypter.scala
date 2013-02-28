package us.theatr.spray.nozzle

/*
Copyright 2012 Yann Ramin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import sun.misc.{BASE64Encoder, BASE64Decoder}
import javax.crypto.{Mac, Cipher}

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

	/**
	 * Verify an input String (base64 encoded)
	 * @param inp Input string
	 * @param extra Any extra information to add to the verification (host name, browser, etc)
	 * @return
	 */
	def verify(inp: String, extra: String = "") : Option[String] = {
		val decoder = new BASE64Decoder()
		val now = System.currentTimeMillis() / 1000

		val cipher = Cipher.getInstance(cipherAlgo)

		val decoded = decoder.decodeBuffer(inp)
		val (iv, ciphertext) = decoded.splitAt(cipher.getBlockSize)

		cipher.init(Cipher.DECRYPT_MODE, cipherKeySpec, new IvParameterSpec(iv))
		val clear = cipher.doFinal(ciphertext)

		val (rest, msg_time_bytes) = clear.splitAt(clear.size - 4)
		// Unpack msgtime from its by array
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

	/**
	 * Generate a signed and encrypted output file, which has a specified time to live given in milliseconds
	 * @param inp Raw input string to sign and encrypt. This will be converted to UTF-8 before either operation.
	 * @param extra Extra information to include in the signature but NOT in the output data -
	 *              this same (exact) information must be provided to the verify function. For instance,
	 *              the browser client string can be used to obfuscate non-SSL cookie-snooping attacks.
	 * @param expires Expire after this number of milliseconds. Intervals of less than 1 second are not supported.
	 * @return A base64 encoded string containing the encrypted and signed input string to be given to the client
	 *         and verified with the verify function.
	 */
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

trait CookieSHA1 {
	val macAlgo = "HmacSHA1"
}

trait CookieAES {
	val cipherAlgo = "AES/CBC/PKCS5Padding"
	val keyType = "AES"
}

trait CookieBlowfish {
	val cipherAlgo = "Blowfish/CBC/PKCS5Padding"
	val keyType = "Blowfish"
}

trait CookieMD5 {
	val macAlgo = "HmacMD5"
}

trait CookieSHA256 {
	val macAlgo = "HmacSHA256"
}
