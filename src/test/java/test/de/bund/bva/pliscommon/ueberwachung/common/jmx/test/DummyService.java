package test.de.bund.bva.pliscommon.ueberwachung.common.jmx.test;

import de.bund.bva.pliscommon.exception.service.PlisBusinessToException;

public interface DummyService {

	public void dummy();
	
	public void dummyMitException() throws PlisBusinessToException;
}
