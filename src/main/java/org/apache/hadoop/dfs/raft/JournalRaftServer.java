package org.apache.hadoop.dfs.raft;

import net.data.technology.jraft.*;
import net.data.technology.jraft.extensions.FileBasedServerStateManager;
import net.data.technology.jraft.extensions.RpcTcpClientFactory;
import net.data.technology.jraft.extensions.RpcTcpListener;
import org.apache.hadoop.dfs.FSNamesystem;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class JournalRaftServer {
    static final private Logger logger = LoggerFactory.getLogger(JournalRaftServer.class);
    private String configDir;

    public FSNamesystem getNamesystem() {
        return namesystem;
    }

    private FSNamesystem namesystem;

    RaftServer raftServer;
    public JournalRaftServer(String configDir, FSNamesystem namesystem) {
        this.configDir = configDir;
        this.namesystem = namesystem;
        logger.info("construct JournalRaftServer with config dir {}", configDir);
    }

    public void start() {
        ScheduledThreadPoolExecutor executor =
                new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        RaftParameters raftParameters = new RaftParameters()
                .withElectionTimeoutUpper(5000)
                .withElectionTimeoutLower(3000)
                .withHeartbeatInterval(1500)
                .withRpcFailureBackoff(500)
                .withMaximumAppendingSize(200)
                .withLogSyncBatchSize(5)
                .withLogSyncStoppingGap(5)
                .withSnapshotEnabled(5000)
                .withSyncSnapshotBlockSize(0);

        Path baseDir = Paths.get(configDir);
        if(!Files.isDirectory(baseDir)){
            System.out.printf("%s does not exist as a directory\n", configDir);
            logger.error("{} does not exist as a directory", configDir);
            return;
        }
        FileBasedServerStateManager stateManager = new FileBasedServerStateManager(configDir);
        ClusterConfiguration config = stateManager.loadClusterConfiguration();
        URI localEndpoint = null;
        try {
            localEndpoint = new URI(config.getServer(stateManager.getServerId()).getEndpoint());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        JournalStateMachine stateMachine = new JournalStateMachine(localEndpoint, this);
        RaftContext context = new RaftContext(stateManager, stateMachine, raftParameters,
                    new RpcTcpListener(localEndpoint.getPort(), executor),
                    new RpcTcpClientFactory(executor),
                    executor);

        // run it
        raftServer = new RaftServer(context);
        RaftMessageSender messageSender = raftServer.createMessageSender();
        context.getStateMachine().start(messageSender);
        context.getRpcListener().startListening(raftServer);
        logger.info("raft server {} start success", localEndpoint.toString());
    }

    public class Task {
        public byte[] getValue() {
            return value;
        }

        private final byte[] value;
        public Task(byte[] value) {
            this.value = value;
        }
    }
    public boolean apply(Task task) {
        LogEntry[] logEntries = new LogEntry[1];
        logEntries[0] = new LogEntry(0, task.getValue());

        RaftRequestMessage request = new RaftRequestMessage();
        request.setMessageType(RaftMessageType.ClientRequest);
        request.setLogEntries(logEntries);

        RaftResponseMessage response = raftServer.processRequest(request);

        return response.isAccepted();
    }
}
