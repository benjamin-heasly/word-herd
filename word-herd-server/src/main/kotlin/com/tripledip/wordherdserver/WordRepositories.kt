package com.tripledip.wordherdserver

import java.util.concurrent.ConcurrentSkipListSet

interface WordRepository {
    fun all(): Set<String>
    fun add(word: String): Boolean

    interface WordHandler {
        fun onWordAdded(word: String)
    }
}

class InMemoryWordRepository(val wordHandler: WordRepository.WordHandler) : WordRepository {
    private val words: MutableSet<String> = ConcurrentSkipListSet()

    override fun all(): Set<String> = words.toSet()

    override fun add(word: String): Boolean =
        if (words.add(word)) {
            wordHandler.onWordAdded(word)
            true
        } else {
            false
        }
}
