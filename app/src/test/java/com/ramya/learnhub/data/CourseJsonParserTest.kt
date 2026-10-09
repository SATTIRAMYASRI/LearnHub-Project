package com.ramya.learnhub.data

import com.ramya.learnhub.data.remote.CourseJsonParser
import com.ramya.learnhub.data.remote.MalformedDataException
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseJsonParserTest {

    @Test
    fun `parses a valid course`() {
        val json = """[{"id":1,"title":"Python","instructor":"John","progress":65,"lessons":20}]"""

        val courses = CourseJsonParser.parse(json)

        assertEquals(1, courses.size)
        assertEquals("Python", courses[0].title)
        assertEquals(65, courses[0].progress)
        assertEquals(20, courses[0].lessons)
    }

    @Test
    fun `skips broken entries but keeps the good ones`() {
        val json = """
            [
              {"id":1,"title":"Good","instructor":"A","progress":10,"lessons":5},
              {"id":2,"instructor":"No title","progress":10,"lessons":5},
              {"id":3,"title":"Negative lessons","lessons":-4},
              "not an object"
            ]
        """
        val courses = CourseJsonParser.parse(json)

        assertEquals(listOf(1L), courses.map { it.id })
    }

    @Test
    fun `keeps progress between 0 and 100`() {
        val json = """[{"id":1,"title":"X","instructor":"A","progress":250,"lessons":5}]"""

        assertEquals(100, CourseJsonParser.parse(json)[0].progress)
    }

    @Test
    fun `empty array gives an empty list`() {
        assertEquals(0, CourseJsonParser.parse("[]").size)
    }

    @Test(expected = MalformedDataException::class)
    fun `invalid json throws`() {
        CourseJsonParser.parse("this is not json")
    }

    @Test(expected = MalformedDataException::class)
    fun `json that is not an array throws`() {
        CourseJsonParser.parse("""{"id":1}""")
    }
}
