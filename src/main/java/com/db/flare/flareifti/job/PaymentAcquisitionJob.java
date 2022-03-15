package com.db.flare.flareifti.job;

import com.db.flare.flareifti.exception.FlareSkippableException;
import com.db.flare.flareifti.model.PaymentIn;
import com.db.flare.flareifti.service.FlareJobHelper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;

@Configuration
public class PaymentAcquisitionJob {


    @Autowired
    private FlareJobHelper flareJobHelper;

    @Bean
    @StepScope
    public Resource resource(@Value("#{jobParameters['fileName']}") String fileName) throws MalformedURLException {
        return new UrlResource(fileName);
    }


    @Bean
    public FlatFileItemReader<PaymentIn> itemReader(Resource resource) {
        return new FlatFileItemReaderBuilder<PaymentIn>()
                .name("personItemReader")
                .resource(resource)
                .strict(false)
                .linesToSkip(1)
                .targetType(PaymentIn.class)
                .delimited()
                .delimiter(",")
                .names("transactionId", "direction", "orderingBic",
                        "beneficiaryBic",
                        "senderBic", "receiverBic",
                        "amount")
                .build();
    }

    @Bean
    public Job job(JobBuilderFactory jobs, StepBuilderFactory steps) throws MalformedURLException {
        return flareJobHelper.getJobBuilder("ifti-report")
                .start(buildReport())
                .build();
    }

    @Bean
    public Step buildReport() throws MalformedURLException {
        return flareJobHelper.getStepBuilder("build-report")
                .<PaymentIn, PaymentIn>chunk(100)
                .faultTolerant()
                .processorNonTransactional()
                .skip(FlareSkippableException.class)
                .skipLimit(Integer.MAX_VALUE)
                .reader(itemReader(resource(null)))
                .processor(processor())
                .writer(paymentWriter(new FileSystemResource("target/output/result.csv")))
                .build();
    }


    @Bean
    public ItemProcessor<PaymentIn, PaymentIn> processor() {
        return new ItemProcessor<PaymentIn, PaymentIn>() {
            @Override
            public PaymentIn process(PaymentIn paymentIn) throws Exception {
                if (paymentIn.getDirection().startsWith("incoming")) {
                    if (!(paymentIn.getBeneficiaryBic().contains("AU") || paymentIn.getReceiverBic().contains("AU"))) {
                        throw new FlareSkippableException();
                    }
                } else {
                    if (!(paymentIn.getOrderingBic().contains("AU") || paymentIn.getSenderBic().contains("AU"))) {
                        throw new FlareSkippableException();
                    }
                }
                return paymentIn;
            }
        };
    }

    @Bean
    public ItemWriter<PaymentIn> paymentWriter(Resource outputResource) {
        //Create writer instance
        FlatFileItemWriter<PaymentIn> writer = new FlatFileItemWriter<>();

        //Set output file location
        writer.setResource(outputResource);

        //All job repetitions should "append" to same output file
        writer.setAppendAllowed(true);

        //Name field values sequence based on object properties
        writer.setLineAggregator(new DelimitedLineAggregator<PaymentIn>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<PaymentIn>() {
                    {
                        setNames(new String[]{"transactionId", "direction", "orderingBic", "beneficiaryBic", "senderBic", "receiverBic", "amount"});
                    }
                });
            }
        });
        return writer;
    }
}
