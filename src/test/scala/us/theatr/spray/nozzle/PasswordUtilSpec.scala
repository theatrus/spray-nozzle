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


class PasswordUtilSpec extends Specification {
	"Passwords" should {
		"encrypt" in {
			PasswordUtil.securePassword("Hello")._1.length must be_>(2)
		}

		"verify" in {
			val (pw, salt) = PasswordUtil.securePassword("Hello")
			PasswordUtil.checkPassword("Hello", pw, salt) must beTrue
			PasswordUtil.checkPassword("Hi", pw, salt) must beFalse
		}

		"support long passwords" in {
			PasswordUtil.securePassword("HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello")._1.length must be_>(2)
		}


	}

	"Salt" should {
		"be different" in {
			val s1 = PasswordUtil.generateSalt.toSeq
			val s2 = PasswordUtil.generateSalt.toSeq
			s1 must not be equalTo(s2)
			s1 must beEqualTo(s1)
		}
	}
}

