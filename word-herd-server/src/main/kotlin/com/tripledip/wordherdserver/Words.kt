package com.tripledip.wordherdserver

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import java.security.Principal
import java.util.concurrent.ConcurrentHashMap

@Repository
class WordRepositoryRegistry(val template: SimpMessagingTemplate) {
    private val repositories: MutableMap<String, WordRepository> = ConcurrentHashMap()

    class WordHandlerMessenger(val user: String, val template: SimpMessagingTemplate) : WordRepository.WordHandler {
        override fun onWordAdded(word: String) {
            println("on word added: $word")
            template.convertAndSendToUser(user, "/topic/new", listOf(word))
        }
    }

    fun getRepository(user: String): WordRepository =
        if (repositories.containsKey(user)) {
            repositories.get(user)!!
        } else {
            val repository = InMemoryWordRepository(WordHandlerMessenger(user, template))
            repositories.put(user, repository)
            repository
        }
}

@Controller
class WordController(val wordRepositoryRegistry: WordRepositoryRegistry) {
    @SubscribeMapping("/all")
    fun startSubscription(principal: Principal): List<String> {
        val user = principal.name;
        println("start subscription for $user")

        val repository = wordRepositoryRegistry.getRepository(user)
        return repository.all().toList()
    }

    @MessageMapping("/add")
    fun addWord(word: String, principal: Principal) {
        val user = principal.name;
        println("add word $word for $user")

        val repository = wordRepositoryRegistry.getRepository(user)
        repository.add(word)
    }
}
