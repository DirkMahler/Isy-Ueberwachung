package test.de.bund.bva.pliscommon.ueberwachung.common.jmx;

import static org.junit.Assert.*;

import org.junit.Test;

import de.bund.bva.pliscommon.ueberwachung.common.jmx.StatusMonitorMBean;

public class TestStatusMonitorMBean {

	@Test
	public void testStatusMonitor() {
		StatusMonitorMBean statMBean = new StatusMonitorMBean();
		statMBean.registrierePruefung(true);
		assertTrue(statMBean.isLetztePruefungErfolgreich());
		statMBean.registrierePruefung(false);
		assertFalse(statMBean.isLetztePruefungErfolgreich());
		
	}
	
	@Test
	public void testGetLetztePrüfung(){
		StatusMonitorMBean statMBean = new StatusMonitorMBean();
		statMBean.registrierePruefung(true);
		long interval = System.currentTimeMillis() - statMBean.getZeitpunktLetztePruefung().getTime();
		assertTrue(interval <= 100);
	}
	

}
