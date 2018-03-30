/**
 * 
 */
package org.opentoutatice.ecm.reporting.test.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.nuxeo.runtime.api.Framework;


/**
 * @author david
 *
 */
public class ErrorTestMode {

    /** Error test mode parameter key. */
    public static final String ERROR_TEST_MODE = "ottc.scan.error.test.mode";

    /** Indicator to generate use case error. */
    private static int GENERATE_USE_CASE_ERROR_INDICATOR = 0;

    /** Genetaed use case error. */
    private static List<Integer> generatedUCErrors = new ArrayList<Integer>(5);

    /** Use case error generated in try (catch). */
    private static Map<Integer, Boolean> generatedUCErrorsInTry = new HashMap<Integer, Boolean>(1);

    /**
     * Utility class.
     */
    private ErrorTestMode() {
        super();
    }

    /** Increments UC errors indicator. */
    public static void incrementUCErrorsIndicator() {
        GENERATE_USE_CASE_ERROR_INDICATOR++;
    }

    /** Reset generated use cases errors. */
    public static void resetGeneratedUseCaseErrors() {
        if (isActivated()) {
            GENERATE_USE_CASE_ERROR_INDICATOR = 0;
            generatedUCErrors = new ArrayList<>(5);
            generatedUCErrorsInTry = new HashMap<Integer, Boolean>(1);
        }
    }

    /**
     * Checks if error test mode is activated.
     * 
     * @return boolean
     */
    public static boolean isActivated() {
        String mode = Framework.getProperty(ERROR_TEST_MODE, "false");
        return BooleanUtils.isTrue(Boolean.valueOf(mode));
    }

    /**
     * Indicates to generate UC error.
     * 
     * @throws ErrorTestModeException
     */
    public static boolean generateError(int uCError) throws ErrorTestModeException {
        // Result
        boolean generate = false;

        if (isActivated()) {

            boolean hasToGenerate = GENERATE_USE_CASE_ERROR_INDICATOR % 2 == 0;

            if (hasToGenerate && !generatedUCErrors.contains(uCError)) {
                generatedUCErrors.add(uCError);
                generate = true;
            }

        }

        return generate;
    }

    /**
     * Indicates to generate UC error in try of try / catch block.
     * 
     * @param uCError
     * @return boolean
     * @throws ErrorTestModeException
     */
    public static boolean generateErrorInTry(int uCError) throws ErrorTestModeException {
        // Result
        boolean generate = false;

        if (isActivated()) {

            boolean hasToGenerate = GENERATE_USE_CASE_ERROR_INDICATOR % 2 == 0;

            if (hasToGenerate) {
                if (!generatedUCErrorsInTry.keySet().contains(uCError)) {
                    generatedUCErrorsInTry.put(uCError, Boolean.TRUE);
                    generate = true;
                } else {
                    Boolean toGenerate = generatedUCErrorsInTry.get(uCError);
                    generate = BooleanUtils.isTrue(toGenerate);
                    if (generate) {
                        generatedUCErrorsInTry.put(uCError, Boolean.FALSE);
                    }
                }
            }
        }

        return generate;
    }


}
