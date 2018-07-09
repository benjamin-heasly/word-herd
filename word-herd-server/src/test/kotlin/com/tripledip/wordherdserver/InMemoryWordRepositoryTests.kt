package com.tripledip.wordherdserver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest
class InMemoryWordRepositoryTests {

    class TestWordHandler : WordRepository.WordHandler {
        val wordsHandled: MutableSet<String> = HashSet()

        override fun onWordAdded(word: String) {
            wordsHandled.add(word)
        }
    }

    @Test
    fun all() {
        val wordHandler = TestWordHandler()
        val repository = InMemoryWordRepository(wordHandler)

        repository.add("a")
        repository.add("a")
        repository.add("b")
        assertThat(repository.all()).containsExactly("a", "b")
    }

    @Test
    fun add() {
        val wordHandler = TestWordHandler()
        val repository = InMemoryWordRepository(wordHandler)

        assertThat(repository.add("a")).isTrue()
        assertThat(repository.add("a")).isFalse()
        assertThat(repository.add("b")).isTrue()
    }

    @Test
    fun handle() {
        val wordHandler = TestWordHandler()
        val repository = InMemoryWordRepository(wordHandler)

        repository.add("a")
        repository.add("a")
        repository.add("b")
        assertThat(wordHandler.wordsHandled).containsExactly("a", "b")
    }

}
