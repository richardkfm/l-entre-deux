package org.entredeux.app.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleAppSelectionUseCaseTest {

    @Test
    fun adding_package_to_empty_set() {
        val result = toggleAppSelection(emptySet(), "com.example.app")
        assertEquals(setOf("com.example.app"), result)
    }

    @Test
    fun adding_package_to_existing_set() {
        val current = setOf("com.example.one")
        val result = toggleAppSelection(current, "com.example.two")
        assertTrue(result.contains("com.example.one"))
        assertTrue(result.contains("com.example.two"))
    }

    @Test
    fun removing_package_that_is_selected() {
        val current = setOf("com.example.one", "com.example.two")
        val result = toggleAppSelection(current, "com.example.one")
        assertFalse(result.contains("com.example.one"))
        assertTrue(result.contains("com.example.two"))
    }

    @Test
    fun toggling_twice_returns_original_set() {
        val current = setOf("com.example.app")
        val after = toggleAppSelection(toggleAppSelection(current, "com.example.app"), "com.example.app")
        assertEquals(current, after)
    }

    @Test
    fun original_set_is_not_mutated() {
        val current = setOf("com.example.app")
        toggleAppSelection(current, "com.example.other")
        assertEquals(1, current.size)
    }
}
