package com.tripledip.wordherdserver

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentSkipListSet

interface WordRepository {
    fun all(): Set<String>
    fun add(word: String): Boolean
}

@Repository
class InMemoryWordRepository : WordRepository {
    private val words: MutableSet<String> = ConcurrentSkipListSet()
    override fun all(): Set<String> = words.toSet()
    override fun add(word: String): Boolean = words.contains(word) || words.add(word)
}

@Controller
class WordController(val wordRepository: WordRepository, val template: SimpMessagingTemplate) {
    @SubscribeMapping("/all")
    fun startSubscription(): List<String> {
        println("all")
        return wordRepository.all().toList()
    }

    @MessageMapping("/add")
    fun addWord(word: String): List<String> {
        println("addWord: $word")
        if (wordRepository.add(word)) template.convertAndSend("/topic/new", listOf(word))
        return listOf(word)
    }
}
