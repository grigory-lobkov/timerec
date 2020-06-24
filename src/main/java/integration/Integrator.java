package integration;

import integration.impl.MoodleIntegrator;

public class Integrator {

    static private volatile IIntegrator instance = null;

    /**
     * Generate Schedule storage actions
     *
     * @return singleton instance
     */
    public static IIntegrator getInstance() {
        if (instance == null)
            synchronized (Integrator.class) {
                if (instance == null) {
                    instance = new MoodleIntegrator();
                }
            }
        return instance;
    }

}
