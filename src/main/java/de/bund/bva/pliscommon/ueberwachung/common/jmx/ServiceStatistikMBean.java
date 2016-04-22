package de.bund.bva.pliscommon.ueberwachung.common.jmx;

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


import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.bund.bva.pliscommon.exception.service.PlisBusinessToException;

/**
 * Diese Klasse implementiert eine Überwachungs-MBean für Services. Sie liefert
 * die Überwachungsoptionen, welche jeder Service der PLIS anbieten muss.
 * 
 */
@ManagedResource(description = "Diese MBean liefert Überwacht die Aufrufe eines Services.")
public class ServiceStatistikMBean implements MethodInterceptor {
    /**
     * Standard-Wert fuer Anzahl Suchen, anhand derer der Durchschnitt berechnet
     * wird.
     */
    private static final int ANZAHL_AUFRUFE_FUER_DURCHSCHNITT = 10;

    /**
     * Dauern der letzten Such-Aufrufe (in Millisekunden).
     */
    private List<Long> letzteSuchdauern = new LinkedList<Long>();

    /**
     * Merker für die Minute, in der Werte der letzten Minute ermittelt wurden.
     */
    private volatile int letzteMinute;

    /**
     * Anzahl der nicht fehlerhaften Aufrufe, die in der durch letzteMinute
     * bezeichneten Minute durchgeführt wurden.
     * 
     */
    private volatile int anzahlAufrufeLetzteMinute;

    /**
     * Anzahl der nicht fehlerhaften Aufrufe, die in der aktuellen Minute
     * durchgeführt wurden.
     */
    private volatile int anzahlAufrufeAktuelleMinute;

    /**
     * Anzahl der Aufrufe, die in der durch letzteMinute bezeichneten Minute
     * durchgeführt wurden, bei denen ein techinscher Fehler aufgetreten ist.
     */
    private volatile int anzahlFehlerLetzteMinute;

    /**
     * Anzahl der Aufrufe, die in der aktuellen Minute durchgeführt wurden, bei
     * denen ein techinscher Fehler aufgetreten ist.
     */
    private volatile int anzahlFehlerAktuelleMinute;

    /**
     * Logger.
     */
    private static final Logger LOG = Logger.getLogger(ServiceStatistikMBean.class);

    /**
     * Diese Methode zählt einen Aufruf der Komponente für die Statistik. Für
     * die Statistik wird die Angabe der Dauer und ob der Aufruf fehlerhaft war
     * benötigt.
     * @param dauer
     *            Die Dauer des Aufrufs in Millisekunden.
     * @param erfolgreich
     *            Kennzeichen, ob der Aufruf erfolgreich war (<code>true</code>)
     *            oder ein technischer Fehler aufgetreten ist (<code>false</code>).
     */
    public synchronized void zaehleAufruf(long dauer, boolean erfolgreich) {
        aktualisiereZeitfenster();
        anzahlAufrufeAktuelleMinute++;
        if (!erfolgreich) {
            anzahlFehlerAktuelleMinute++;
        }
        if (letzteSuchdauern.size() == ANZAHL_AUFRUFE_FUER_DURCHSCHNITT) {
            letzteSuchdauern.remove(ANZAHL_AUFRUFE_FUER_DURCHSCHNITT - 1);
        }
        letzteSuchdauern.add(0, dauer);

    }

