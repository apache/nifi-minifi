/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nifi.minifi.bootstrap.util;


import org.apache.nifi.connectable.Connection;
import org.apache.nifi.connectable.StandardConnection;
import org.apache.nifi.controller.FlowController;
import org.apache.nifi.controller.ProcessScheduler;
import org.apache.nifi.controller.ProcessorNode;
import org.apache.nifi.controller.exception.ProcessorInstantiationException;
import org.apache.nifi.controller.queue.FlowFileQueue;
import org.apache.nifi.controller.scheduling.StandardProcessScheduler;
import org.apache.nifi.encrypt.StringEncryptor;
import org.apache.nifi.flowfile.FlowFilePrioritizer;
import org.apache.nifi.groups.RemoteProcessGroup;
import org.apache.nifi.groups.RemoteProcessGroupPortDescriptor;
import org.apache.nifi.logging.LogLevel;
import org.apache.nifi.persistence.StandardXMLFlowConfigurationDAO;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.remote.StandardRemoteProcessGroupPortDescriptor;
import org.apache.nifi.scheduling.SchedulingStrategy;
import org.apache.nifi.util.NiFiProperties;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

public final class ConfigTransformer {
    // Underlying version NIFI POC will be using
    public static final String NIFI_VERSION = "0.6.0";

    public static final String NAME_KEY = "name";
    public static final String COMMENT_KEY = "comment";
    public static final String ALWAYS_SYNC_KEY = "always sync";
    public static final String YIELD_PERIOD_KEY = "yield period";
    public static final String MAX_CONCURRENT_TASKS_KEY = "max concurrent tasks";
    public static final String ID_KEY = "id";

    public static final String FLOW_CONTROLLER_PROPS_KEY = "Flow Controller";

    public static final String CORE_PROPS_KEY = "Core Properties";
    public static final String FLOW_CONTROLLER_SHUTDOWN_PERIOD_KEY = "flow controller graceful shutdown period";
    public static final String FLOW_SERVICE_WRITE_DELAY_INTERVAL_KEY = "flow service write delay interval";
    public static final String ADMINISTRATIVE_YIELD_DURATION_KEY = "administrative yield duration";
    public static final String BORED_YIELD_DURATION_KEY = "bored yield duration";

    public static final String FLOWFILE_REPO_KEY = "FlowFile Repository";
    public static final String PARTITIONS_KEY = "partitions";
    public static final String CHECKPOINT_INTERVAL_KEY = "checkpoint interval";
    public static final String THRESHOLD_KEY = "queue swap threshold";
    public static final String SWAP_PROPS_KEY = "Swap";
    public static final String IN_PERIOD_KEY = "in period";
    public static final String IN_THREADS_KEY = "in threads";
    public static final String OUT_PERIOD_KEY = "out period";
    public static final String OUT_THREADS_KEY = "out threads";


    public static final String CONTENT_REPO_KEY = "Content Repository";
    public static final String CONTENT_CLAIM_MAX_APPENDABLE_SIZE_KEY = "content claim max appendable size";
    public static final String CONTENT_CLAIM_MAX_FLOW_FILES_KEY = "content claim max flow files";

    public static final String COMPONENT_STATUS_REPO_KEY = "Component Status Repository";
    public static final String BUFFER_SIZE_KEY = "buffer size";
    public static final String SNAPSHOT_FREQUENCY_KEY = "snapshot frequency";

    public static final String SECURITY_PROPS_KEY = "Security Properties";
    public static final String KEYSTORE_KEY = "keystore";
    public static final String KEYSTORE_TYPE_KEY = "keystore type";
    public static final String KEYSTORE_PASSWORD_KEY = "keystore password";
    public static final String KEY_PASSWORD_KEY = "key password";
    public static final String TRUSTSTORE_KEY = "truststore";
    public static final String TRUSTSTORE_TYPE_KEY = "truststore type";
    public static final String TRUSTSTORE_PASSWORD_KEY = "truststore password";
    public static final String SENSITIVE_PROPS_KEY = "Sensitive Props";
    public static final String SENSITIVE_PROPS_KEY__KEY = "key";
    public static final String SENSITIVE_PROPS_ALGORITHM_KEY = "algorithm";
    public static final String SENSITIVE_PROPS_PROVIDER_KEY = "provider";

