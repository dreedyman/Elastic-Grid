/**
 * Elastic Grid
 * Copyright (C) 2008-2010 Elastic Grid, LLC.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
deployment(name:'My Sample Webapp') {
    groups('elastic-grid')
    serviceExec(name:'Tomcat') {
        software(name:'Tomcat', version:'6.0.20', removeOnDestroy: true) {
            install source:'https://elastic-grid-substrates.s3.amazonaws.com/tomcat/apache-tomcat-6.0.20.zip',
                    target:'tomcat', unarchive: true
            postInstall(removeOnCompletion: true) {
                execute command: '/bin/chmod +x bin/*.sh'
            }
        }
        execute inDirectory:'bin', command: 'catalina.sh run'
        data source:'https://javaone-demo.s3.amazonaws.com/video-conversion-oar/video-conversion.war',
             target:'tomcat/apache-tomcat-6.0.20/webapps'
        maintain 1
    }
}
