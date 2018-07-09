package com.tripledip.wordherdserver

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
class AuthenticationChecker {
    @GetMapping("/checkAuth")
    fun checkAuth(principal: Principal): String {
        println("checkAuth for ${principal.name}")
        return "If you can read this, you are authenticated."
    }
}
