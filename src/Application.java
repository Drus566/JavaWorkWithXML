import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Application {
    public static final String extension = ".icd";
    public static final String source_path = "./source";
    public static final String result_path = "./result";

    public Application() throws IOException {
        if (!Files.exists(Paths.get(result_path))) Files.createDirectory(Paths.get(result_path));

        ArrayList<Path> icd_paths = getICDPaths();

        if (icd_paths != null && !icd_paths.isEmpty()) {
            for (Path path : icd_paths) {
                new XMLManager(path);
            }
        }
    }

    public ArrayList<Path> getICDPaths() throws IOException {
        List<Path> icd_files = Files.walk(Paths.get(source_path))
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase(Locale.ROOT).endsWith(extension)).collect(Collectors.toList());
        return (ArrayList<Path>) icd_files;
    }

    public static void main(String[] args) throws IOException {
        new Application();
    }
}
