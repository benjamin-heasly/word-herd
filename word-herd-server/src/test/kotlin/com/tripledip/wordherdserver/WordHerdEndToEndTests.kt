package com.tripledip.wordherdserver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("insecure")
class WordHerdEndToEndTests {

    @LocalServerPort
    var port: Int? = null

    @Autowired
    lateinit var wordRepositoryRegistry: WordRepositoryRegistry

    @Autowired
    lateinit var messageConverter: MessageConverter

    fun stompClient(): WebSocketStompClient {
        val client = WebSocketStompClient(SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient()))))
        client.messageConverter = messageConverter
        return client
    }

    open class WordHandler(count: Int) : StompSessionHandlerAdapter() {
        val anyActivityLatch = CountDownLatch(1)
        val wordLatch = CountDownLatch(count)
        fun await() = anyActivityLatch.await(5, TimeUnit.SECONDS) && wordLatch.await(5, TimeUnit.SECONDS)

        val words: MutableSet<String> = ConcurrentSkipListSet()
        fun wordsHandled() = words.toSet()

        override fun handleFrame(headers: StompHeaders, payload: Any?) {
            println("handle words: $payload")
            val wordList = payload as List<String>
            wordList.forEach {
                words.add(it)
                wordLatch.countDown()
            }
            anyActivityLatch.countDown()
        }

        override fun getPayloadType(headers: StompHeaders): Type = List::class.java
    }

    class WordSession(port: Int, client: WebSocketStompClient, expectedExisting: Int, expectedNew: Int) {
        val allHandler = WordHandler(expectedExisting)
        val newHandler = WordHandler(expectedNew)

        val session = client
            .connect("ws://localhost:$port/words", WordHandler(0))
            .get(5, TimeUnit.SECONDS)

        fun subscribe() {
            session.subscribe("/user/topic/new", newHandler)
            session.subscribe("/app/all", allHandler)
            allHandler.await()
        }

        fun send(word: String) = session.send("/app/add", word)
    }

    @Test
    @DirtiesContext
    fun clientReceivesExistingWordsOnSubscription() {
        val existingWords = setOf("existing1", "existing2", "existing3", "existing4")

        val wordRepository = wordRepositoryRegistry.getRepository("anonymous")
        existingWords.forEach { wordRepository.add(it) }
        assertThat(wordRepository.all()).containsExactlyElementsOf(existingWords)

        val session = WordSession(port!!, stompClient(), existingWords.size, 0)
        session.subscribe()
        assertThat(session.allHandler.wordsHandled()).containsExactlyElementsOf(existingWords)
    }

    @Test
    @DirtiesContext
    fun clientReceivesWordsAddedBySelf() {
        val selfWords = setOf("self1", "self2", "self3", "self4")

        val session = WordSession(port!!, stompClient(), 0, selfWords.size)
        session.subscribe()
        selfWords.forEach { session.send(it) }
        session.newHandler.await()

        assertThat(session.newHandler.wordsHandled()).containsExactlyElementsOf(selfWords)

        val wordRepository = wordRepositoryRegistry.getRepository("anonymous")
        assertThat(wordRepository.all()).containsExactlyElementsOf(selfWords)
    }

    @Test
    @DirtiesContext
    fun differentClientsWithSameUserReceiveSameWords() {
        val aWords = setOf("a1", "a2", "a3", "a4")
        val bWords = setOf("b1", "b2", "b3", "b4")
        val allWords = aWords.union(bWords)

        val a = WordSession(port!!, stompClient(), 0, allWords.size)
        a.subscribe()

        val b = WordSession(port!!, stompClient(), 0, allWords.size)
        b.subscribe()

        aWords.forEach { a.send(it) }
        bWords.forEach { b.send(it) }

        a.newHandler.await()
        b.newHandler.await()

        assertThat(a.newHandler.wordsHandled()).containsExactlyElementsOf(allWords)
        assertThat(b.newHandler.wordsHandled()).containsExactlyElementsOf(allWords)

        val wordRepository = wordRepositoryRegistry.getRepository("anonymous")
        assertThat(wordRepository.all()).containsExactlyElementsOf(allWords)
    }

    @Test
    @DirtiesContext
    fun clientReceivesWordsAddedBySystemInternals() {
        val systemWords = setOf("system1", "system2", "system3", "system4")

        val session = WordSession(port!!, stompClient(), 0, systemWords.size)
        session.subscribe()

        val wordRepository = wordRepositoryRegistry.getRepository("anonymous")
        systemWords.forEach { wordRepository.add(it) }
        session.newHandler.await()

        assertThat(session.newHandler.wordsHandled()).containsExactlyElementsOf(systemWords)
        assertThat(wordRepository.all()).containsExactlyElementsOf(systemWords)
    }

}
