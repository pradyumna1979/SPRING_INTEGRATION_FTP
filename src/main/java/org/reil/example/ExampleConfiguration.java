package org.reil.example;

import java.io.File;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ExampleConfiguration {

	@Value("${batch.jdbc.driver}")
	private String driverClassName;

	@Value("${batch.jdbc.url}")
	private String driverUrl;

	@Value("${batch.jdbc.user}")
	private String driverUsername;

	@Value("${batch.jdbc.password}")
	private String driverPassword;

	@Autowired
	@Qualifier("jobRepository")
	private JobRepository jobRepository;

	@Autowired
	@Qualifier("myFtpSessionFactory")
	private SessionFactory myFtpSessionFactory;
	
	@Bean
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(driverUrl);
		dataSource.setUsername(driverUsername);
		dataSource.setPassword(driverPassword);
		return dataSource;
	}

	@Bean
	@Scope(value="step")
	public FtpGetRemoteFilesTasklet myFtpGetRemoteFilesTasklet()
	{
		FtpGetRemoteFilesTasklet  ftpTasklet = new FtpGetRemoteFilesTasklet();
		ftpTasklet.setRetryIfNotFound(true);
		ftpTasklet.setDownloadFileAttempts(3);
		ftpTasklet.setRetryIntervalMilliseconds(10000);
		//ftpTasklet.setFileNamePattern("README");
		ftpTasklet.setFileNamePattern("MISSING-FILES");
		ftpTasklet.setRemoteDirectory("/");
		ftpTasklet.setLocalDirectory(new File(System.getProperty("java.io.tmpdir")));
		ftpTasklet.setSessionFactory(myFtpSessionFactory);
		
		return ftpTasklet;
	}
	
	@Bean
	public SessionFactory myFtpSessionFactory()
	{
		DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory();
		ftpSessionFactory.setHost("ftp.gnu.org");
		ftpSessionFactory.setClientMode(0);
		ftpSessionFactory.setFileType(0);
		ftpSessionFactory.setPort(21);
		ftpSessionFactory.setUsername("anonymous");
		ftpSessionFactory.setPassword("anonymous");
		
		return ftpSessionFactory;
	}
	
	@Bean
	public SimpleJobLauncher jobLauncher() {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		return jobLauncher;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

}
