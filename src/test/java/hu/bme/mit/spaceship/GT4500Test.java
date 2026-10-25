
package hu.bme.mit.spaceship;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GT4500Test {

    private GT4500 ship;

    @BeforeEach
    public void init() {
        ship = new GT4500();
    }

    @Test
    public void fireTorpedo_Single_Success() {
        assertTrue(ship.fireTorpedo(FiringMode.SINGLE));
    }

    @Test
    public void fireTorpedo_All_Success() {
        assertTrue(ship.fireTorpedo(FiringMode.ALL));
    }

    @Test
    public void fireTorpedo_AllWithBothStoresEmpty_ReturnsFalse() {
        for (int i = 0; i < 10; i++) {
            ship.fireTorpedo(FiringMode.ALL);
        }

        assertFalse(ship.fireTorpedo(FiringMode.ALL));
    }

    @Test
    public void fireTorpedo_SingleFallsBackToSecondaryWhenPrimaryIsEmpty() {
        for (int i = 0; i < 10; i++) {
            assertTrue(ship.fireTorpedo(FiringMode.SINGLE));
            assertTrue(ship.fireTorpedo(FiringMode.SINGLE));
        }

        assertFalse(ship.fireTorpedo(FiringMode.SINGLE));
    }

    @Test
    public void fireTorpedo_SingleReachesEmptyPrimaryFallbackWhenSecondaryIsEmpty() {
        assertTrue(ship.fireTorpedo(FiringMode.SINGLE));

        for (int i = 0; i < 10; i++) {
            ship.fireTorpedo(FiringMode.ALL);
        }

        assertFalse(ship.fireTorpedo(FiringMode.SINGLE));
    }

    @Test
    public void fireTorpedo_NullMode_ThrowsNullPointerException() {
        assertThrows(
                NullPointerException.class,
                () -> ship.fireTorpedo(null)
        );
    }
}
