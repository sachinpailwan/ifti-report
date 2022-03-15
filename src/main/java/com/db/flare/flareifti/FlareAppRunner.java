package com.db.flare.flareifti;

import com.db.flare.flareifti.service.JobRunningService;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;


@Component
public class FlareAppRunner implements ApplicationRunner, ExitCodeGenerator {
    private final Pattern jobParameterPattern = Pattern.compile("([^=]+)=([^=]+)");

    private final JobRunningService jobRunningService;


    @Autowired
    public FlareAppRunner(JobRunningService jobRunningService) {
        this.jobRunningService = jobRunningService;
    }


    @Override
    public void run(ApplicationArguments arguments) {
        checkArgument(!arguments.getNonOptionArgs().isEmpty(), "No job name provided");

        String jobName = arguments.getNonOptionArgs().get(0);
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        for (String arg : arguments.getNonOptionArgs().subList(1, arguments.getNonOptionArgs().size())) {
            Matcher matcher = jobParameterPattern.matcher(arg);
            checkArgument(matcher.matches(), "Invalid format of a job parameter: %s", arg);
            String jobParameterName = matcher.group(1);
            String jobParameterValue = matcher.group(2);
            jobParametersBuilder.addParameter(jobParameterName, parseJobParameter(jobParameterValue));
        }

        if (arguments.containsOption("force")) {
            jobParametersBuilder.addLong("uniqueId", System.currentTimeMillis());
        }

        jobRunningService.runJob(jobName, jobParametersBuilder.toJobParameters());
    }

    private JobParameter parseJobParameter(String argument) {
        if (isParsable(argument)) {
            if (argument.contains(".")) {
                return new JobParameter(Double.parseDouble(argument));
            } else {
                return new JobParameter(Long.parseLong(argument));
            }
        }
        return new JobParameter(argument);
    }

    @Override
    public int getExitCode() {
        return jobRunningService.lastExitCode();
    }
}