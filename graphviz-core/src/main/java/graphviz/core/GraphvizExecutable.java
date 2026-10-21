package graphviz.core;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class GraphvizExecutable{

    public GraphvizExecutable() {}

    /** Resolve the default dot executable. */
    public static Path resolve() {
        return resolve("dot");
    }

    /** Resolve a Graphviz engine such as dot or neato. */
    public static Path resolve(String engine){

        if(engine == null || engine.isBlank()){
            throw new IllegalArgumentException("Graphviz engine must not be blank");
        }

        String normalized = engine.trim().toLowerCase(Locale.ROOT);
        if(!normalized.equals("dot") && !normalized.equals("neato")){
            throw new IllegalArgumentException(
                    "Unsupported Graphviz engine: " + engine
            );
        }

        if(normalized.equals("dot")){
            String explicit = System.getenv("GRAPHVIZ_DOT");

            if(explicit != null && !explicit.isBlank()){
                Path path = Path.of(explicit);

                if(Files.isRegularFile(path)){
                    return path.toAbsolutePath().normalize();
                }

                throw new GraphvizException(
                        "GRAPHVIZ_DOT is set, but the executable was not found: "
                                + explicit
                );
            }
        }

        Path fromPath = findOnPath(normalized);
        if(fromPath != null){
            return fromPath;
        }

        String home = System.getenv("GRAPHVIZ_HOME");
        if(home != null && !home.isBlank()){

            Path fromHome = Path.of(
                    home, "bin", executableName(normalized)
            );

            if(Files.isRegularFile(fromHome)){
                return fromHome.toAbsolutePath().normalize();
            }
        }

        // If dot was explicitly configured, look for neato beside it.
        if(normalized.equals("neato")){
            String explicitDot = System.getenv("GRAPHVIZ_DOT");

            if(explicitDot != null && !explicitDot.isBlank()){
                Path dotPath = Path.of(explicitDot).toAbsolutePath().normalize();
                Path sibling = dotPath.resolveSibling(executableName("neato"));

                if(Files.isRegularFile(sibling)){
                    return sibling;
                }
            }
        }

        throw new GraphvizException(
                "Graphviz '" + normalized + "' executable was not found. "
                        + "Run scripts/setup.ps1 or scripts/setup.sh, or put Graphviz on PATH."
        );
    }

    private static Path findOnPath(String engine) {
        String pathValue = System.getenv("PATH");

        if(pathValue == null || pathValue.isBlank()) {
            return null;
        }

        String executable = executableName(engine);
        for(String entry : pathValue.split(File.pathSeparator)){
            if(entry == null || entry.isBlank()){
                continue;
            }
            Path candidate = Path.of(entry).resolve(executable);
            if(Files.isRegularFile(candidate)){
                return candidate.toAbsolutePath().normalize();
            }
        }
        return null;
    }

    private static String executableName(String engine){
        return isWindows() ? engine + ".exe" : engine;
    }

    private static boolean isWindows(){
        return System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT)
                .contains("win");
    }
}
