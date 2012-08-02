package com.stackfoundry.spray

import org.specs2.mutable.Specification
import javax.crypto.spec.SecretKeySpec

object DemoKeys {
  val hmacKeySpec = new SecretKeySpec(
    "qnscAdgRlkIhAdPY44oiexBKtQbGY0orf7OV1I50".getBytes(),
    "HmacSHA1")

  val rc4KeySpec = new SecretKeySpec(
    "abcdefghimnoalqi".getBytes(), "RC4"
  )
}

class CookieCrypter$Test extends Specification {

  "Test building a cookiecrypter" should {
    val t = new CookieCrypter with CookieSHA1 with CookieRC4 {
      protected val hmacKeySpec: SecretKeySpec = DemoKeys.hmacKeySpec
      protected val cipherKeySpec: SecretKeySpec = DemoKeys.rc4KeySpec
    }
    t must not beNull
  }
  "Simple verification should" should {
    val t = new CookieCrypter with CookieSHA1 with CookieRC4 {
      protected val hmacKeySpec: SecretKeySpec = DemoKeys.hmacKeySpec
      protected val cipherKeySpec: SecretKeySpec = DemoKeys.rc4KeySpec
    }
    "produce output" in {
      t.output("Hello") must not beNull
    }
    "internally verify" in {
      t.verify(t.output("Hello")) === Some("Hello")
    }
  }

}
