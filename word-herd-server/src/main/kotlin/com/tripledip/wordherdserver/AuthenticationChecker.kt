package com.tripledip.wordherdserver

import org.jboss.logging.Logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class AuthenticationChecker {
    val log = Logger.getLogger(AuthenticationChecker::class.java)

    @GetMapping("/checkAuth")
    fun checkAuth(principal: Principal): String {
        log.info("checkAuth for ${principal.name}")
        return "If you can read this, you are authenticated."
    }
}
