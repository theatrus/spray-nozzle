package us.theatr.spray.nozzle

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
