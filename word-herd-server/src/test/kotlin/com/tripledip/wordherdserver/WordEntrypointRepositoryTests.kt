package com.tripledip.wordherdserver

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.messaging.converter.MessageConverter
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.DirtiesContext
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
class WordWebsocketTests {

    @LocalServerPort
    var port: Int? = null

    @Autowired
    lateinit var wordRepository: WordRepository

    @Autowired
    lateinit var wordEntrypoint: WordEntrypoint

    @Autowired
    lateinit var messageConverter: MessageConverter

    @Test
    @DirtiesContext
    fun entrypointAddsToRepository() {
        wordEntrypoint.addWord("a")
        assertEquals(setOf("a"), wordRepository.all())
    }

    @Test
    @DirtiesContext
    fun clientAddsToRepository() {
        val session = stompSession()
        session.send("/app/add-word", "a")
        Thread.sleep(1000)
        assertEquals(setOf("a"), wordRepository.all())
    }

    fun stompClient(): WebSocketStompClient {
        val client = WebSocketStompClient(SockJsClient(listOf(WebSocketTransport(StandardWebSocketClient()))))
        client.messageConverter = messageConverter
        return client
    }

    fun stompSession(): StompSession {
        val url = "ws://localhost:$port/words"
        return stompClient().connect(url, object : StompSessionHandlerAdapter() {}).get(1, TimeUnit.SECONDS)
    }

    @Test
    @DirtiesContext
    fun clientSubscribeToAdditionsFromSelf() {
        val session = stompSession()
        val handler = WordFrameHandler(1)
        session.subscribe("/topic/added-word", handler)
        session.send("/app/add-word", "a")
        handler.wait()
        assertEquals(setOf("a"), handler.all())
        assertEquals(setOf("a"), wordRepository.all())
    }

    private class WordFrameHandler(val count: Int) : StompFrameHandler {
        val latch = CountDownLatch(count)
        fun wait() = latch.await(2, TimeUnit.SECONDS)

        val payloadsSeen: MutableSet<String> = ConcurrentSkipListSet()
        fun all() = payloadsSeen.toSet()

        override fun handleFrame(headers: StompHeaders, payload: Any?) {
            payloadsSeen.add(payload as String)
            latch.countDown()
        }

        override fun getPayloadType(headers: StompHeaders): Type = String::class.java
    }

    @Test
    @DirtiesContext
    fun clientSubscribeToAdditionsFromOtherClient() {
        val otherSession = stompSession()

        val session = stompSession()
        val handler = WordFrameHandler(2)
        session.subscribe("/topic/added-word", handler)
        otherSession.send("/app/add-word", "a")
        session.send("/app/add-word", "b")
        handler.wait()
        assertEquals(setOf("a", "b"), wordRepository.all())
        assertEquals(setOf("a", "b"), handler.all())
    }

    @Test
    @DirtiesContext
    fun clientSubscribeToAdditionsFromServer() {
        val session = stompSession()
        val handler = WordFrameHandler(2)
        session.subscribe("/topic/added-word", handler)
        // TODO: client is not seeing "a"
        // TODO: why is this different from clientSubscribeToAdditionsFromOtherClient?
        wordEntrypoint.addWord("a")
        session.send("/app/add-word", "b")
        handler.wait()
        assertEquals(setOf("a", "b"), wordRepository.all())
        assertEquals(setOf("a", "b"), handler.all())
    }
}
