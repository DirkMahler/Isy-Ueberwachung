package test.de.bund.bva.pliscommon.ueberwachung.common.jmx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Ueberwachungsschnittstelle fuer Services der PLIS.
 * %%
 * 
 * %%
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * The Federal Office of Administration (Bundesverwaltungsamt, BVA)
 * licenses this file to you under the Apache License, Version 2.0 (the
 * License). You may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * #L%
 */

import java.security.SecureRandom;
import java.util.Random;

import org.aopalliance.aop.Advice;
import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.aop.framework.ProxyFactory;

import de.bund.bva.pliscommon.exception.service.PlisBusinessToException;
import de.bund.bva.pliscommon.ueberwachung.common.jmx.ServiceStatistikMBean;
import test.de.bund.bva.pliscommon.ueberwachung.common.jmx.test.DummyService;
import test.de.bund.bva.pliscommon.ueberwachung.common.jmx.test.impl.DummyServiceImpl;

/**
 * Tests für ServiceStatistikMBean.
 * 
 */
//@Ignore
public class TestServiceStatistikMBean {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(TestServiceStatistikMBean.class);
    
    /**
     * Standardwert = 1000
     */
    private static int secondMultiplicity = 1000;

    /**
     * Testet ZaehleAufruf von ServiceStatistikMBean in Erste Minute.
     */
    @Test
    public void testZaehleAufrufErsteMinute() throws Exception {
        ServiceStatistikMBean mbean = new ServiceStatistikMBean();
        Random random = new SecureRandom();
        LOG.debug("Warte bis anfang naechste Minute ...");
        Thread.sleep(((61*secondMultiplicity) - (System.currentTimeMillis() % (60*secondMultiplicity))));
        
        int anzahlAufrufe = 12;
        int anzahlAufrufeLetzteMinute = 0;
        int anzahlFehlerLetzteMinute = 0;
        long durchschnittsDauerLetzteZehnAufrufe = 0;
        long durchschnittsDauerLetzteZehnAufrufeReferenz = 0;
        for (int count = 0; count < anzahlAufrufe; count++) {
            long dauer = random.nextInt(10*secondMultiplicity);
            boolean erfolgreich = random.nextBoolean();
            mbean.zaehleAufruf(dauer, erfolgreich);
            LOG.debug("Rufe MBean.zaehleAufruf auf mit (" + dauer + "," + erfolgreich + ")");
        }
        anzahlAufrufeLetzteMinute = mbean.getAnzahlAufrufeLetzteMinute();
        anzahlFehlerLetzteMinute = mbean.getAnzahlFehlerLetzteMinute();
        durchschnittsDauerLetzteZehnAufrufeReferenz = mbean.getDurchschnittsDauerLetzteAufrufe();
        LOG.info("AnzahlAufrufeLetzteMinute           :" + anzahlAufrufeLetzteMinute);
        LOG.info("AnzahlFehlerLetzteMinute            :" + anzahlFehlerLetzteMinute);
        LOG.info("DurchschnittsDauerLetzteZehnAufrufe :" + durchschnittsDauerLetzteZehnAufrufeReferenz);
        // the statistics from the current minute are not yet populated
        assertEquals(0, anzahlAufrufeLetzteMinute);
        assertEquals(0, anzahlFehlerLetzteMinute);
        assertTrue(durchschnittsDauerLetzteZehnAufrufeReferenz > 0);
        
        // wait 60 seconds and check the values.
        LOG.debug("Warte 1 Minute ...");
        Thread.sleep(60*secondMultiplicity);
        anzahlAufrufeLetzteMinute = mbean.getAnzahlAufrufeLetzteMinute();
        anzahlFehlerLetzteMinute = mbean.getAnzahlFehlerLetzteMinute();
        durchschnittsDauerLetzteZehnAufrufe = mbean.getDurchschnittsDauerLetzteAufrufe();
        LOG.info("AnzahlAufrufeLetzteMinute           :" + anzahlAufrufeLetzteMinute);
        LOG.info("AnzahlFehlerLetzteMinute            :" + anzahlFehlerLetzteMinute);
        LOG.info("DurchschnittsDauerLetzteZehnAufrufe :" + durchschnittsDauerLetzteZehnAufrufe);
		if (secondMultiplicity >= 1000) {
			assertEquals(anzahlAufrufe, anzahlAufrufeLetzteMinute);
			assertTrue(anzahlFehlerLetzteMinute > 0);
			assertEquals(durchschnittsDauerLetzteZehnAufrufe, durchschnittsDauerLetzteZehnAufrufeReferenz);
		}
        LOG.info("Prüfungen für 1. Minute erfolgreich.");
    }
    
