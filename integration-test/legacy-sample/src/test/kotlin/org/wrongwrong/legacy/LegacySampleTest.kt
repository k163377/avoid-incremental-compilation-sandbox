package org.wrongwrong.legacy

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySampleTest {
    @Test
    fun `ClassA doSomething returns called by FQN`() {
        val result = ClassA().doSomething()
        assertEquals("called by org.wrongwrong.legacy.ClassA.doSomething", result)
    }

    @Test
    fun `ClassB doSomething returns called by FQN`() {
        val result = ClassB().doSomething()
        assertEquals("called by org.wrongwrong.legacy.ClassB.doSomething", result)
    }
}
