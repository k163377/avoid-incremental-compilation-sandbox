package org.wrongwrong.sample

import kotlin.test.Test
import kotlin.test.assertEquals

class SampleTest {
    @Test
    fun `ClassA doSomething returns called by FQN`() {
        val result = ClassA().doSomething()
        assertEquals("called by org.wrongwrong.sample.ClassA.doSomething", result)
    }

    @Test
    fun `ClassB doSomething returns called by FQN`() {
        val result = ClassB().doSomething()
        assertEquals("called by org.wrongwrong.sample.ClassB.doSomething", result)
    }
}
