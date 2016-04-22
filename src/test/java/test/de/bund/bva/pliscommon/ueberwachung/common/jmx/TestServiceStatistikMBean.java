package test.de.bund.bva.pliscommon.ueberwachung.common.jmx;

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

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.junit.Test;

import de.bund.bva.pliscommon.ueberwachung.common.jmx.ServiceStatistikMBean;

/**
 * Tests für ServiceStatistikMBean.
 * 
 */
public class TestServiceStatistikMBean extends TestCase {
    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(TestServiceStatistikMBean.class);

    /**
     * Testet ZaehleAufruf von ServiceStatistikMBean in Erste Minute.
     */
    @Test
    public void testZaehleAufrufErsteMinute() throws Exception {
        ServiceStatistikMBean mbean = new ServiceStatistikMBean();
        Random random = new SecureRandom();
        LOG.debug("Warte bis anfang naechste Minute ...");
        Thread.sleep((61000 - (System.currentTimeMillis() % 60000)));
        
        int anzahlAufrufe = 10;
        int anzahlAufrufeLetzteMinute = 0;
        int anzahlFehlerLetzteMinute = 0;
        long durchschnittsDauerLetzteZehnAufrufe = 0;
        long durchschnittsDauerLetzteZehnAufrufeReferenz = 0;
        for (int count = 0; count < anzahlAufrufe; count++) {
            long dauer = random.nextInt(10000);
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
        Thread.sleep(60000);
        anzahlAufrufeLetzteMinute = mbean.getAnzahlAufrufeLetzteMinute();
        anzahlFehlerLetzteMinute = mbean.getAnzahlFehlerLetzteMinute();
        durchschnittsDauerLetzteZehnAufrufe = mbean.getDurchschnittsDauerLetzteAufrufe();
        LOG.info("AnzahlAufrufeLetzteMinute           :" + anzahlAufrufeLetzteMinute);
        LOG.info("AnzahlFehlerLetzteMinute            :" + anzahlFehlerLetzteMinute);
        LOG.info("DurchschnittsDauerLetzteZehnAufrufe :" + durchschnittsDauerLetzteZehnAufrufe);
        assertEquals(anzahlAufrufe, anzahlAufrufeLetzteMinute);
        assertTrue(anzahlFehlerLetzteMinute > 0);
        assertEquals(durchschnittsDauerLetzteZehnAufrufe,durchschnittsDauerLetzteZehnAufrufeReferenz);
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
        Thread.sleep((61000 - (System.currentTimeMillis() % 60000)));
        int anzahlAufrufe = 10;
        int anzahlAufrufeLetzteMinute = 0;
        int anzahlFehlerLetzteMinute = 0;
        long durchschnittsDauerLetzteZehnAufrufe = 0;
        long durchschnittsDauerLetzteZehnAufrufeReferenz = 0;
        for (int count = 0; count < anzahlAufrufe; count++) {
            long dauer = random.nextInt(10000);
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
        Thread.sleep(120000);
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
}
