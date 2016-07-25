package com.example;

import static com.example.CopyTypeEnum.DIR_TO_DIR;
import static com.example.CopyTypeEnum.FILE_TO_DIR;
import static com.example.CopyTypeEnum.FILE_TO_FILE;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

@SpringBootApplication
public class CreateAirZipApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateAirZipApplication.class);

    private static final String FILE_NAME = "install.csv";

    private static final String REGEX_ENV_VARIABLE = "\\$\\(\\w+\\)";

	@Value("${project-path}")
	private String projectPath;

	@Value("${air-zip-path}")
	private String airZipPath;

	public static void main(String[] args) {
		SpringApplication.run(CreateAirZipApplication.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {
		if (projectPath == null || projectPath.length() == 0) {
            LOGGER.error("Please, define project-path.");
			throw new IllegalArgumentException("Please, define project-path.");
		}
		if (airZipPath == null || airZipPath.length() == 0) {
            LOGGER.error("Please, define air-zip-path.");
			throw new IllegalArgumentException("Please, define air-zip-path.");
		}
		copyInstallCsvResources();
	}

	private void copyInstallCsvResources() {
		try (Stream<String> installCsvLines = Files.lines(Paths.get(projectPath, FILE_NAME))) {
            installCsvLines
                    .skip(1)
                    .filter(this::filterCsvLines)
                    .map(CopyDetail::new)
                    .forEach(this::copyFiles)
            ;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean filterCsvLines(String line) {
		return line.length() > 0 && !line.startsWith("#");
	}

    private boolean checkFileExtension(String fileExtension, List<String> legalList){
        for(String legalExtension: legalList){
            if(fileExtension.equals(legalExtension)){
                return true;
            }
        }
        return false;
    }

    private String getGeneratedFilePath(Matcher matcher){
        // remove "$("  and  ")"
        String envVariableName = matcher.group().substring(2, matcher.group().length() - 1);
        String pathLocalEnvVariable = System.getenv(envVariableName);

        if(null == pathLocalEnvVariable){
            LOGGER.error("No such environment variable is found on local machine.");
            return null;
        }


        pathLocalEnvVariable = pathLocalEnvVariable.replace("\\", "/");

        return matcher.replaceFirst(pathLocalEnvVariable);
    }

    private boolean isCopyTypeFamiliar(String copyTypeFromFile){
        return Arrays.stream(CopyTypeEnum.values()).anyMatch(copyType -> copyType.getType().equals(copyTypeFromFile));
    }

	private void copyFiles(CopyDetail copyDetail) {

        if(!isCopyTypeFamiliar(copyDetail.getType())){
            LOGGER.error(String.format("%s is not familiar copy type! ", copyDetail.getType()));
            return;
        }
        else if (StringUtils.isEmpty(copyDetail.getSource()) || StringUtils.isEmpty(copyDetail.getDestination())) {
            LOGGER.error("Source or\\and destination is empty");
            return;
        } else {
            File from = new File(projectPath, copyDetail.getSource());
            File to = new File(airZipPath, copyDetail.getDestination());

            Pattern pattern = Pattern.compile(REGEX_ENV_VARIABLE);
            Matcher matcherFrom = pattern.matcher(copyDetail.getSource());
            Matcher matcherTo = pattern.matcher(copyDetail.getDestination());

            if (matcherFrom.find()) {

                String newPathFile = getGeneratedFilePath(matcherFrom);
                if(null == newPathFile){
                    return;
                }
                from = new File(newPathFile);
            }

            if (matcherTo.find()) {
                String newPathFile = getGeneratedFilePath(matcherTo);
                if(null == newPathFile){
                    return;
                }
                to = new File(newPathFile);
            }

            if (FILE_TO_DIR.getType().equals(copyDetail.getType())) {
                copySingleFileToFolder(copyDetail, from, to);
            } else if (DIR_TO_DIR.getType().equals(copyDetail.getType())
                    || FILE_TO_FILE.getType().equals(copyDetail.getType())) {
                copyFilesAndFolders(copyDetail, from, to);
            }


        }
    }

    private void copySingleFileToFolder(CopyDetail copyDetail, File from, File to){
        boolean isFileExtensionPermitted = true;

        if(!copyDetail.getPermittedFileTypes().isEmpty()){
            String fileExtension = FilenameUtils.getExtension(from.getName());
            isFileExtensionPermitted = checkFileExtension(fileExtension, copyDetail.getPermittedFileTypes());
        }

        if(isFileExtensionPermitted) {
            try {
                FileUtils.copyFileToDirectory(from, to);
            } catch (IOException e) {
                LOGGER.error(e.getClass().toString() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void copyFilesAndFolders(CopyDetail copyDetail, File sourceFolder, File destinationFolder){
        if(sourceFolder.isDirectory()){
            if(!destinationFolder.exists()){
                destinationFolder.mkdir();
            }

            String files[] = sourceFolder.list();

            for(String file: files){
                File srcFile = new File(sourceFolder, file);
                File destFile = new File(destinationFolder, file);

                copyFilesAndFolders(copyDetail, srcFile, destFile);
            }
        }
        else{

            // used only for specific cases in FileToFile copy type
            if(!destinationFolder.getParentFile().exists()){
                File tempFile = new File(destinationFolder.getParent());
                tempFile.mkdirs();
            }

            try {
                boolean isFileExtensionPermitted = true;
                if(!StringUtils.isEmpty(copyDetail.getPermittedFileTypes())){
                    String fileExtension = FilenameUtils.getExtension(sourceFolder.getName());
                    isFileExtensionPermitted = checkFileExtension(fileExtension, copyDetail.getPermittedFileTypes());
                }
                if(isFileExtensionPermitted) {
                    Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                LOGGER.error(e.getClass().toString() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
