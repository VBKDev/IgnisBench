package org.example.benchmark;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for IgnisBench components.
 *
 * Note: Full FireEngine testing requires JavaFX initialization which is complex
 * in a unit test environment. These tests verify basic compilation and structure.
 */
class FireEngineTest {

    @Test
    void testTestModeEnum() {
        // Test that all enum values exist
        IgnisBench.TestMode[] modes = IgnisBench.TestMode.values();
        assertEquals(3, modes.length, "Should have 3 test modes");

        assertNotNull(IgnisBench.TestMode.CPU_SINGLE);
        assertNotNull(IgnisBench.TestMode.CPU_MULTI);
        assertNotNull(IgnisBench.TestMode.GPU_STRESS);
    }

    @Test
    void testEnumNames() {
        // Test enum names are as expected
        assertEquals("CPU_SINGLE", IgnisBench.TestMode.CPU_SINGLE.name());
        assertEquals("CPU_MULTI", IgnisBench.TestMode.CPU_MULTI.name());
        assertEquals("GPU_STRESS", IgnisBench.TestMode.GPU_STRESS.name());
    }

    // TODO: Add JavaFX-compatible tests when test infrastructure is set up
    // Current tests would require JavaFX platform initialization
}
