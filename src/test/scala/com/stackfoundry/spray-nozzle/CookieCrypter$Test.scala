package com.stackfoundry.spray-nozzle

import org.specs2.mutable.Specification
import javax.crypto.spec.SecretKeySpec

object DemoKeys {
  val hmacKeySpec = "qnscAdgRlkIhAdPY44oiexBKtQbGY0orf7OV1I50".getBytes()
  val rc4KeySpec = "abcdefghimnoalqi".getBytes()
}

class CookieCrypter$Test extends Specification {

  "Test building a cookiecrypter" should {
    "work" in {
      val t = new CookieCrypter with CookieSHA1 with CookieRC4 {
        protected val hmacKey = DemoKeys.hmacKeySpec
        protected val cipherKey = DemoKeys.rc4KeySpec
      }
      t must not beNull
    }
  }
  "Simple verification" should {
    val t = new CookieCrypter with CookieSHA1 with CookieRC4 {
      protected val hmacKey = DemoKeys.hmacKeySpec
      protected val cipherKey = DemoKeys.rc4KeySpec
    }
    "produce output" in {
      t.output("Hello") must not beNull
    }
    "internally verify" in {
      t.verify(t.output("Hello")) === Some("Hello")
    }
    "support extra" in {
      t.verify(t.output("Hello", extra = "MAGIC"), extra = "MAGIC") === Some("Hello")
    }
    "expire" in {
      t.verify(t.output("Hello", expires = -1)) === None
    }
  }
  "Other algorithms ->" should {
    "AES" in {
      val t = new CookieCrypter with CookieMD5 with CookieAES {
        protected val hmacKey = DemoKeys.hmacKeySpec
        protected val cipherKey = DemoKeys.rc4KeySpec
      }
      t.verify(t.output("I ate a pony")) === Some("I ate a pony")
    }
  }

}
