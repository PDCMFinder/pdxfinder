package org.pdxfinder.commandline;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

@Component
public class ApplicationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final FinderCommandLine command;
    private final IFactory factory;
    private int exitCode;

    public ApplicationRunner(FinderCommandLine command, IFactory factory) {
        this.command = command;
        this.factory = factory;
    }

    @Override
    public void run(String... args) throws Exception {
        exitCode = new CommandLine(command, factory).execute(args);
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

}
