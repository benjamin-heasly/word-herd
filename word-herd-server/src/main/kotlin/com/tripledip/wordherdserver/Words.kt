package com.tripledip.wordherdserver

import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
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
class WordEntrypoint(val wordRepository: WordRepository, val template: SimpMessagingTemplate) {
    @MessageMapping("/add-word")
    fun addWord(word: String): String {
        println("word: $word")
        if (wordRepository.add(word)) template.convertAndSend("/topic/added-word", word)
        return word
    }
}
