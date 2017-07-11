package com.connexta.cellar.ext;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.UUID;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write configs using config admin.
 */
public class ConfigWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigWriter.class);

    private static final String FACTORY_PID = "com.connexta.cellar.LogLoopImpl";

    private static final String CONFIG_UUID = UUID.randomUUID()
            .toString();

    private final ConfigurationAdmin configurationAdmin;

    private final boolean useGet;

    private Configuration config;

    public ConfigWriter(final ConfigurationAdmin configurationAdmin, final boolean useGet) {
        this.configurationAdmin = configurationAdmin;
        this.useGet = useGet;

        config = null;
    }

    public void init() throws Exception {
        if (useGet) {
            config = configurationAdmin.getConfiguration(FACTORY_PID + "-" + CONFIG_UUID, null);
        } else {
            config = configurationAdmin.createFactoryConfiguration(FACTORY_PID, null);
        }

        LOGGER.warn(config.toString());
        updateConfig(config);
    }

    public void destroy() throws Exception {
        if (config != null) {
            config.delete();
        } else {
            LOGGER.error("Config was null");
        }
    }

    private void updateConfig(Configuration config) throws IOException {
        Dictionary defaultProps = config.getProperties();
        Hashtable props = new Hashtable();
        if (defaultProps != null) {
            LOGGER.warn(defaultProps.toString());
            Enumeration enumProps = defaultProps.keys();
            while (enumProps.hasMoreElements()) {
                Object key = enumProps.nextElement();
                props.put(key, defaultProps.get(key));
            }
        }
        config.update(props);
    }
}
