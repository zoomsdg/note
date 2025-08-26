package com.dailynotes.utils

import org.junit.Test
import org.junit.Assert.*

class MediaUtilsTest {
    
    @Test
    fun pathListToString_emptyList_returnsEmptyJsonArray() {
        val result = MediaUtils.pathListToString(emptyList())
        assertEquals("[]", result)
    }
    
    @Test
    fun pathListToString_singlePath_returnsJsonArray() {
        val paths = listOf("/path/to/image.jpg")
        val result = MediaUtils.pathListToString(paths)
        assertEquals("[\"/path/to/image.jpg\"]", result)
    }
    
    @Test
    fun pathListToString_multiplePaths_returnsJsonArray() {
        val paths = listOf("/path/to/image1.jpg", "/path/to/image2.jpg")
        val result = MediaUtils.pathListToString(paths)
        assertTrue(result.contains("/path/to/image1.jpg"))
        assertTrue(result.contains("/path/to/image2.jpg"))
    }
    
    @Test
    fun stringToPathList_emptyString_returnsEmptyList() {
        val result = MediaUtils.stringToPathList("")
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun stringToPathList_validJson_returnsList() {
        val jsonString = "[\"/path/to/image.jpg\"]"
        val result = MediaUtils.stringToPathList(jsonString)
        assertEquals(1, result.size)
        assertEquals("/path/to/image.jpg", result[0])
    }
    
    @Test
    fun stringToPathList_invalidJson_returnsEmptyList() {
        val result = MediaUtils.stringToPathList("invalid json")
        assertTrue(result.isEmpty())
    }
}