    /**
     * Testet ZaehleAufruf von ServiceStatistikMBean mit 1. Minute ohne MBean Aufruf. 
     */
    @Test
    public void testZaehleAufrufNachAbstand() throws Exception {
        ServiceStatistikMBean mbean = new ServiceStatistikMBean();
        Random random = new SecureRandom();
        // wait till the minute starts.
        LOG.debug("Warte bis anfang naechste Minute ...");
        Thread.sleep(((61*secondMultiplicity) - (System.currentTimeMillis() % (60*secondMultiplicity))));
        int anzahlAufrufe = 10;
        int anzahlAufrufeLetzteMinute = 0;
        int anzahlFehlerLetzteMinute = 0;
        long durchschnittsDauerLetzteZehnAufrufe = 0;
        long durchschnittsDauerLetzteZehnAufrufeReferenz = 0;
        for (int count = 0; count < anzahlAufrufe; count++) {
            long dauer = random.nextInt(10*secondMultiplicity);
            boolean erfolgreich = random.nextBoolean();
            mbean.zaehleAufruf(dauer, erfolgreich);
            LOG.debug("Rufe MBean.zaehleAufruf auf mit (" + dauer + "," + erfolgreich + ")");
        }

        anzahlAufrufeLetzteMinute = mbean.getAnzahlAufrufeLetzteMinute();
        anzahlFehlerLetzteMinute = mbean.getAnzahlFehlerLetzteMinute();
        durchschnittsDauerLetzteZehnAufrufeReferenz = mbean.getDurchschnittsDauerLetzteAufrufe();
        LOG.info("AnzahlAufrufeLetzteMinute           :" + anzahlAufrufeLetzteMinute);
        LOG.info("AnzahlFehlerLetzteMinute            :" + anzahlFehlerLetzteMinute);
        LOG.info("DurchschnittsDauerLetzteZehnAufrufe :" + durchschnittsDauerLetzteZehnAufrufeReferenz);
        // the statistics from the current minute are not yet populated
        assertEquals(0, anzahlAufrufeLetzteMinute);
        assertEquals(0, anzahlFehlerLetzteMinute);
        assertTrue(durchschnittsDauerLetzteZehnAufrufeReferenz > 0);

        // wait 120 seconds and check the values. Since there were no calls in the last minute
        // number of calls and number of erroneous calls should be zero.
        LOG.debug("Warte 2 Minute ...");
        Thread.sleep(120*secondMultiplicity);
        anzahlAufrufeLetzteMinute = mbean.getAnzahlAufrufeLetzteMinute();
        anzahlFehlerLetzteMinute = mbean.getAnzahlFehlerLetzteMinute();
        durchschnittsDauerLetzteZehnAufrufe = mbean.getDurchschnittsDauerLetzteAufrufe();
        LOG.info("AnzahlAufrufeLetzteMinute           :" + anzahlAufrufeLetzteMinute);
        LOG.info("AnzahlFehlerLetzteMinute            :" + anzahlFehlerLetzteMinute);
        LOG.info("DurchschnittsDauerLetzteZehnAufrufe :" + durchschnittsDauerLetzteZehnAufrufe);
        assertEquals(0, anzahlAufrufeLetzteMinute);
        assertEquals(0, anzahlFehlerLetzteMinute);
        assertEquals(durchschnittsDauerLetzteZehnAufrufe,durchschnittsDauerLetzteZehnAufrufeReferenz);
        LOG.info("Prüfungen für 2. Minute erfolgreich.");
    }   
    
    @Test
    public void testGetDurchschnittsDauerLetzteAufrufe(){
    	ServiceStatistikMBean mbean = new ServiceStatistikMBean();
    	assertEquals(0, mbean.getDurchschnittsDauerLetzteAufrufe());
    }
    
    @Test
    public void testInvoke(){
    	ServiceStatistikMBean mbean = new ServiceStatistikMBean();
    	DummyService dummy = new DummyServiceImpl();
		ProxyFactory proxFac = new ProxyFactory(dummy);
		proxFac.addAdvice(mbean);
		DummyService proxy = (DummyService) proxFac.getProxy();
		Appender mockApp = Mockito.mock(Appender.class);
		ArgumentCaptor<LoggingEvent> mockArguCap = ArgumentCaptor.forClass(LoggingEvent.class);
		Logger log = Logger.getLogger(ServiceStatistikMBean.class);
		log.addAppender(mockApp);
		proxy.dummy();
		Mockito.verify(mockApp ,Mockito.times(1)).doAppend(mockArguCap.capture());
    }
    
    @Test(expected = PlisBusinessToException.class)
    public void testInvokemitException() throws PlisBusinessToException{
    	ServiceStatistikMBean mbean = new ServiceStatistikMBean();
    	DummyService dummy = new DummyServiceImpl();
		ProxyFactory proxFac = new ProxyFactory(dummy);
		proxFac.addAdvice(mbean);
		DummyService proxy = (DummyService) proxFac.getProxy();
		proxy.dummyMitException();
    }
}
