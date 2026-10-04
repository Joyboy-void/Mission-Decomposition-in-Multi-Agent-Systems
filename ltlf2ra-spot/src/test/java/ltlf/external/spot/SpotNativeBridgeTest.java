package ltlf.external.spot;

import ltlf.ast.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpotNativeBridgeTest {

    @Test
    void nativeEquivalenceAcceptsIdenticalFormulas() {
        assertTrue(
                SpotNativeBridge.equivalent("a", "a")
        );
    }

    @Test
    void nativeEquivalenceRejectsDifferentFormulas() {
        assertFalse(
                SpotNativeBridge.equivalent("a", "b")
        );
    }

    @Test
    void nativeParserCreatesAtoms() {
        assertEquals(
                new AtomicProposition("p"),
                SpotNativeBridge.parse("p")
        );
    }

    @Test
    void nativeParserCreatesBinaryFormulas() {
        assertEquals(
                new And(
                        new AtomicProposition("a"),
                        new AtomicProposition("b")
                ),
                SpotNativeBridge.parse("a & b")
        );
    }

    @Test
    void nativeParserCreatesTemporalFormulas() {

        assertEquals(
                new Eventually(new AtomicProposition("p")),
                SpotNativeBridge.parse("F(p)")
        );
    }

    @Test
    void nativeParserRejectsInvalidInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpotNativeBridge.parse("a &")
        );
    }

    @Test
    void nativeEquivalenceRejectsNullLeft() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpotNativeBridge.equivalent(null, "a")
        );
    }

    @Test
    void nativeEquivalenceRejectsNullRight() {
        assertThrows(
                IllegalArgumentException.class,
                () -> SpotNativeBridge.equivalent("a", null)
        );
    }
}
