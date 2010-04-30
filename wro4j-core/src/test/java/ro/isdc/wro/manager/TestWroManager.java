/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import ro.isdc.wro.config.ConfigurationContext;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.ServletContextAwareWroManagerFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;

/**
 * TestWroManager.java.
 *
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class TestWroManager {
  private WroManager manager;
  @Before
	public void setUp() {
    initConfigWithUpdatePeriodValue(0);
    final WroManagerFactory factory = new ServletContextAwareWroManagerFactory();
    manager = factory.getInstance();
	  Context.set(Mockito.mock(Context.class, Mockito.RETURNS_DEEP_STUBS));
	}

  @Test
  public void testNoProcessorWroManagerFactory() throws IOException {
    final WroManagerFactory factory = new ServletContextAwareWroManagerFactory();
    manager = factory.getInstance();
    manager.setModelFactory(getValidModelFactory());
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

    final OutputStream os = System.out;

    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(response.getWriter()).thenReturn(new PrintWriter(os));
    Mockito.when(response.getOutputStream()).thenReturn(new DelegatingServletOutputStream(os));
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");
    manager.process(request, response);

    //WroTestUtils.compareProcessedResourceContents(resultReader, expectedReader, processor)
  }

  @Test
  public void processValidModel() throws IOException {
    manager.setModelFactory(getValidModelFactory());
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");
    manager.process(request, response);
  }

  /**
   * @return a {@link XmlModelFactory} pointing to a valid config resource.
   */
  private XmlModelFactory getValidModelFactory() {
    return new XmlModelFactory() {
    	@Override
    	protected InputStream getConfigResourceAsStream() {
    		return getResourceAsStream(TestWroManager.class.getPackage().getName().replace(".", "/") + "/wro.xml");
    	}
    };
  }



  /**
   * Test how manager behaves when the update period value is greater than zero and the scheduler starts.
   *
   * @throws Exception
   */
  @Test
  public void testManagerWhenSchedulerIsStarted() throws Exception {
    manager.setModelFactory(getValidModelFactory());
    initConfigWithUpdatePeriodValue(1);
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    final HttpServletResponse response = Context.get().getResponse();
    Mockito.when(request.getRequestURI()).thenReturn("/app/g1.css");
    manager.process(request, response);
    //allow thread to do their job
    Thread.sleep(2000);
  }

  /**
   * Initialize {@link WroConfiguration} object with cacheUpdatePeriod & modelUpdatePeriod equal with provided argument.
   */
  private void initConfigWithUpdatePeriodValue(final long periodValue) {
    final WroConfiguration config = new WroConfiguration();
    config.setCacheUpdatePeriod(periodValue);
    config.setModelUpdatePeriod(periodValue);

    ConfigurationContext.get().setConfig(config);
  }

  @After
  public void tearDown() {
    manager.destroy();
    Context.unset();
  }
}
