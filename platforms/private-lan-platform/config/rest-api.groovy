/*
 * Configuration for the REST service
 */
import org.rioproject.config.Component
import org.rioproject.config.Constants
import org.rioproject.core.ClassBundle
import org.rioproject.log.LoggerConfig
import org.rioproject.log.LoggerConfig.LogHandlerConfig
import org.rioproject.fdh.FaultDetectionHandlerFactory
import java.util.logging.ConsoleHandler
import java.util.logging.Level

/*
 * Declare REST API properties
 */
@Component('com.elasticgrid.rest')
class RestServiceConfig {
    String serviceName = 'Elastic Grid REST API'
    String serviceComment = 'Elastic Grid REST API'
    String jmxName = 'com.elasticgrid.rest:type=API'

    String[] getInitialLookupGroups() {
        def groups = [System.getProperty(Constants.GROUPS_PROPERTY_NAME,
                      'elastic-grid')]
        return groups as String[]
    }

}
