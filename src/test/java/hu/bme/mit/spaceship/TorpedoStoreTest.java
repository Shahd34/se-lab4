package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TorpedoStoreTest {

    @Test
    void newStoreHasRequestedCountAndIsNotEmpty() {
        TorpedoStore store = new TorpedoStore(3);

        assertEquals(3, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void emptyStoreReportsEmpty() {
        TorpedoStore store = new TorpedoStore(0);

        assertTrue(store.isEmpty());
        assertEquals(0, store.getTorpedoCount());
    }

    @Test
    void fireOneSuccessfullyDecreasesCountByOne() {
        TorpedoStore store = new TorpedoStore(1, 0.0);

        assertTrue(store.fire(1));
        assertEquals(0, store.getTorpedoCount());
        assertTrue(store.isEmpty());
    }

    @Test
    void firingMultipleTorpedoesDecreasesCountByRequestedAmount() {
        TorpedoStore store = new TorpedoStore(4, 0.0);

        assertTrue(store.fire(3));

        assertEquals(1, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void simulatedFiringFailureDoesNotConsumeTorpedoes() {
        TorpedoStore store = new TorpedoStore(2, 1.0);

        assertFalse(store.fire(1));

        assertEquals(2, store.getTorpedoCount());
        assertFalse(store.isEmpty());
    }

    @Test
    void firingZeroTorpedoesThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);

        assertThrows(IllegalArgumentException.class, () -> store.fire(0));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingNegativeNumberOfTorpedoesThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);

        assertThrows(IllegalArgumentException.class, () -> store.fire(-1));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingMoreTorpedoesThanAvailableThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(2, 0.0);

        assertThrows(IllegalArgumentException.class, () -> store.fire(3));
        assertEquals(2, store.getTorpedoCount());
    }

    @Test
    void firingFromEmptyStoreThrowsIllegalArgumentException() {
        TorpedoStore store = new TorpedoStore(0, 0.0);

        assertThrows(IllegalArgumentException.class, () -> store.fire(1));
        assertEquals(0, store.getTorpedoCount());
        assertTrue(store.isEmpty());
    }
}
