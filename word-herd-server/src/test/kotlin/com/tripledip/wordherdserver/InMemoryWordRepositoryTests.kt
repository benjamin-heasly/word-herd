package com.tripledip.wordherdserver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class InMemoryWordRepositoryTests {

    @Autowired
    lateinit var wordRepository: InMemoryWordRepository

    @Test
    fun all() {
        wordRepository.add("a")
        wordRepository.add("a")
        wordRepository.add("b")
        assertEquals(setOf("a", "b"), wordRepository.all())
    }

    @Test
    fun add() {
        assertTrue(wordRepository.add("a"))
        assertTrue(wordRepository.add("a"))
        assertTrue(wordRepository.add("b"))
    }

}
