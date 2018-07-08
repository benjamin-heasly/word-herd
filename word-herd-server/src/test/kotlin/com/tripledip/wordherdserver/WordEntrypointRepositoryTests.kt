package com.tripledip.wordherdserver

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import java.lang.reflect.Type
import java.nio.charset.Charset
import java.security.Principal
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("insecure")
class WordWebsocketTests {

    @LocalServerPort
    var port: Int? = null

    @Autowired
    lateinit var wordRepository: WordRepository

    @Autowired
    lateinit var wordController: WordController

    @Autowired
    lateinit var messageConverter: MessageConverter

    fun stompClient(): WebSocketStompClient {
        val client = WebSocketStompClient(SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient()))))
        client.messageConverter = messageConverter
        return client
    }

    open class WordHandler(count: Int) : StompSessionHandlerAdapter() {
        val latch = CountDownLatch(count)
        fun await() = latch.await(5, TimeUnit.SECONDS)

        val words: MutableSet<String> = ConcurrentSkipListSet()
        fun wordsHandled() = words.toSet()

        override fun handleFrame(headers: StompHeaders, payload: Any?) {
            println("handle words: $payload")
            val wordList = payload as List<String>
            wordList.forEach {
                words.add(it)
                latch.countDown()
            }
        }

        override fun getPayloadType(headers: StompHeaders): Type = List::class.java
    }

    class WordSession(port: Int, client: WebSocketStompClient, expectedNewWordCount: Int) {
        val allHandler = WordHandler(1)
        val newHandler = WordHandler(expectedNewWordCount)

        fun getHeaders(): WebSocketHttpHeaders {
            val auth = "user:password"
            val encodedAuth = Base64.getEncoder().encode(auth.toByteArray(Charset.forName("US-ASCII")))
            val authHeader = "Basic " + String(encodedAuth)
            val headers = WebSocketHttpHeaders()
            headers.set("Authorization", authHeader)
            headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
            return headers
        }

        val session = client
            .connect("ws://localhost:$port/words", WordHandler(0))
            .get(5, TimeUnit.SECONDS)

        fun subscribe() {
            session.subscribe("/topic/new", newHandler)
            session.subscribe("/app/all", allHandler)
            allHandler.await()
        }

        fun send(word: String) = session.send("/app/add", word)
    }

    @Test
    @DirtiesContext
    fun controllerAddToRepository() {
        wordController.addWord("a", Principal { "test" })
        assertEquals(setOf("a"), wordRepository.all())
    }

    @Test
    @DirtiesContext
    fun clientAddToRepository() {
        val session = WordSession(port!!, stompClient(), 0)
        session.send("a")
        session.subscribe()
        assertEquals(setOf("a"), wordRepository.all())
    }

    @Test
    @DirtiesContext
    fun clientReceivesExistingWordsOnSubscription() {
        wordRepository.add("existing1")
        wordRepository.add("existing2")

        val session = WordSession(port!!, stompClient(), 0)
        session.subscribe()
        assertEquals(setOf("existing1", "existing2"), wordRepository.all())
        assertEquals(setOf("existing1", "existing2"), session.allHandler.wordsHandled())
    }

    @Test
    @DirtiesContext
    fun clientSubscriptionFromSelf() {
        val session = WordSession(port!!, stompClient(), 2)
        session.subscribe()

        session.send("self1")
        session.send("self2")
        session.newHandler.await()

        assertEquals(setOf("self1", "self2"), wordRepository.all())
        assertEquals(setOf("self1", "self2"), session.newHandler.wordsHandled())
    }

    @Test
    @DirtiesContext
    fun clientSubscriptionFromOther() {
        val session = WordSession(port!!, stompClient(), 2)
        session.subscribe()

        val other = WordSession(port!!, stompClient(), 0)
        other.send("other1")
        other.send("other2")
        session.newHandler.await()

        assertEquals(setOf("other1", "other2"), wordRepository.all())
        assertEquals(setOf("other1", "other2"), session.newHandler.wordsHandled())
    }

    @Test
    @DirtiesContext
    fun clientSubscriptionFromServerInternal() {
        val session = WordSession(port!!, stompClient(), 2)
        session.subscribe()

        wordController.addWord("server1", Principal { "test" })
        wordController.addWord("server2", Principal { "test" })
        session.newHandler.await()

        assertEquals(setOf("server1", "server2"), session.newHandler.wordsHandled())
        assertEquals(setOf("server1", "server2"), wordRepository.all())
    }

    @Test
    @DirtiesContext
    fun clientSubscriptionFromVariousSources() {
        wordRepository.add("existing1")
        wordRepository.add("existing2")

        val session = WordSession(port!!, stompClient(), 6)
        session.subscribe()
        session.send("self1")
        session.send("self2")

        val other = WordSession(port!!, stompClient(), 0)
        other.send("other1")
        other.send("other2")

        wordController.addWord("server1", Principal { "test" })
        wordController.addWord("server2", Principal { "test" })

        session.newHandler.await()

        assertEquals(setOf("existing1", "existing2"), session.allHandler.wordsHandled())
        assertEquals(
            setOf("self1", "self2", "other1", "other2", "server1", "server2"),
            session.newHandler.wordsHandled()
        )
        assertEquals(
            setOf("existing1", "existing2", "self1", "self2", "other1", "other2", "server1", "server2"),
            wordRepository.all()
        )
    }

}
