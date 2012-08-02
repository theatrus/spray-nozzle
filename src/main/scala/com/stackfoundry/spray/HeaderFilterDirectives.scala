package com.stackfoundry.spray

import cc.spray.{Reject, Directives, Pass}

trait HeaderFilterDirectives extends Directives {
  def ajaxRequest = filter { ctx => ctx.request.headers.find(_.name.toLowerCase == "x-requested-with") match {
    case Some(h) => if (h.value.toLowerCase.contains("xmlhttprequest")) Pass else Reject()
    case None => Reject()
  } }
}
