package graphviz.core;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class GraphvizRendererTest {

    private final GraphvizRenderer renderer =
            new GraphvizRenderer();

    @Test
    void acceptsAnyDotExporterType(){
        DotExporter<String> exporter =
                text -> "digraph G { a [label=\"" + text + "\"]; }";

        assertDoesNotThrow(() ->
                exporter.export("example")
        );

        assertTrue(exporter.export("example")
                .contains("label=\"example\""));
    }

    @Test
    void rejectsBlankOutputFormat(){
        assertThrows(
                IllegalArgumentException.class,
                () -> renderer.renderDot(
                        "digraph G {}",
                        " ",
                        Path.of("target/test-output.svg")
                )
        );
    }
}