    public static final String PROCESSOR_CONFIG_KEY = "Processor Configuration";
    public static final String CLASS_KEY = "class";
    public static final String SCHEDULING_PERIOD_KEY = "scheduling period";
    public static final String PENALIZATION_PERIOD_KEY = "penalization period";
    public static final String SCHEDULING_STRATEGY_KEY = "scheduling strategy";
    public static final String RUN_DURATION_NANOS_KEY = "run duration nanos";

    public static final String PROCESSOR_PROPS_KEY = "Properties";

    public static final String CONNECTION_PROPS_KEY = "Connection Properties";
    public static final String MAX_WORK_QUEUE_SIZE_KEY = "max work queue size";
    public static final String MAX_WORK_QUEUE_DATA_SIZE_KEY = "max work queue data size";
    public static final String FLOWFILE_EXPIRATION__KEY = "flowfile expiration";
    public static final String QUEUE_PRIORITIZER_CLASS_KEY = "queue prioritizer class";

    public static final String REMOTE_PROCESSING_GROUP_KEY = "Remote Processing Group";
    public static final String URL_KEY = "url";
    public static final String TIMEOUT_KEY = "timeout";

    public static final String INPUT_PORT_KEY = "Input Port";
    public static final String USE_COMPRESSION_KEY = "use compression";


    // Final util classes should have private constructor
    private ConfigTransformer() {}

    public static void main(String[] args) throws IOException, ClassNotFoundException, ProcessorInstantiationException, InstantiationException, IllegalAccessException {
        transformConfigFile("./config.yml",
                "./");
    }

    public static void transformConfigFile(String sourceFile, String destPath) throws IOException, ClassNotFoundException, ProcessorInstantiationException, InstantiationException, IllegalAccessException {
        Yaml yaml = new Yaml();
        File ymlConfigFile = new File(sourceFile);
        InputStream ios = new FileInputStream(ymlConfigFile);

        // Parse the YAML file
        Map<String,Object> result = (Map<String,Object>) yaml.load(ios);

        // Write nifi.properties and flow.xml.gz
        writeNiFiProperties(result, destPath);
        writeFlowXml(result, destPath);
    }

    public static void transformConfigFile(InputStream sourceStream, String destPath) throws IOException, ClassNotFoundException, ProcessorInstantiationException, InstantiationException, IllegalAccessException {
        Yaml yaml = new Yaml();

        // Parse the YAML file
        Map<String,Object> result = (Map<String,Object>) yaml.load(sourceStream);

        // Write nifi.properties and flow.xml.gz
        writeNiFiProperties(result, destPath);
        writeFlowXml(result, destPath);
    }

    private static void writeNiFiProperties(Map<String, Object> topLevelYaml, String path) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(path+"nifi.properties", "UTF-8");

            Map<String,Object> coreProperties = (Map<String, Object>) topLevelYaml.get(CORE_PROPS_KEY);
            Map<String,Object> flowfileRepo = (Map<String, Object>) topLevelYaml.get(FLOWFILE_REPO_KEY);
            Map<String, Object> swapProperties = (Map<String, Object>) flowfileRepo.get(SWAP_PROPS_KEY);
            Map<String,Object> contentRepo = (Map<String, Object>) topLevelYaml.get(CONTENT_REPO_KEY);
            Map<String,Object> componentStatusRepo = (Map<String, Object>) topLevelYaml.get(COMPONENT_STATUS_REPO_KEY);
            Map<String,Object> securityProperties = (Map<String, Object>) topLevelYaml.get(SECURITY_PROPS_KEY);
            Map<String,Object> sensitiveProperties = (Map<String, Object>) securityProperties.get(SENSITIVE_PROPS_KEY);


