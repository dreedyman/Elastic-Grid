import org.rioproject.config.Component

@Component('org.rioproject.tools.ui')
class UI {
    waitMessage="Waiting to discover the Elastic Grid ...";
    title="Elastic Grid Management Console";
    bannerIcon="com/elasticgrid/tools/ui/Elastic-Grid-Logo.png";
    aboutIcon="com/elasticgrid/tools/ui/Elastic-Grid-Logo.png";
    aboutInfo="Elastic Grid<br>Management Console<br>";
    build="Early Access";
    version="${pom.version}";
}