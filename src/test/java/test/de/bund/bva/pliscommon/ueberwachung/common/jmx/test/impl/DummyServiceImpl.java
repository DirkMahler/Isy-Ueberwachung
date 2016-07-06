package test.de.bund.bva.pliscommon.ueberwachung.common.jmx.test.impl;

import de.bund.bva.pliscommon.exception.service.PlisBusinessToException;
import test.de.bund.bva.pliscommon.ueberwachung.common.jmx.test.DummyService;

public class DummyServiceImpl implements DummyService {

	@Override
	public void dummy() {
		// macht nix..
		
	}

	@Override
	public void dummyMitException() throws PlisBusinessToException {
		throw new PlisBusinessToException(null, null, null) {
		};
		
	}
	
	

}