            writer.print(PROPERTIES_FILE_APACHE_2_0_LICENSE);
            writer.println("# Core Properties #");
            writer.println();
            writer.println("nifi.version="+NIFI_VERSION);
            writer.println("nifi.flow.configuration.file=./conf/flow.xml.gz");
            writer.println("nifi.flow.configuration.archive.dir=./conf/archive/");
            writer.println("nifi.flowcontroller.graceful.shutdown.period=" + getValueString(coreProperties, FLOW_CONTROLLER_SHUTDOWN_PERIOD_KEY));
            writer.println("nifi.flowservice.writedelay.interval=" + getValueString(coreProperties, FLOW_SERVICE_WRITE_DELAY_INTERVAL_KEY));
            writer.println("nifi.administrative.yield.duration=" + getValueString(coreProperties, ADMINISTRATIVE_YIELD_DURATION_KEY));
            writer.println("# If a component has no work to do (is \"bored\"), how long should we wait before checking again for work?");
            writer.println("nifi.bored.yield.duration=" + getValueString(coreProperties, BORED_YIELD_DURATION_KEY));
            writer.println();
            writer.println("nifi.ui.banner.text= ");
            writer.println("nifi.nar.library.directory=./lib");
            writer.println("nifi.nar.working.directory=./work/nar/");
            writer.println();
            writer.println("# FlowFile Repository");
            writer.println("nifi.flowfile.repository.implementation=org.apache.nifi.controller.repository.WriteAheadFlowFileRepository");
            writer.println("nifi.flowfile.repository.directory=./flowfile_repository");
            writer.println("nifi.flowfile.repository.partitions=" + getValueString(flowfileRepo, PARTITIONS_KEY));
            writer.println("nifi.flowfile.repository.checkpoint.interval=" + getValueString(flowfileRepo,CHECKPOINT_INTERVAL_KEY));
            writer.println("nifi.flowfile.repository.always.sync=" + getValueString(flowfileRepo,ALWAYS_SYNC_KEY));
            writer.println();
            writer.println("nifi.swap.manager.implementation=org.apache.nifi.controller.FileSystemSwapManager");
            writer.println("nifi.queue.swap.threshold=" + getValueString(swapProperties, THRESHOLD_KEY));
            writer.println("nifi.swap.in.period=" + getValueString(swapProperties, IN_PERIOD_KEY));
            writer.println("nifi.swap.in.threads=" + getValueString(swapProperties, IN_THREADS_KEY));
            writer.println("nifi.swap.out.period=" + getValueString(swapProperties, OUT_PERIOD_KEY));
            writer.println("nifi.swap.out.threads=" + getValueString(swapProperties, OUT_THREADS_KEY));
            writer.println();
            writer.println("# Content Repository");
            writer.println("nifi.content.repository.implementation=org.apache.nifi.controller.repository.FileSystemRepository");
            writer.println("nifi.content.claim.max.appendable.size=" + getValueString(contentRepo, CONTENT_CLAIM_MAX_APPENDABLE_SIZE_KEY));
            writer.println("nifi.content.claim.max.flow.files=" + getValueString(contentRepo, CONTENT_CLAIM_MAX_FLOW_FILES_KEY));
            writer.println("nifi.content.repository.directory.default=./content_repository");
            writer.println("nifi.content.repository.always.sync=" + getValueString(contentRepo, ALWAYS_SYNC_KEY));
            writer.println();
            writer.println("# Component Status Repository");
            writer.println("nifi.components.status.repository.implementation=org.apache.nifi.controller.status.history.VolatileComponentStatusRepository");
            writer.println("nifi.components.status.repository.buffer.size=" + getValueString(componentStatusRepo, BUFFER_SIZE_KEY));
            writer.println("nifi.components.status.snapshot.frequency=" + getValueString(componentStatusRepo, SNAPSHOT_FREQUENCY_KEY));
            writer.println();
            writer.println("# web properties #");
            writer.println("nifi.web.war.directory=./lib");
            writer.println("nifi.web.http.host=");
            writer.println("nifi.web.http.port=8081");
            writer.println("nifi.web.https.host=");
            writer.println("nifi.web.https.port=");
            writer.println("nifi.web.jetty.working.directory=./work/jetty");
            writer.println("nifi.web.jetty.threads=200");
            writer.println();
            writer.println("# security properties #");
            writer.println("nifi.sensitive.props.key=" + getValueString(sensitiveProperties, SENSITIVE_PROPS_KEY__KEY));
            writer.println("nifi.sensitive.props.algorithm=" + getValueString(sensitiveProperties, SENSITIVE_PROPS_ALGORITHM_KEY));
            writer.println("nifi.sensitive.props.provider=" + getValueString(sensitiveProperties, SENSITIVE_PROPS_PROVIDER_KEY));
            writer.println();
            writer.println("nifi.security.keystore=" + getValueString(securityProperties, KEYSTORE_KEY));
            writer.println("nifi.security.keystoreType=" + getValueString(securityProperties, KEYSTORE_TYPE_KEY));
            writer.println("nifi.security.keystorePasswd=" + getValueString(securityProperties, KEYSTORE_PASSWORD_KEY));
            writer.println("nifi.security.keyPasswd=" + getValueString(securityProperties, KEY_PASSWORD_KEY));
            writer.println("nifi.security.truststore=" + getValueString(securityProperties, TRUSTSTORE_KEY));
            writer.println("nifi.security.truststoreType=" + getValueString(securityProperties, TRUSTSTORE_TYPE_KEY));
            writer.println("nifi.security.truststorePasswd=" + getValueString(securityProperties, TRUSTSTORE_PASSWORD_KEY));
        } finally {
            if (writer != null){
                writer.flush();
                writer.close();
            }
        }
    }
    private static void writeFlowXml(Map<String, Object> topLevelYaml, String path) throws IOException, ProcessorInstantiationException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        Map<String,Object> flowControllerProperties = (Map<String, Object>) topLevelYaml.get(FLOW_CONTROLLER_PROPS_KEY);
        Map<String,Object> processorConfig = (Map<String, Object>) topLevelYaml.get(PROCESSOR_CONFIG_KEY);
        Map<String,Object> processorProperties = (Map<String, Object>) processorConfig.get(PROCESSOR_PROPS_KEY);
        Map<String,Object> connectionProperties = (Map<String, Object>) topLevelYaml.get(CONNECTION_PROPS_KEY);
        Map<String,Object> remoteProcessingGroup = (Map<String, Object>) topLevelYaml.get(REMOTE_PROCESSING_GROUP_KEY);
        Map<String,Object> inputPort = (Map<String, Object>) remoteProcessingGroup.get(INPUT_PORT_KEY);

        // NiFiProperties niFiProperties = NiFiProperties.getInstance();
        StringEncryptor stringEncryptor = StringEncryptor.createEncryptor();
        FlowController flowController = FlowController.createStandaloneInstance(null, null, null, null, stringEncryptor);
        flowController.setComments(getValueString(flowControllerProperties, COMMENT_KEY));
        flowController.setName(getValueString(flowControllerProperties, NAME_KEY));
        flowController.setMaxEventDrivenThreadCount(Integer.parseInt(getValueString(processorConfig,MAX_CONCURRENT_TASKS_KEY)));
        flowController.setMaxTimerDrivenThreadCount(Integer.parseInt(getValueString(processorConfig,MAX_CONCURRENT_TASKS_KEY)));

        ProcessorNode processorNode = flowController.createProcessor(getValueString(processorConfig, CLASS_KEY), "processor");
        processorNode.setName(getValueString(processorConfig, NAME_KEY));
        processorNode.setComments(getValueString(processorConfig, COMMENT_KEY));
        processorNode.setMaxConcurrentTasks(Integer.parseInt(getValueString(processorConfig, MAX_CONCURRENT_TASKS_KEY)));
        processorNode.setSchedulingStrategy(SchedulingStrategy.valueOf(getValueString(processorConfig, SCHEDULING_STRATEGY_KEY)));
        processorNode.setPenalizationPeriod(getValueString(processorConfig, PENALIZATION_PERIOD_KEY));
        processorNode.setYieldPeriod(getValueString(processorConfig, YIELD_PERIOD_KEY));
        processorNode.setBulletinLevel(LogLevel.WARN);
        processorNode.setRunDuration(Long.parseLong(getValueString(processorConfig, RUN_DURATION_NANOS_KEY)), TimeUnit.NANOSECONDS);
        processorNode.setScheduldingPeriod(getValueString(processorConfig, SCHEDULING_PERIOD_KEY));

        for(String key: processorProperties.keySet()){
            processorNode.setProperty(key, getValueString(processorProperties, key));
        }

        RemoteProcessGroup remoteProcessGroup = flowController.createRemoteProcessGroup("Remote-Process-Group", getValueString(remoteProcessingGroup, URL_KEY));
        remoteProcessGroup.setName(getValueString(remoteProcessingGroup, NAME_KEY) );
        remoteProcessGroup.setComments(getValueString(remoteProcessingGroup, COMMENT_KEY));
        remoteProcessGroup.setCommunicationsTimeout(getValueString(remoteProcessingGroup, TIMEOUT_KEY));
        remoteProcessGroup.setYieldDuration(getValueString(remoteProcessingGroup, YIELD_PERIOD_KEY));

        StandardRemoteProcessGroupPortDescriptor remoteProcessGroupPortDescriptor = new StandardRemoteProcessGroupPortDescriptor();
        remoteProcessGroupPortDescriptor.setName(getValueString(inputPort, NAME_KEY));
        remoteProcessGroupPortDescriptor.setComments(getValueString(inputPort, COMMENT_KEY));
        remoteProcessGroupPortDescriptor.setId(getValueString(inputPort,ID_KEY));
        remoteProcessGroupPortDescriptor.setUseCompression(Boolean.valueOf(getValueString(inputPort, USE_COMPRESSION_KEY)));
        remoteProcessGroupPortDescriptor.setConcurrentlySchedulableTaskCount(Integer.valueOf(getValueString(inputPort, MAX_CONCURRENT_TASKS_KEY)));
        HashSet<RemoteProcessGroupPortDescriptor> inputPortSet = new HashSet<>();
        inputPortSet.add(remoteProcessGroupPortDescriptor);
        remoteProcessGroup.setInputPorts(inputPortSet);

        Connection connection = new StandardConnection.Builder(flowController.getProcessScheduler())
                .name(getValueString(connectionProperties, NAME_KEY))
                .id("Connection")
                .source(processorNode)
                .destination(remoteProcessGroup.getInputPort(getValueString(inputPort, NAME_KEY)))
                .relationships(Collections.singletonList(new Relationship.Builder().name("success").build()))
                .build();

        FlowFileQueue flowFileQueue = connection.getFlowFileQueue();
        flowFileQueue.setPriorities(Collections.singletonList(flowController.createPrioritizer(getValueString(connectionProperties, QUEUE_PRIORITIZER_CLASS_KEY))));
        flowFileQueue.setBackPressureDataSizeThreshold(getValueString(connectionProperties, MAX_WORK_QUEUE_DATA_SIZE_KEY));
        flowFileQueue.setBackPressureObjectThreshold(Long.parseLong(getValueString(connectionProperties, MAX_WORK_QUEUE_SIZE_KEY)));
        flowFileQueue.setFlowFileExpiration(getValueString(connectionProperties, QUEUE_PRIORITIZER_CLASS_KEY));

        processorNode.addConnection(connection);

        StandardXMLFlowConfigurationDAO standardXMLFlowConfigurationDAO = new StandardXMLFlowConfigurationDAO(Paths.get(path), stringEncryptor);
        standardXMLFlowConfigurationDAO.save(flowController);
    }

    private static void writeFlowXmlHardCoded(Map<String, Object> topLevelYaml, String path) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new GZIPOutputStream(new FileOutputStream(path+"flow.xml.gz")));

            Map<String,Object> flowControllerProperties = (Map<String, Object>) topLevelYaml.get(FLOW_CONTROLLER_PROPS_KEY);
            Map<String,Object> processorConfig = (Map<String, Object>) topLevelYaml.get(PROCESSOR_CONFIG_KEY);
            Map<String,Object> processorProperties = (Map<String, Object>) processorConfig.get(PROCESSOR_PROPS_KEY);
            Map<String,Object> connectionProperties = (Map<String, Object>) topLevelYaml.get(CONNECTION_PROPS_KEY);
            Map<String,Object> remoteProcessingGroup = (Map<String, Object>) topLevelYaml.get(REMOTE_PROCESSING_GROUP_KEY);
            Map<String,Object> inputPort = (Map<String, Object>) remoteProcessingGroup.get(INPUT_PORT_KEY);

            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
            writer.println("<flowController>");
            writer.println("  <maxTimerDrivenThreadCount>" + getValueString(processorConfig,MAX_CONCURRENT_TASKS_KEY) + "</maxTimerDrivenThreadCount>");
            writer.println("  <maxEventDrivenThreadCount>" + getValueString(processorConfig,MAX_CONCURRENT_TASKS_KEY) + "</maxEventDrivenThreadCount>");
            writer.println("  <rootGroup>");
            writer.println("    <id>Root-Group</id>");
            writer.println("    <name>" + getValueString(flowControllerProperties, NAME_KEY) + "</name>");
            writer.println("    <position x=\"0.0\" y=\"0.0\"/>");
            writer.println("    <comment>" + getValueString(flowControllerProperties, COMMENT_KEY) + "</comment>");
            writer.println("    <processor>");
            writer.println("      <id>Processor</id>");
            writer.println("      <name>" + getValueString(processorConfig, NAME_KEY) + "</name>");
            writer.println("      <position x=\"0.0\" y=\"0.0\"/>");
            writer.println("      <styles/>");
            writer.println("      <comment>" + getValueString(processorConfig, COMMENT_KEY) + "</comment>");
            writer.println("      <class>" + getValueString(processorConfig, CLASS_KEY) + "</class>");
            writer.println("      <maxConcurrentTasks>" + getValueString(processorConfig, MAX_CONCURRENT_TASKS_KEY) + "</maxConcurrentTasks>");
            writer.println("      <schedulingPeriod>" + getValueString(processorConfig, SCHEDULING_PERIOD_KEY) + "</schedulingPeriod>");
            writer.println("      <penalizationPeriod>" + getValueString(processorConfig, PENALIZATION_PERIOD_KEY) + "</penalizationPeriod>");
            writer.println("      <yieldPeriod>" + getValueString(processorConfig, YIELD_PERIOD_KEY) + "</yieldPeriod>");
            writer.println("      <bulletinLevel>WARN</bulletinLevel>");
            writer.println("      <lossTolerant>false</lossTolerant>");
            writer.println("      <scheduledState>RUNNING</scheduledState>");
            writer.println("      <schedulingStrategy>" + getValueString(processorConfig, SCHEDULING_STRATEGY_KEY) + "</schedulingStrategy>");
            writer.println("      <runDurationNanos>" + getValueString(processorConfig, RUN_DURATION_NANOS_KEY) + "</runDurationNanos>");
            for(String key: processorProperties.keySet()){
                writer.println("      <property>");
                writer.println("        <name>" + key + "</name>");
                writer.println("        <value>" + getValueString(processorProperties, key) + "</value>");
                writer.println("      </property>");
            }
            writer.println("    </processor>");
            writer.println("    <remoteProcessGroup>");
            writer.println("      <id>Remote-Process-Group</id>");
            writer.println("      <name>" + getValueString(remoteProcessingGroup, NAME_KEY) + "</name>");
            writer.println("      <position x=\"0.0\" y=\"0.0\"/>");
            writer.println("      <comment>" + getValueString(remoteProcessingGroup, COMMENT_KEY) + "</comment>");
            writer.println("      <url>" + getValueString(remoteProcessingGroup, URL_KEY) + "</url>");
            writer.println("      <timeout>" + getValueString(remoteProcessingGroup, TIMEOUT_KEY) + "</timeout>");
            writer.println("      <yieldPeriod>" + getValueString(remoteProcessingGroup, YIELD_PERIOD_KEY) + "</yieldPeriod>");
            writer.println("      <transmitting>true</transmitting>");
            writer.println("      <inputPort>");
            writer.println("        <id>" + getValueString(inputPort,ID_KEY) + "</id>");
            writer.println("        <name>" + getValueString(inputPort, NAME_KEY) + "</name>");
            writer.println("        <position x=\"0.0\" y=\"0.0\"/>");
            writer.println("        <comments>" + getValueString(inputPort, COMMENT_KEY) + "</comments>");
            writer.println("        <scheduledState>RUNNING</scheduledState>");
            writer.println("        <maxConcurrentTasks>" + getValueString(inputPort, MAX_CONCURRENT_TASKS_KEY) + "</maxConcurrentTasks>");
            writer.println("        <useCompression>" + getValueString(inputPort, USE_COMPRESSION_KEY) + "</useCompression>");
            writer.println("      </inputPort>");
            writer.println("    </remoteProcessGroup>");
            writer.println("    <connection>");
            writer.println("      <id>Connection</id>");
            writer.println("      <name>" + getValueString(connectionProperties, NAME_KEY) + "</name>");
            writer.println("      <bendPoints/>");
            writer.println("      <labelIndex>1</labelIndex>");
            writer.println("      <zIndex>0</zIndex>");
            writer.println("      <sourceId>Processor</sourceId>");
            writer.println("      <sourceGroupId>Root-Group</sourceGroupId>");
            writer.println("      <sourceType>PROCESSOR</sourceType>");
            writer.println("      <destinationId>" + getValueString(inputPort,ID_KEY) + "</destinationId>");
            writer.println("      <destinationGroupId>Remote-Process-Group</destinationGroupId>");
            writer.println("      <destinationType>REMOTE_INPUT_PORT</destinationType>");
            writer.println("      <relationship>success</relationship>");
            writer.println("      <maxWorkQueueSize>" + getValueString(connectionProperties, MAX_WORK_QUEUE_SIZE_KEY) + "</maxWorkQueueSize>");
            writer.println("      <maxWorkQueueDataSize>" + getValueString(connectionProperties, MAX_WORK_QUEUE_DATA_SIZE_KEY) + "</maxWorkQueueDataSize>");
            writer.println("      <flowFileExpiration>" + getValueString(connectionProperties, FLOWFILE_EXPIRATION__KEY) + "</flowFileExpiration>");
            writer.println("      <queuePrioritizerClass>" + getValueString(connectionProperties, QUEUE_PRIORITIZER_CLASS_KEY) + "</queuePrioritizerClass>");
            writer.println("    </connection>");
            writer.println("  </rootGroup>");
            writer.println("  <controllerServices/>");
            writer.println("  <reportingTasks/>");
            writer.println("</flowController>");
        } finally {
            if (writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }

    public static <K> String getValueString(Map<K,Object> map, K key){
        Object value = map.get(key);
        return value == null ? "" : value.toString();

    }


    public static final String PROPERTIES_FILE_APACHE_2_0_LICENSE =
            "# Licensed to the Apache Software Foundation (ASF) under one or more\n" +
            "# contributor license agreements.  See the NOTICE file distributed with\n" +
            "# this work for additional information regarding copyright ownership.\n" +
            "# The ASF licenses this file to You under the Apache License, Version 2.0\n" +
            "# (the \"License\"); you may not use this file except in compliance with\n" +
            "# the License.  You may obtain a copy of the License at\n" +
            "#\n" +
            "#     http://www.apache.org/licenses/LICENSE-2.0\n" +
            "#\n" +
            "# Unless required by applicable law or agreed to in writing, software\n" +
            "# distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
            "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
            "# See the License for the specific language governing permissions and\n" +
            "# limitations under the License.\n"+
            "\n";

}
