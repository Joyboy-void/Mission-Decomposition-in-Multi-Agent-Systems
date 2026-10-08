package ltlf.graphviz;

import ltlf.automaton.ResidualAutomaton;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class GraphvizRenderer {

    private final ResidualAutomatonDotExporter exporter;

    public GraphvizRenderer(){
        this(new ResidualAutomatonDotExporter());
    }

    public GraphvizRenderer(ResidualAutomatonDotExporter exporter){
        this.exporter = Objects.requireNonNull(exporter, "exporter");
    }

    public void render(ResidualAutomaton automaton, String format, Path output){
        Objects.requireNonNull(automaton, "automaton");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(output, "output");

        String normalizedFormat = format.trim().toLowerCase();
        if(normalizedFormat.isEmpty()){
            throw new IllegalArgumentException("Graphviz output Format mustn't be Blank");
        }

        Path parent = output.toAbsolutePath().normalize().getParent();

        try{
            if(parent != null){
                Files.createDirectories(parent);
            }
        }catch(IOException e){
            throw new GraphvizException("Couldn't create output directory" + parent, e);
        }

        Path dot = GraphvizExecutable.resolve();
        String dotSource = exporter.export(automaton);

        Process process;
        try{
            process = new ProcessBuilder(
                    dot.toString(),
                    "-T" + normalizedFormat,
                    "-o" + output.toAbsolutePath().normalize().toString()
            ).start();

            process.getOutputStream().write(dotSource.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
        }catch(IOException e){
            throw new GraphvizException("Could not start Graphviz executable" + dot, e);
        }

        String stderr;
        try(InputStream error = process.getErrorStream()){
            stderr = readFully(error);
        }catch(IOException e){
            throw new GraphvizException("Couldn't read Graphviz diagnostics", e);
        }


        try{
            int exitCode = process.waitFor();

            if(exitCode != 0){
                throw new GraphvizException("Graphviz Failed with exit code" + exitCode +
                        (stderr.isBlank() ? "" : ": ") + stderr.trim());
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new GraphvizException("Graphviz rendering was Interrupted", e);
        }

        if(! Files.isRegularFile(output)){
            throw new GraphvizException("Graphviz reported Success but didn't create : " + output);
        }
    }

    private String readFully(InputStream input) throws IOException{
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        input.transferTo(buffer);

        return buffer.toString(StandardCharsets.UTF_8);
    }
}
