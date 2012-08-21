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

import org.specs2.mutable.Specification

class CookieCrypter$Test extends Specification {

  "Test building a cookiecrypter" should {
    "work" in {
      val t = new CookieCrypter with CookieSHA1 with CookieAES {
        protected val hmacKey = DemoKeys.hmacKeySpec
        protected val cipherKey = DemoKeys.one28key
      }
      t must not beNull
    }
  }
  "Simple verification" should {
    val t = new CookieCrypter with CookieSHA1 with CookieAES {
      protected val hmacKey = DemoKeys.hmacKeySpec
      protected val cipherKey = DemoKeys.one28key
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
  "Other algorithms" should {
    "AES/MD5" in {
      val t = new CookieCrypter with CookieMD5 with CookieAES {
        protected val hmacKey = DemoKeys.hmacKeySpec
        protected val cipherKey = DemoKeys.one28key
      }
      t.verify(t.output("I ate a pony")) === Some("I ate a pony")
    }
		"longer key" in {
			val t = new CookieCrypter with CookieMD5 with CookieAES {
				protected val hmacKey = DemoKeys.hmacKeySpec
				protected val cipherKey = DemoKeys.two56key
			}
			t.verify(t.output("Long cat")) === Some("Long cat")
		}
		"blowfish" in {
			val t = new CookieCrypter with CookieSHA1 with CookieBlowfish {
				protected val hmacKey = DemoKeys.hmacKeySpec
				protected val cipherKey = DemoKeys.one28key
			}
			t.verify(t.output("I like sushi")) === Some("I like sushi")
		}
		"SHA256" in {
			val t = new CookieCrypter with CookieSHA256 with CookieAES {
				protected val hmacKey = DemoKeys.hmacKeySpec
				protected val cipherKey = DemoKeys.one28key
			}
			t.verify(t.output("More sha")) === Some("More sha")
		}
	}
	"Multiple usages" should {
		"work" in {
			val t = new CookieCrypter with CookieSHA1 with CookieAES {
				protected val hmacKey = DemoKeys.hmacKeySpec
				protected val cipherKey = DemoKeys.one28key
			}
			var count = 0
			var last = ""
			var lastString = ""
			while(count < 100000) {
				lastString = "Bears " + count.toString
				last = t.output(lastString)
				count = count + 1
			}
			t.verify(last) === Some(lastString)
		}
	}
}

object DemoKeys {
	val hmacKeySpec = "qnscAdgRlkIhAdPY44oiexBKtQbGY0orf7OV1I50".getBytes()
	val one28key = "abcdefghimnoalqi".getBytes()
	val two56key = {
		val a = Array.fill(32) {
			0.toByte
		}; new java.security.SecureRandom(a); a
	}
}
