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

import cc.spray.{MissingCookieRejection, Reject, Directives, Pass}
import cc.spray.directives.{SprayRoute2, SprayRoute1}
import cc.spray.http.{HttpHeaders, HttpCookie}

trait HeaderFilterDirectives extends Directives {
	def ajaxRequest = filter {
		ctx => ctx.request.headers.find(_.name.toLowerCase == "x-requested-with") match {
			case Some(h) => if (h.value.toLowerCase.contains("xmlhttprequest")) Pass else Reject()
			case None => Reject()
		}
	}

	def secureCookie(name: String, crypter: CookieCrypter): SprayRoute1[String] = {
		val directive = headerValue {
			case HttpHeaders.Cookie(cookies) => cookies.find(_.name == name) match {
				case Some(cookie) => crypter.verify(cookie.value) match {
					case Some(value) => Some(value)
					case _ => None
				}
				case _ => None
			}
			case _ => None
		}
		filter1[String] {
			directive.filter(_).mapRejections(_ => MissingCookieRejection(name))
		}
	}

}