    /**
     * Diese Methode veranlasst, dass das Zeitfenster für die Zähler der Fehler
     * und Aufrufe in der aktuellen und letzten Minute aktualisiert wird. Falls
     * eine Minute verstrichen ist, werden die Werte der aktuellen Minute in die
     * der Zähler für die letzte Minut kopiert. Die Zähler für die aktuelle
     * Minute werden auf 0 gesetzt. Die Methode sorg dafür, dass dieser Vorgang
     * nur einmal pro Minute ausgeführt werden kann.
     */
    private synchronized void aktualisiereZeitfenster() {
        int aktuelleMinute = getAktuelleMinute();
        if (aktuelleMinute != letzteMinute) {
            if ((aktuelleMinute - letzteMinute) > 1) {
                // keine infos von letzten Minute
                anzahlAufrufeLetzteMinute = 0;
                anzahlFehlerLetzteMinute = 0;
            } else {
                anzahlAufrufeLetzteMinute = anzahlAufrufeAktuelleMinute;
                anzahlFehlerLetzteMinute = anzahlFehlerAktuelleMinute;
            }

            anzahlAufrufeAktuelleMinute = 0;
            anzahlFehlerAktuelleMinute = 0;
            letzteMinute = aktuelleMinute;
        }
    }

    /**
     * Berechnet die aktuelle Minute der Systemzeit.
     * @return Der Minuten-Anteil der aktuellen Systemzeit
     */
    private static final int getAktuelleMinute() {
        return (int) (System.currentTimeMillis() / 60000);
    }

    /**
     * Liefert die durchschnittliche Dauer der letzten 10 Aurufe. Definiert eine
     * Methode für das Management-Interface dieser MBean.
     * @return Die durchschnittliche Dauer der letzten 10 Aufrufe in ms.
     */
    @ManagedAttribute(description = "Liefert die durchschnittliche Dauer der letzten 10 Aufrufe in ms.")
    public long getDurchschnittsDauerLetzteAufrufe() {
        long result = 0;
        if (letzteSuchdauern.size() > 0) {
            // Kopiere Liste um konkurrierende Änderungen zu vermeiden
            // Explizit keine Synchronisierung, um die Anwendungsperformance
            // nicht zu verschlechtern.
            Long[] dauern = letzteSuchdauern.toArray(new Long[0]);
            for (long dauer : dauern) {
                result += dauer;
            }
            result /= letzteSuchdauern.size();
        }
        return result;
    }

    /**
     * Liefert die Anzahl der in der letzten Minute gezählten Aufrufe, bei denen
     * kein Fehler aufgetreten ist. Definiert eine Methode für das
     * Management-Interface dieser MBean.
     * @return Die Anzahl der in der letzten Minute gezählten Aufrufe, bei denen
     *         kein Fehler aufgetreten ist.
     */
    @ManagedAttribute(description = "Liefert die Anzahl der nicht fehlerhaften Aufrufe in der letzten Minute")
    public int getAnzahlAufrufeLetzteMinute() {
        aktualisiereZeitfenster();
        return anzahlAufrufeLetzteMinute;
    }

    /**
     * Liefert die Anzahl der in der letzten Minute gezählten Aufrufe, bei denen
     * ein Fehler aufgetreten ist. Definiert eine Methode für das
     * Management-Interface dieser MBean.
     * @return Die Anzahl der in der letzten Minute gezählten Aufrufe, bei denen
     *         ein Fehler aufgetreten ist.
     */
    @ManagedAttribute(description = "Liefert die Anzahl der fehlerhaften Aufrufe in der letzten Minute")
    public int getAnzahlFehlerLetzteMinute() {
        aktualisiereZeitfenster();
        return anzahlFehlerLetzteMinute;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        long startZeit = System.currentTimeMillis();
        boolean erfolgreich = false;
        try {
            Object result = invocation.proceed();
            erfolgreich = true;
            return result;
        } catch (PlisBusinessToException t) {
            // BusinessExceptions werden nicht als Fehler gezählt.
            erfolgreich = true;
            throw t;
        } finally {
            long aufrufDauer = System.currentTimeMillis() - startZeit;
            this.zaehleAufruf(aufrufDauer, erfolgreich);
            Method method = invocation.getMethod();
            LOG.info("Methode '" + method.getDeclaringClass().getName() + "." + method.getName()
                    + "' durchgefuehrt in " + aufrufDauer + "ms.");
        }
    }

}
