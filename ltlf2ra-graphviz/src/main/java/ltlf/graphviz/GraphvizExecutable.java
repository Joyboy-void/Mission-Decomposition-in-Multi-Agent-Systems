package ltlf.graphviz;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Locale;

public final class GraphvizExecutable {

    public GraphvizExecutable(){}

    public static Path resolve(){

        String explicit = System.getenv("GRAPHVIZ_DOT");

        if(explicit != null && !explicit.isBlank()){
            Path path = Path.of(explicit);

            if(Files.isRegularFile(path)){
                return path.toAbsolutePath().normalize();
            }

            throw new GraphvizException(
                    "GRAPHVIZ_DOT is set, but the executable was not found: " + explicit
            );
        }

        Path fromPath = findOnPath();
        if(fromPath != null)
            return fromPath;

        String home = System.getenv("GRAPHVIZ_HOME");

        if(home != null && !home.isBlank()){
            Path fromHome = Path.of(home, "bin", isWindows() ? "dot.exe" : "dot");
            if(Files.isRegularFile(fromHome)){
                return fromHome.toAbsolutePath().normalize();
            }
        }

        throw new GraphvizException(
                "Graphviz 'dot' executable was not found. " +
                "Run scripts/setup.ps1 or scripts/setup.sh, or put Graphviz on PATH."
        );
    }

    private static Path findOnPath(){

        String pathValue = System.getenv("PATH");
        if(pathValue == null || pathValue.isBlank()){
            return null;
        }

        String executableName = isWindows() ? "dot.exe" : "dot";
        String[] entries = pathValue.split(File.pathSeparator);

        for(String entry : entries){

            if(entry == null || entry.isBlank()){
                continue;
            }

            Path candidate = Path.of(entry).resolve(executableName);
            if(Files.isRegularFile(candidate)){
                return candidate.toAbsolutePath().normalize();
            }
        }

        return null;
    }

    private static boolean isWindows(){
        return System.getProperty("os.name","").
                toLowerCase(Locale.ROOT).
                contains("win");
    }
}
