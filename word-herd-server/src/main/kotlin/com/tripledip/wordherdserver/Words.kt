package com.tripledip.wordherdserver

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.concurrent.ConcurrentSkipListSet

interface WordRepository {
    fun all(): Set<String>
    fun add(word: String): Boolean
}

@Repository
class InMemoryWordRepository : WordRepository {
    private val words: MutableSet<String> = ConcurrentSkipListSet()
    override fun all(): Set<String> = words.toSet()
    override fun add(word: String): Boolean = words.add(word)
}

@RestController
class AuthenticationChecker {
    @GetMapping("/checkAuth")
    fun checkAuth(principal: Principal): String {
        println("checkAuth for ${principal.name}")
        return "If you can read this, you are authenticated."
    }
}

@Controller
class WordController(val wordRepository: WordRepository, val template: SimpMessagingTemplate) {
    @SubscribeMapping("/all")
    fun startSubscription(principal: Principal): List<String> {
        println("all for ${principal.name}")
        return wordRepository.all().toList()
    }

    @MessageMapping("/add")
    fun addWord(word: String, principal: Principal): List<String> {
        println("addWord: $word for ${principal.name}")
        if (wordRepository.add(word)) template.convertAndSend("/topic/new", listOf(word))
        return listOf(word)
    }
}


// TODO:
// The tests are probably broken by oauth stuff.  Fix them with fake auth test support.
// Scope repositories per user, with a registry by user id
// Add tests with independent users
// Expose UI ROOT property to allow react app cross-origin