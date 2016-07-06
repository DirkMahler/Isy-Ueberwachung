package de.bund.bva.pliscommon.ueberwachung.service.loadbalancer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class TestLoadbalancerServlet {

	private LoadbalancerServlet loadBalancer;
	private ServletConfig mockConfig;
	private Appender mockAppender;
	private ArgumentCaptor<LoggingEvent> mockCaptor;
	private ServletContext mockContext;
	
	@Before
	public void setUp(){
		loadBalancer = new LoadbalancerServlet();
		mockAppender = Mockito.mock(Appender.class);
		mockCaptor = ArgumentCaptor.forClass(LoggingEvent.class);
		Logger log = Logger.getLogger(LoadbalancerServlet.class);
		log.addAppender(mockAppender);
		mockConfig = Mockito.mock(ServletConfig.class);
		mockContext = Mockito.mock(ServletContext.class);
		Mockito.when(mockConfig.getServletContext()).thenReturn(mockContext);		
		
		
	}
	
	@Test
	public void testInit() throws ServletException {
		Mockito.when(mockContext.getRealPath("/WEB-INF/classes/config/isAlive")).thenReturn("/WEB-INF/classes/config/isAlive/isAlive");
		Mockito.when(mockConfig.getInitParameter("isAliveFileLocation")).thenReturn("/WEB-INF/classes/config/isAlive");
		loadBalancer.init(mockConfig);	
		Mockito.verify(mockAppender, Mockito.times(2)).doAppend(mockCaptor.capture());
	}
	
	@Test
	public void testInitNull() throws ServletException {
		Mockito.when(mockContext.getRealPath("/WEB-INF/classes/config/isAlive")).thenReturn("/WEB-INF/classes/config/isAlive/isAlive");
		Mockito.when(mockConfig.getInitParameter("isAliveFileLocation")).thenReturn(null);
		loadBalancer.init(mockConfig);	
		Mockito.verify(mockAppender, Mockito.times(3)).doAppend(mockCaptor.capture());		
	}
	
	
	@Test
	public void testDoGet() throws ServletException, IOException {
		Mockito.when(mockContext.getRealPath("/src/test/resources")).thenReturn("/src/test/resources/isAlive");
		Mockito.when(mockConfig.getInitParameter("isAliveFileLocation")).thenReturn("/src/test/resources");
		loadBalancer.init(mockConfig);	
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);		
		loadBalancer.doGet(null, resp);
		Mockito.verify(mockAppender, Mockito.times(3)).doAppend(mockCaptor.capture());
		Mockito.verify(resp, Mockito.times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);		
	}
	
	@Test
	public void testDoGetIsAlive() throws ServletException, IOException {
		Mockito.when(mockContext.getRealPath("/src/test/resources")).thenReturn("src/test/resources/isAlive");
		Mockito.when(mockConfig.getInitParameter("isAliveFileLocation")).thenReturn("/src/test/resources");
		File f = new File("src/test/resources/isAlive");
		f.createNewFile();
		PrintWriter writer = new PrintWriter(f);
		loadBalancer.init(mockConfig);	
		HttpServletResponse resp = Mockito.mock(HttpServletResponse.class);		
		Mockito.when(resp.getWriter()).thenReturn(writer);
		loadBalancer.doGet(null, resp);
		Mockito.verify(mockAppender, Mockito.times(3)).doAppend(mockCaptor.capture());
		Mockito.verify(resp, Mockito.times(1)).setStatus(HttpServletResponse.SC_OK);
		f.delete();
		
	}

}
