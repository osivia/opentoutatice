package fr.toutatice.ecm.platform.service.fragments;



public class FragmentServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1895063574496443379L;

    public FragmentServiceException(String message) {
        super(message);
    }

    public FragmentServiceException(Exception e) {
        super(e);
    }

    public FragmentServiceException(Exception e, String message) {
        super(message, e);
    }
}
