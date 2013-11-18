package fr.toutatice.ecm.platform.core.utils.exception;

public class ToutaticeException extends Exception {

	private static final long serialVersionUID = 935981606551477611L;

	public ToutaticeException(Exception e) {
		super(e);
	}
	
	public ToutaticeException(String message) {
		super(message);
	}

}
