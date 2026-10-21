package graphviz.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

public final class GraphvizRenderer {

    /** Export a domain object to DOT and render it with the default dot engine. */
    public <T> void render(
            T object,
            DotExporter<? super T> exporter,
            String format,
            Path output) {

        Objects.requireNonNull(object, "object");
        Objects.requireNonNull(exporter, "exporter");
        renderDot(exporter.export(object), format, output);
    }

    /** Render DOT source using Graphviz's dot layout engine. */
    public void renderDot(String dotSource, String format, Path output){
        renderDotWithEngine("dot", dotSource, format, output);
    }

    /**
     * Render DOT using a selected Graphviz engine.
     *
     * For fixed-position graphs, use engine="neato" and include the
     * appropriate fixed node positions in the DOT source. Existing callers
     * continue using renderDot(), which retains dot as the default.
     */
    public void renderDotWithEngine(
            String engine,
            String dotSource,
            String format,
            Path output) {

        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(dotSource, "dotSource");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(output, "output");

        String normalizedEngine = engine.trim().toLowerCase(Locale.ROOT);
        if(!normalizedEngine.equals("dot")
                && !normalizedEngine.equals("neato")){

            throw new IllegalArgumentException(
                    "Unsupported Graphviz engine: " + engine
                            + ". Supported engines are dot and neato."
            );
        }

        String normalizedFormat = format.trim().toLowerCase(Locale.ROOT);
        if(normalizedFormat.isEmpty()){
            throw new IllegalArgumentException(
                    "Graphviz output format must not be blank"
            );
        }

        Path absoluteOutput = output.toAbsolutePath().normalize();
        Path parent = absoluteOutput.getParent();

        try{
            if(parent != null){
                Files.createDirectories(parent);
            }
        }catch(IOException e){
            throw new GraphvizException(
                    "Could not create output directory: " + parent, e
            );
        }

        Path executable = GraphvizExecutable.resolve(normalizedEngine);
        Process process;

        // builds the argument list explicitly to avoid passing an empty option.
        try{
            ProcessBuilder builder = new ProcessBuilder();

            if(normalizedEngine.equals("neato")){
                builder.command(
                        executable.toString(), "-n2",
                        "-T" + normalizedFormat, "-o" + absoluteOutput
                );
            }else{
                builder.command(
                        executable.toString(),
                        "-T" + normalizedFormat, "-o" + absoluteOutput
                );
            }

            process = builder.start();
            try(var stdin = process.getOutputStream()){
                stdin.write(dotSource.getBytes(StandardCharsets.UTF_8));
            }
        }catch(IOException e){
            throw new GraphvizException(
                    "Could not start Graphviz executable: " + executable, e
            );
        }

        String stderr;
        try{
            stderr = readFully(process.getErrorStream());
        }catch (IOException e) {
            process.destroyForcibly();
            throw new GraphvizException(
                    "Could not read Graphviz diagnostics", e
            );
        }

        try{
            int exitCode = process.waitFor();
            if(exitCode != 0){
                throw new GraphvizException(
                        "Graphviz (" + normalizedEngine
                                + ") failed with exit code " + exitCode
                                + (stderr.isBlank() ? "" : ": " + stderr.trim())
                );
            }
        }catch (InterruptedException e){
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new GraphvizException(
                    "Graphviz rendering was interrupted", e
            );
        }

        if(!Files.isRegularFile(absoluteOutput)){
            throw new GraphvizException(
                    "Graphviz reported success but did not create: "
                            + absoluteOutput
            );
        }
    }

    private String readFully(InputStream input) throws IOException{

        try(input;
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            input.transferTo(buffer);
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
