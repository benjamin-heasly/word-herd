package com.tripledip.wordherdserver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.security.Principal

@RunWith(SpringRunner::class)
@SpringBootTest()
class WordHerdControllerTests {

    @Autowired
    lateinit var wordRepositoryRegistry: WordRepositoryRegistry

    @Autowired
    lateinit var wordController: WordController

    @Test
    fun userRepositoryAdditionsAreIndependent() {
        val userA = Principal { "a" }
        val aRepository = wordRepositoryRegistry.getRepository("a")
        val aWords = setOf("a1", "a2", "a3", "a4")

        val userB = Principal { "b" }
        val bRepository = wordRepositoryRegistry.getRepository("b")
        val bWords = setOf("b1", "b2", "b3", "b4")

        aWords.forEach { wordController.addWord(it, userA) }
        bWords.forEach { wordController.addWord(it, userB) }

        assertThat(aRepository.all()).containsExactlyElementsOf(aWords)
        assertThat(bRepository.all()).containsExactlyElementsOf(bWords)
    }

    @Test
    fun userSubscriptionsAreIndependent() {
        val userA = Principal { "a" }
        val repositoryA = wordRepositoryRegistry.getRepository("a")
        val aWords = setOf("a1", "a2", "a3", "a4")
        aWords.forEach { repositoryA.add(it) }

        val userB = Principal { "b" }
        val repositoryB = wordRepositoryRegistry.getRepository("b")
        val bWords = setOf("b1", "b2", "b3", "b4")
        bWords.forEach { repositoryB.add(it) }

        val subscriptionA = wordController.startSubscription(userA)
        val subscriptionB = wordController.startSubscription(userB)

        assertThat(subscriptionA).containsExactlyElementsOf(aWords)
        assertThat(subscriptionB).containsExactlyElementsOf(bWords)
    }
}