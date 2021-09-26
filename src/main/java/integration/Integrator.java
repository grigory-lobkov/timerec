package integration;

import integration.impl.DefaultIntegrator;
import integration.impl.MoodleIntegrator;

import java.util.NoSuchElementException;

public class Integrator {

    public static final IIntegrator INSTANCE = getIntegrator();

    private static final String INTEGRATOR_TYPE = getProperty("INTEGRATOR_TYPE");

    private static IIntegrator getIntegrator() {
        switch (INTEGRATOR_TYPE != null ? INTEGRATOR_TYPE.toUpperCase() : "") {
            case "MOODLE":
                return new MoodleIntegrator();
            case "":
                return new DefaultIntegrator();
            default:
                throw new NoSuchElementException("Environment variable INTEGRATOR_TYPE=" + INTEGRATOR_TYPE + " have unsupported value");
        }
    }

    public static String getProperty(String name) {
        String prop = System.getProperty(name);
        return prop != null ? prop : System.getenv(name);
    }

    public static Integer getIntProperty(String name) {
        String prop = getProperty(name);
        return prop != null ? Integer.parseInt(prop) : null;
    }

}
