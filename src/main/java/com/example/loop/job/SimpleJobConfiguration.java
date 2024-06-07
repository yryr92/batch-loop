package com.example.loop.job;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.extensions.excel.mapping.PassThroughRowMapper;
import org.springframework.batch.extensions.excel.streaming.StreamingXlsxItemReader;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.example.loop.domain.Employee;
import com.example.loop.domain.EmployeeRowmapper;
import com.example.loop.domain.Product;
import com.example.loop.domain.ProductRowMapper;
import com.example.loop.domain.ResponseDTO;
import com.example.loop.domain.ResponseDTO.fileMeta;
import com.example.loop.domain.ResponseDTO.fileRelease;
import com.example.loop.service.LoopService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    private int size = 0;
    @Autowired
    private LoopService service;

    @Bean
    public Job testSimpleJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("testSimpleJob", jobRepository)
                .start(jsonStep(transactionManager, jobRepository))
                .listener(new JobExecutionListener() {
                    @Override
                    public void afterJob(JobExecution jobExecution) {

                    LocalDateTime start = jobExecution.getCreateTime();

                    //  get job's end time
                    LocalDateTime end = jobExecution.getEndTime();
    
                    // get diff between end time and start time
                    Duration d = Duration.between(start, end);
    
                    // log diff time
                    log.info(">>>> how long : {}", d.toString());
                    log.info(">>>> how many : {}", size);
                    }
                })
                .build();
    }

    @Bean
    public Step testSimpleStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("testSimpleStep", jobRepository)
                .tasklet(testTasklet(), transactionManager)
                .build();
    }

    public Tasklet testTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>> test");
            return RepeatStatus.FINISHED;
        };
    }
    
    @Bean
    public JsonItemReader<ResponseDTO> jsonItemReader() {
        return new JsonItemReaderBuilder<ResponseDTO>()
                 .jsonObjectReader(new JacksonJsonObjectReader<>(ResponseDTO.class))
                 .resource(new ClassPathResource("dummy.json"))
                 .name("jsonItemReader")
                 .build();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Bean
    @StepScope
    public StreamingXlsxItemReader excelReader(@Value("#{jobParameters[filePath]}") String filePath) {
        StreamingXlsxItemReader reader = new StreamingXlsxItemReader();
        reader.setResource(new FileSystemResource(filePath));
        reader.setRowMapper(productRowMapper());
        reader.setLinesToSkip(1);
    return reader;
}

    @Bean
    public EmployeeRowmapper rowMapper() {
        return new EmployeeRowmapper();
    }

    @Bean
    public ProductRowMapper productRowMapper() {
        return new ProductRowMapper();
    }


    // @Bean
    // public JsonFileItemWriter<ResponseDTO> jsonFileItemReader() {
    //     return new JsonFileItemWriterBuilder<ResponseDTO>()
    //              .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
    //              .resource(new ClassPathResource("trades.json"))
    //              .name("tradeJsonFileItemWriter")
    //              .build();
    // }

    @Bean
    public ItemWriter<ResponseDTO> logItemWriter() {
        return new ItemWriter<ResponseDTO>() {

            @Override
            public void write(Chunk<? extends ResponseDTO> chunk) throws Exception {

                for(ResponseDTO dto : chunk) {
                    for(fileMeta fm : dto.getContents()) {
                        service.setContents(dto, fm);
                        
                        for(fileRelease fr : fm.getRelease()) {
                            //log.info(fr.toString());
                            size++;
                        }
                    }
                }
            }
            
        };
        
    }

    @Bean
    public ItemWriter<Product> productItemWriter() {
        return items -> {
            for(Product product : items) {
                log.info(product.toString());
            }
        };
    }

    

    @Bean
    public JdbcBatchItemWriter<ResponseDTO> jdbcBatchItemWriter(DataSource dataSource) {
        String sql = "INSERT INTO npc_content values(default, :bomId, :newYn, :fileName, :vpath, :version, :os, :note, :desc, default)";
        
        return new JdbcBatchItemWriterBuilder<ResponseDTO>()
            .dataSource(dataSource)
            .sql(sql)
            .columnMapped()
            .build();
        
    }

    @Bean JdbcBatchItemWriter<Product> jdbcProductBatchItemWriter(DataSource dataSource) {
        String sql = "INSERT INTO product values(default, :bomId, :fileName, :vPath, :version, :releaseNote, default)";
        
        return new JdbcBatchItemWriterBuilder<Product>()
            .dataSource(dataSource)
            .sql(sql)
            .beanMapped()
            //.columnMapped()
            .build();
    }


    @Bean
    public Step jsonStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("jsonStep", jobRepository)
            .<ResponseDTO, ResponseDTO>chunk(10, transactionManager)
            .reader(jsonItemReader())
            //.processor(movieItemProcessor())
            .writer(logItemWriter())
            .build();
        
    }

    @Bean
    public Step excelStep(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new StepBuilder("excelStep", jobRepository)
            .<Product, Product>chunk(100, transactionManager)
            .reader(excelReader(null))
            .writer(jdbcProductBatchItemWriter(null))
            .build();
    }

    @Bean
    public Job excelJob(PlatformTransactionManager transactionManager, JobRepository jobRepository) {
        return new JobBuilder("excelJob", jobRepository)
                .start(excelStep(transactionManager, jobRepository))
                .build();
    }

}
